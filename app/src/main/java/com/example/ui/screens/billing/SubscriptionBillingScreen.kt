package com.example.ui.screens.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.SubscriptionPlan
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SubscriptionBillingScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val currentPlan by repository.currentPlan.collectAsState()
    val commissionRate by repository.commissionRate.collectAsState()

    val plans = remember {
        listOf(
            SubscriptionPlan(
                id = "starter",
                name = "Starter Plan",
                priceMonthly = 999,
                priceFormatted = "₹999/mo",
                userLimit = "Up to 5 Users",
                orderLimit = "500 Orders/Month",
                features = listOf("Up to 5 Users", "500 Orders/Month", "Basic Analytics", "Inventory Management", "Standard Email Support"),
                isPopular = false
            ),
            SubscriptionPlan(
                id = "growth",
                name = "Growth Plan",
                priceMonthly = 4999,
                priceFormatted = "₹4,999/mo",
                userLimit = "Up to 50 Users",
                orderLimit = "Unlimited Orders/Month",
                features = listOf("Up to 50 Users", "Unlimited Orders", "AI Demand Forecasting", "Supplier Marketplace", "Logistics Tracking", "Smart Inventory Reorders"),
                isPopular = true
            ),
            SubscriptionPlan(
                id = "enterprise",
                name = "Enterprise Plan",
                priceMonthly = 19999,
                priceFormatted = "₹19,999/mo",
                userLimit = "Unlimited Users",
                orderLimit = "Unlimited Orders/Month",
                features = listOf("Unlimited Users & Seats", "Advanced Neural AI Engine", "Custom ERP Integration (SAP/Oracle)", "Dedicated Account Architect", "White Label Tenant"),
                isPopular = false
            )
        )
    }

    var selectedGateway by remember { mutableStateOf("Razorpay") }
    var showCheckoutDialog by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showPaymentSuccessSnackbar by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp).testTag("subscription_billing_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("SaaS Subscription & Billing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Choose a plan tailored for your manufacturing volume and AI requirements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Current Active Plan Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LinkoraPrimary), width = 1.5.dp),
                modifier = Modifier.fillMaxWidth().testTag("active_plan_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT SUBSCRIPTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary, letterSpacing = 1.sp)
                            Text(currentPlan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Surface(color = StatusSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text("ACTIVE • AUTO-RENEW ON", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = StatusSuccess, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Billed monthly at ${currentPlan.priceFormatted} + 18% GST via $selectedGateway auto-debit.", style = MaterialTheme.typography.bodySmall)
                    Text("Marketplace Commission: ${commissionRate}% on concluded transactions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            SectionHeader(title = "Payment Gateway Selection", subtitle = "Enterprise-grade checkout integration")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = selectedGateway == "Razorpay",
                    onClick = { selectedGateway = "Razorpay" },
                    label = { Text("Razorpay (UPI / NetBanking / Cards)") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedGateway == "Stripe",
                    onClick = { selectedGateway = "Stripe" },
                    label = { Text("Stripe (Global USD / Wire)") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        item {
            SectionHeader(title = "Available Subscription Tiers", subtitle = "Instant upgrade or downgrade with prorated billing")
        }

        items(plans) { plan ->
            val isCurrent = currentPlan.id == plan.id
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("plan_card_${plan.id}"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(plan.priceFormatted, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        }

                        if (plan.isPopular) {
                            Surface(color = LinkoraAIIndigo.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                                Text("MOST POPULAR", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = LinkoraAIIndigo, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        plan.features.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                                Text(feat, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (isCurrent) {
                            OutlinedButton(onClick = {}, enabled = false) {
                                Text("Current Plan")
                            }
                        } else {
                            Button(
                                onClick = { showCheckoutDialog = plan },
                                modifier = Modifier.testTag("select_plan_${plan.id}")
                            ) {
                                Text("Upgrade with $selectedGateway")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog != null) {
        val targetPlan = showCheckoutDialog!!
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = null },
            title = { Text("$selectedGateway Checkout", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plan: ${targetPlan.name}")
                    Text("Monthly Subscription: ${targetPlan.priceFormatted}")
                    Text("Applicable GST (18%): ₹${String.format("%,.0f", targetPlan.priceMonthly * 0.18)}")
                    val totalCharge = targetPlan.priceMonthly * 1.18
                    Text("Total Charged: ₹${String.format("%,.0f", totalCharge)} / month", fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                    Text("Auto-renewal enabled. Cancel anytime in compliance with RBI e-mandate rules.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updatePlan(targetPlan)
                        showCheckoutDialog = null
                        showPaymentSuccessSnackbar = true
                    },
                    modifier = Modifier.testTag("confirm_payment_btn")
                ) {
                    Text("Authorize & Pay")
                }
            },
            dismissButton = { TextButton(onClick = { showCheckoutDialog = null }) { Text("Cancel") } }
        )
    }
}
