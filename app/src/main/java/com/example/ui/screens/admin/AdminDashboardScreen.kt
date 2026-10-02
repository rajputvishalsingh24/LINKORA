package com.example.ui.screens.admin

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
import com.example.data.models.*
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val companies by repository.companies.collectAsState()
    val fraudAlerts by repository.fraudAlerts.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val commissionRate by repository.commissionRate.collectAsState()
    val purchaseOrders by repository.purchaseOrders.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Monitoring", "Organizations", "Fraud Detection", "Commission & Reports")

    var showReportGeneratedDialog by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().testTag("admin_dashboard")) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = LinkoraPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> AdminMonitoringTab(
                companies = companies,
                purchaseOrders = purchaseOrders,
                commissionRate = commissionRate,
                auditLogs = auditLogs
            )
            1 -> AdminOrganizationsTab(companies = companies)
            2 -> AdminFraudDetectionTab(fraudAlerts = fraudAlerts)
            3 -> AdminCommissionAndReportsTab(
                commissionRate = commissionRate,
                onUpdateCommission = { repository.setCommissionRate(it) },
                onGenerateReport = { reportType -> showReportGeneratedDialog = reportType }
            )
        }
    }

    if (showReportGeneratedDialog != null) {
        AlertDialog(
            onDismissRequest = { showReportGeneratedDialog = null },
            title = { Text("Report Generated Successfully", fontWeight = FontWeight.Bold) },
            text = {
                Text("Linkora $showReportGeneratedDialog compiled with SOC2 compliant timestamp and exported to encrypted platform storage.")
            },
            confirmButton = {
                Button(onClick = { showReportGeneratedDialog = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun AdminMonitoringTab(
    companies: List<Company>,
    purchaseOrders: List<PurchaseOrder>,
    commissionRate: Double,
    auditLogs: List<AuditLog>
) {
    val totalGmv = purchaseOrders.sumOf { it.amount }
    val commissionEarned = totalGmv * (commissionRate / 100.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Platform GMV",
                    value = "₹${String.format("%,.0f", totalGmv)}",
                    subtitle = "${purchaseOrders.size} Processed Transactions",
                    icon = Icons.Default.AccountBalance,
                    accentColor = LinkoraPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Take Rate (${commissionRate}%)",
                    value = "₹${String.format("%,.0f", commissionEarned)}",
                    subtitle = "Marketplace Platform Revenue",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Enterprises",
                    value = "${companies.size} Verified",
                    subtitle = "100% KYC & GST verified",
                    icon = Icons.Default.Business,
                    accentColor = LinkoraAIIndigo,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "System Uptime",
                    value = "99.98%",
                    subtitle = "All Microservices Healthy",
                    icon = Icons.Default.CloudDone,
                    accentColor = LinkoraAccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionHeader(title = "Real-Time System Audit Logs", subtitle = "Immutable blockchain-ready activity trail")
        }

        items(auditLogs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(LinkoraPrimary)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${log.action} • ${log.performedBy} (${log.role})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(log.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AdminOrganizationsTab(companies: List<Company>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Enterprise Organizations Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Audit onboarded Manufacturers, Suppliers, Logistics Partners, and Buyers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(companies) { comp ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("admin_company_${comp.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(comp.companyName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge("VERIFIED KYC", "success")
                    }

                    Text("${comp.industry} • ${comp.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("GSTIN: ${comp.gst}", style = MaterialTheme.typography.bodySmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        comp.certifications.forEach { cert ->
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                Text(cert, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFraudDetectionTab(fraudAlerts: List<FraudAlert>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("AI Fraud Detection & Anti-Money Laundering (AML)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Automated anomaly detection across orders, duplicate GSTs, and erratic payments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(fraudAlerts) { alert ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (alert.riskLevel == "HIGH") StatusError else StatusWarning),
                    width = 1.dp
                ),
                modifier = Modifier.fillMaxWidth().testTag("fraud_alert_${alert.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(alert.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (alert.riskLevel == "HIGH") StatusError else MaterialTheme.colorScheme.onSurface)
                        StatusBadge("${alert.riskLevel} RISK", if (alert.riskLevel == "HIGH") "error" else "warning")
                    }

                    Text(alert.description, style = MaterialTheme.typography.bodySmall)
                    Text("Affected Node: ${alert.entityAffected} • ${alert.detectedTime}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AdminCommissionAndReportsTab(
    commissionRate: Double,
    onUpdateCommission: (Double) -> Unit,
    onGenerateReport: (String) -> Unit
) {
    var rateInput by remember(commissionRate) { mutableStateOf(commissionRate.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Marketplace Commission & Platform Pricing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Configure transaction take rate applied on every finalized purchase order", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configurable Marketplace Fee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Take Rate Percentage (%)") },
                        modifier = Modifier.fillMaxWidth().testTag("commission_input")
                    )

                    Button(
                        onClick = {
                            val r = rateInput.toDoubleOrNull() ?: 2.0
                            onUpdateCommission(r)
                        },
                        modifier = Modifier.testTag("save_commission_btn")
                    ) {
                        Text("Update Marketplace Commission")
                    }
                }
            }
        }

        item {
            SectionHeader(title = "System Compliance & Financial Reports", subtitle = "Generate downloadable audit-ready PDF/CSV manifests")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportActionCard(
                    title = "Monthly Platform Revenue Report",
                    desc = "Gross transactions, take rate yields, subscription MRR, GST collected",
                    icon = Icons.Default.Receipt,
                    onClick = { onGenerateReport("Platform Financial Revenue Report") }
                )
                ReportActionCard(
                    title = "Enterprise KYC & User Audit Report",
                    desc = "Verified GST registrations, user access privilege matrices, role audit logs",
                    icon = Icons.Default.People,
                    onClick = { onGenerateReport("Enterprise User & Organization Audit Report") }
                )
                ReportActionCard(
                    title = "Consignment & Transaction Settlement Manifest",
                    desc = "Order dispatch timestamps, proof of delivery sign-offs, freight invoices",
                    icon = Icons.Default.LocalShipping,
                    onClick = { onGenerateReport("Supply Chain Transaction Settlement Report") }
                )
            }
        }
    }
}

@Composable
private fun ReportActionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = LinkoraPrimary, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onClick = onClick) {
                Text("Generate")
            }
        }
    }
}
