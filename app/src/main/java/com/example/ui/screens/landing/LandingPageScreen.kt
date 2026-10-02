package com.example.ui.screens.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun LandingPageScreen(
    onSelectRole: (UserRole) -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDemoDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("landing_page_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Section
        item {
            HeroSection(
                onStartTrial = onNavigateToBilling,
                onScheduleDemo = { showDemoDialog = true },
                onExploreAI = onNavigateToAI
            )
        }

        // Quick Role Portal Launchers
        item {
            Text(
                text = "ENTERPRISE ROLE HUBS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LinkoraPrimary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Select an operational workspace to explore live workflows",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RoleLaunchCard(
                    title = "Manufacturer Portal",
                    subtitle = "Production planning, Smart Inventory, Purchase Orders & Quality Inspection",
                    icon = Icons.Default.Factory,
                    accentColor = LinkoraPrimary,
                    onClick = { onSelectRole(UserRole.MANUFACTURER) }
                )
                RoleLaunchCard(
                    title = "Supplier Portal",
                    subtitle = "Product catalogue, Quotations (RFQ), Invoicing & Dispatch fulfillment",
                    icon = Icons.Default.Storefront,
                    accentColor = LinkoraAccentTeal,
                    onClick = { onSelectRole(UserRole.SUPPLIER) }
                )
                RoleLaunchCard(
                    title = "Logistics Portal",
                    subtitle = "Shipment marketplace, AI Route Optimization & GPS fleet telemetry",
                    icon = Icons.Default.LocalShipping,
                    accentColor = LinkoraAIIndigo,
                    onClick = { onSelectRole(UserRole.LOGISTICS) }
                )
                RoleLaunchCard(
                    title = "Buyer Portal",
                    subtitle = "Supplier discovery, Quotation comparison engine & Real-time tracking",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = StatusSuccess,
                    onClick = { onSelectRole(UserRole.BUYER) }
                )
                RoleLaunchCard(
                    title = "Platform Admin",
                    subtitle = "User governance, platform revenue, 2% commission & AI fraud detection",
                    icon = Icons.Default.AdminPanelSettings,
                    accentColor = StatusWarning,
                    onClick = { onSelectRole(UserRole.ADMIN) }
                )
            }
        }

        // Platform Features
        item {
            FeaturesGridSection()
        }

        // Target Industries Section
        item {
            IndustriesSection()
        }

        // Testimonials Section
        item {
            TestimonialsSection()
        }

        // Pricing Plans Summary
        item {
            PricingSummarySection(onUpgrade = onNavigateToBilling)
        }

        // FAQs Section
        item {
            FaqSection()
        }

        // Footer
        item {
            FooterSection()
        }
    }

    if (showDemoDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDialog = false },
            title = { Text("Schedule Enterprise Demo") },
            text = {
                Text("Linkora Enterprise Specialists are available 24/7. All 5 role portals and the AI Predictive Engine are active in this applet.")
            },
            confirmButton = {
                Button(
                    onClick = { showDemoDialog = false },
                    modifier = Modifier.testTag("dismiss_demo_dialog")
                ) {
                    Text("Explore Portals Now")
                }
            }
        )
    }
}

@Composable
private fun HeroSection(
    onStartTrial: () -> Unit,
    onScheduleDemo: () -> Unit,
    onExploreAI: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(LinkoraPrimary, LinkoraAccentTeal, LinkoraAIIndigo)),
            width = 1.5.dp
        ),
        modifier = Modifier.fillMaxWidth().testTag("hero_section")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tagline chip
            Surface(
                color = LinkoraPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusSuccess)
                    )
                    Text(
                        text = "CONNECT. PREDICT. DELIVER.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LinkoraPrimary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Text(
                text = "AI-Powered Supply Chain Intelligence for Modern Manufacturing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Manage suppliers, inventory, logistics, procurement, and neural demand forecasting from a single enterprise platform designed for SMBs and OEM manufacturers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStartTrial,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("start_trial_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = LinkoraPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Free Trial", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onScheduleDemo,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("schedule_demo_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Schedule Demo", fontWeight = FontWeight.SemiBold)
                }
            }

            FilledTonalButton(
                onClick = onExploreAI,
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("explore_ai_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = LinkoraAIIndigo)
                Spacer(Modifier.width(8.dp))
                Text("Open AI Intelligence Command Center", fontWeight = FontWeight.Bold, color = LinkoraAIIndigo)
            }
        }
    }
}

@Composable
private fun RoleLaunchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_launch_${title.take(8)}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FeaturesGridSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "ENTERPRISE CAPABILITIES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = LinkoraPrimary
        )
        Text(
            text = "Combining SAP S/4HANA, NetSuite & Flexport agility",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val features = listOf(
            Triple(Icons.Default.TrendingUp, "Demand Forecasting", "Predict monthly & seasonal demand with 94% neural confidence"),
            Triple(Icons.Default.Inventory2, "Smart Inventory AI", "Autonomous stock depletion warning and dynamic reorder thresholding"),
            Triple(Icons.Default.Storefront, "Supplier Marketplace", "Discover, audit, compare & send direct RFQs to certified manufacturers"),
            Triple(Icons.Default.GpsFixed, "Logistics Tracking", "Live waypoint GPS telemetry with real-time ETA updates and delay alerts"),
            Triple(Icons.Default.Route, "Route Optimization", "AI multi-modal route engine: lowest cost vs fastest expressway corridor"),
            Triple(Icons.Default.PieChart, "Cost Analytics", "Deep cost attribution across procurement, manufacturing lines & freight")
        )

        features.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (icon, title, desc) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = LinkoraPrimary, modifier = Modifier.size(22.dp))
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndustriesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "INDUSTRIES POWERED",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = LinkoraPrimary
        )

        val industries = listOf("Manufacturing", "Automotive & EV", "Electronics & Semi", "Steel & Metallurgy", "FMCG", "Technical Textiles")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(industries) { ind ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = ind,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TestimonialsSection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("Enterprise Verified", style = MaterialTheme.typography.labelSmall, color = StatusSuccess, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "\"Linkora replaced 4 fragmented spreadsheets and reduced our raw steel stockouts by 34% in the very first quarter. The AI demand forecasting is shockingly accurate.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(LinkoraPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text("Vikram Malhotra", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("VP Operations, Apex Industrial Systems", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PricingSummarySection(onUpgrade: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SaaS Subscription Tiers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Starting at ₹999/mo with 2% marketplace commission", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onUpgrade) {
                    Text("View Plans")
                }
            }
        }
    }
}

@Composable
private fun FaqSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("FREQUENTLY ASKED QUESTIONS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("How does Linkora AI Demand Forecasting work?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Linkora ingests historical ERP purchase orders, production line throughput, seasonal macroeconomic indices, and commodity prices to project run-out dates and forecast upcoming volume surges.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "LINKORA PLATFORM",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LinkoraPrimary
        )
        Text(
            text = "Connect. Predict. Deliver. • ISO 27001 & SOC2 Ready Architecture",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
