package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserRole
import com.example.data.repository.LinkoraRepository
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.ai.AIIntelligenceHubScreen
import com.example.ui.screens.billing.SubscriptionBillingScreen
import com.example.ui.screens.buyer.BuyerDashboardScreen
import com.example.ui.screens.landing.LandingPageScreen
import com.example.ui.screens.logistics.LogisticsDashboardScreen
import com.example.ui.screens.manufacturer.ManufacturerDashboardScreen
import com.example.ui.screens.supplier.SupplierDashboardScreen
import com.example.ui.theme.*

enum class AppDestination(val label: String, val icon: ImageVector) {
    LANDING("Home", Icons.Default.Home),
    PORTAL("Portal", Icons.Default.Dashboard),
    AI_HUB("AI Engine", Icons.Default.AutoAwesome),
    BILLING("SaaS Billing", Icons.Default.Payment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkoraApp(
    repository: LinkoraRepository = remember { LinkoraRepository() }
) {
    val currentUser by repository.currentUser.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val currentPlan by repository.currentPlan.collectAsState()

    var currentDestination by remember { mutableStateOf(AppDestination.PORTAL) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Android back handler
    BackHandler(enabled = currentDestination != AppDestination.LANDING) {
        if (currentDestination != AppDestination.PORTAL) {
            currentDestination = AppDestination.PORTAL
        } else {
            currentDestination = AppDestination.LANDING
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("linkora_main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Brand Logo Icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(LinkoraPrimary, LinkoraAccentTeal))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = "Linkora Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "LINKORA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    color = LinkoraAIIndigo.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AI",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LinkoraAIIndigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Connect. Predict. Deliver.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Role Switcher Pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { showRoleDialog = true }
                            .testTag("role_switcher_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val roleIcon = when (currentUser.role) {
                                UserRole.MANUFACTURER -> Icons.Default.Factory
                                UserRole.SUPPLIER -> Icons.Default.Storefront
                                UserRole.LOGISTICS -> Icons.Default.LocalShipping
                                UserRole.BUYER -> Icons.Default.ShoppingBag
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            }
                            Icon(roleIcon, contentDescription = null, tint = LinkoraPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = currentUser.role.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Role", modifier = Modifier.size(16.dp))
                        }
                    }

                    // Notifications Action
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notifications_topbar_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = StatusWarning) {
                                    Text("${auditLogs.size.coerceAtMost(9)}")
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppDestination.values().forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(dest.icon, contentDescription = dest.label)
                        },
                        label = {
                            Text(
                                text = if (dest == AppDestination.PORTAL) currentUser.role.label.take(12) else dest.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LinkoraPrimary,
                            indicatorColor = LinkoraPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.LANDING -> {
                    LandingPageScreen(
                        onSelectRole = { role ->
                            repository.switchRole(role)
                            currentDestination = AppDestination.PORTAL
                        },
                        onNavigateToBilling = { currentDestination = AppDestination.BILLING },
                        onNavigateToAI = { currentDestination = AppDestination.AI_HUB }
                    )
                }
                AppDestination.PORTAL -> {
                    when (currentUser.role) {
                        UserRole.MANUFACTURER -> ManufacturerDashboardScreen(
                            repository = repository,
                            onNavigateToAI = { currentDestination = AppDestination.AI_HUB }
                        )
                        UserRole.SUPPLIER -> SupplierDashboardScreen(
                            repository = repository
                        )
                        UserRole.LOGISTICS -> LogisticsDashboardScreen(
                            repository = repository
                        )
                        UserRole.BUYER -> BuyerDashboardScreen(
                            repository = repository
                        )
                        UserRole.ADMIN -> AdminDashboardScreen(
                            repository = repository
                        )
                    }
                }
                AppDestination.AI_HUB -> {
                    AIIntelligenceHubScreen(repository = repository)
                }
                AppDestination.BILLING -> {
                    SubscriptionBillingScreen(repository = repository)
                }
            }
        }
    }

    // Role Picker Dialog
    if (showRoleDialog) {
        RolePickerDialog(
            currentRole = currentUser.role,
            onDismiss = { showRoleDialog = false },
            onSelectRole = { newRole ->
                repository.switchRole(newRole)
                showRoleDialog = false
                currentDestination = AppDestination.PORTAL
            },
            onGoToLanding = {
                showRoleDialog = false
                currentDestination = AppDestination.LANDING
            }
        )
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = LinkoraPrimary)
                    Text("Enterprise Notifications", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    auditLogs.take(5).forEach { log ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.action, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                                    Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(log.details, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) { Text("Dismiss") }
            }
        )
    }
}

@Composable
private fun RolePickerDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onGoToLanding: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Switch Enterprise Portal Role", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select an authenticated organizational persona to explore its dedicated workflow:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                UserRole.values().forEach { role ->
                    val isSelected = currentRole == role
                    Surface(
                        color = if (isSelected) LinkoraPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LinkoraPrimary), width = 1.5.dp) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRole(role) }
                            .testTag("dialog_role_option_${role.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val icon = when (role) {
                                UserRole.MANUFACTURER -> Icons.Default.Factory
                                UserRole.SUPPLIER -> Icons.Default.Storefront
                                UserRole.LOGISTICS -> Icons.Default.LocalShipping
                                UserRole.BUYER -> Icons.Default.ShoppingBag
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            }
                            Icon(icon, contentDescription = null, tint = if (isSelected) LinkoraPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(role.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                val desc = when (role) {
                                    UserRole.MANUFACTURER -> "Production planning, inventory & PO approvals"
                                    UserRole.SUPPLIER -> "Catalogue, quotations & dispatch fulfillment"
                                    UserRole.LOGISTICS -> "Consignments, GPS fleet tracking & route AI"
                                    UserRole.BUYER -> "Supplier discovery, RFQs & consignment tracking"
                                    UserRole.ADMIN -> "Governance, GMV revenue & fraud detection"
                                }
                                Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = LinkoraPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()
                OutlinedButton(
                    onClick = onGoToLanding,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_view_landing_page_btn")
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Public SaaS Landing Page")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
