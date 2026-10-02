package com.example.ui.screens.manufacturer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManufacturerDashboardScreen(
    repository: LinkoraRepository,
    onNavigateToAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inventory by repository.inventory.collectAsState()
    val purchaseOrders by repository.purchaseOrders.collectAsState()
    val shipments by repository.shipments.collectAsState()
    val productionPlans by repository.productionPlans.collectAsState()
    val qualityReports by repository.qualityReports.collectAsState()
    val aiPredictions by repository.aiPredictions.collectAsState()
    val companies by repository.companies.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Purchase Orders", "Inventory", "Production & QA", "Suppliers")

    var showCreatePODialog by remember { mutableStateOf(false) }
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var showRFQDialog by remember { mutableStateOf(false) }
    var targetSupplierForRFQ by remember { mutableStateOf<Company?>(null) }

    Column(modifier = modifier.fillMaxSize().testTag("manufacturer_dashboard")) {
        // Tab Row
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
                    modifier = Modifier.testTag("mfg_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> ManufacturerOverviewTab(
                inventory = inventory,
                purchaseOrders = purchaseOrders,
                shipments = shipments,
                aiPredictions = aiPredictions,
                onNavigateToAI = onNavigateToAI,
                onViewPOs = { selectedTab = 1 },
                onViewInventory = { selectedTab = 2 }
            )
            1 -> PurchaseOrdersTab(
                purchaseOrders = purchaseOrders,
                onApprove = { poId -> repository.updatePOStatus(poId, POStatus.APPROVED) },
                onReject = { poId -> repository.updatePOStatus(poId, POStatus.REJECTED) },
                onMarkDelivered = { poId -> repository.updatePOStatus(poId, POStatus.DELIVERED) },
                onCreatePOClick = { showCreatePODialog = true }
            )
            2 -> InventoryTab(
                inventory = inventory,
                onAddInventoryClick = { showAddInventoryDialog = true },
                onAdjustStock = { itemId, delta -> repository.updateInventoryStock(itemId, delta) },
                onDeleteItem = { itemId -> repository.deleteInventoryItem(itemId) }
            )
            3 -> ProductionAndQualityTab(
                productionPlans = productionPlans,
                qualityReports = qualityReports
            )
            4 -> SuppliersMarketplaceTab(
                companies = companies,
                onRequestRFQ = { comp ->
                    targetSupplierForRFQ = comp
                    showRFQDialog = true
                }
            )
        }
    }

    // Create PO Dialog
    if (showCreatePODialog) {
        CreatePODialog(
            companies = companies.filter { it.id.contains("sup") },
            onDismiss = { showCreatePODialog = false },
            onCreate = { supplier, itemDesc, qty, rate, dueDate ->
                repository.createPurchaseOrder(
                    supplier = supplier,
                    items = listOf(OrderItem(itemDesc, qty, rate)),
                    dueDate = dueDate
                )
                showCreatePODialog = false
            }
        )
    }

    // Add Inventory Dialog
    if (showAddInventoryDialog) {
        AddInventoryDialog(
            onDismiss = { showAddInventoryDialog = false },
            onAdd = { name, cat, stock, minT, maxT, unit, loc ->
                repository.addInventoryItem(name, cat, stock, minT, maxT, unit, loc)
                showAddInventoryDialog = false
            }
        )
    }

    // RFQ Dialog
    if (showRFQDialog && targetSupplierForRFQ != null) {
        RFQDialog(
            supplier = targetSupplierForRFQ!!,
            onDismiss = { showRFQDialog = false },
            onSubmit = { product, qty, targetRate ->
                repository.submitQuotation(
                    rfqNumber = "RFQ-${(5000..5999).random()}",
                    supplierName = targetSupplierForRFQ!!.companyName,
                    buyerName = "Apex Industrial Systems",
                    product = product,
                    qty = qty,
                    price = targetRate,
                    leadDays = 5
                )
                showRFQDialog = false
            }
        )
    }
}

@Composable
private fun ManufacturerOverviewTab(
    inventory: List<InventoryItem>,
    purchaseOrders: List<PurchaseOrder>,
    shipments: List<Shipment>,
    aiPredictions: List<AIPrediction>,
    onNavigateToAI: () -> Unit,
    onViewPOs: () -> Unit,
    onViewInventory: () -> Unit
) {
    val lowStockCount = inventory.count { it.isLowStock }
    val pendingPOCount = purchaseOrders.count { it.status == POStatus.PENDING }
    val activeShipment = shipments.firstOrNull { it.status == ShipmentStatus.IN_TRANSIT }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Stat Cards Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Stock Health",
                    value = "${inventory.size - lowStockCount}/${inventory.size} SKUs",
                    subtitle = if (lowStockCount > 0) "$lowStockCount Low Stock Warning" else "Optimal Safety Stocks",
                    icon = Icons.Default.Inventory,
                    accentColor = if (lowStockCount > 0) StatusWarning else StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending POs",
                    value = "$pendingPOCount Orders",
                    subtitle = "₹${String.format("%,.0f", purchaseOrders.filter { it.status == POStatus.PENDING }.sumOf { it.amount })} Active",
                    icon = Icons.Default.ShoppingCart,
                    accentColor = LinkoraPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Capacity Load",
                    value = "88.4%",
                    subtitle = "3 CNC Cells Operational",
                    icon = Icons.Default.PrecisionManufacturing,
                    accentColor = LinkoraAIIndigo,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Supplier Score",
                    value = "98.2%",
                    subtitle = "On-Time Inbound Deliveries",
                    icon = Icons.Default.VerifiedUser,
                    accentColor = LinkoraAccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Top AI Insight Banner
        val topAi = aiPredictions.firstOrNull()
        if (topAi != null) {
            item {
                SectionHeader(
                    title = "AI Neural Supply Chain Insight",
                    subtitle = "Real-time predictive telemetry",
                    actionText = "Explore AI Hub",
                    onActionClick = onNavigateToAI
                )
                AIInsightCard(prediction = topAi, onActionClick = onNavigateToAI)
            }
        }

        // Active Logistics Status & Map
        if (activeShipment != null) {
            item {
                SectionHeader(title = "Inbound Logistics Corridor", subtitle = activeShipment.trackingNumber)
                InteractiveLogisticsMap(
                    origin = activeShipment.origin,
                    destination = activeShipment.destination,
                    currentCheckpoint = activeShipment.currentCheckpoint,
                    progress = activeShipment.progressPercent,
                    routeType = activeShipment.routeType
                )
            }
        }

        // Cost Analysis Breakdown Chart
        item {
            SectionHeader(title = "Cost Analysis", subtitle = "Procurement, Production & Freight")
            CostBreakdownChart()
        }

        // Quick PO Summary
        item {
            SectionHeader(
                title = "Recent Purchase Orders",
                subtitle = "Latest procurement transactions",
                actionText = "View All",
                onActionClick = onViewPOs
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                purchaseOrders.take(3).forEach { po ->
                    POCard(po = po, onApprove = null, onReject = null, onMarkDelivered = null)
                }
            }
        }
    }
}

@Composable
private fun PurchaseOrdersTab(
    purchaseOrders: List<PurchaseOrder>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onMarkDelivered: (String) -> Unit,
    onCreatePOClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Purchase Order Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${purchaseOrders.size} Total Orders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onCreatePOClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LinkoraPrimary),
                    modifier = Modifier.testTag("create_po_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Create PO")
                }
            }
        }

        items(purchaseOrders) { po ->
            POCard(
                po = po,
                onApprove = { onApprove(po.id) },
                onReject = { onReject(po.id) },
                onMarkDelivered = { onMarkDelivered(po.id) }
            )
        }
    }
}

@Composable
private fun InventoryTab(
    inventory: List<InventoryItem>,
    onAddInventoryClick: () -> Unit,
    onAdjustStock: (String, Int) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<InventoryCategory?>(null) }

    val filteredList = remember(inventory, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) inventory else inventory.filter { it.category == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Inventory Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Raw Materials, Finished Goods & Warehouse", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onAddInventoryClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_inventory_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add SKU")
                }
            }
        }

        // Category Filter Chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All SKUs (${inventory.size})") }
                )
                InventoryCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                        label = { Text(cat.label) }
                    )
                }
            }
        }

        items(filteredList) { item ->
            InventoryItemCard(
                item = item,
                onAdjustStock = { delta -> onAdjustStock(item.id, delta) },
                onDelete = { onDeleteItem(item.id) }
            )
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onAdjustStock: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${item.category.label} • ${item.warehouseLocation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.isLowStock) {
                    StatusBadge("LOW STOCK", "warning")
                } else {
                    StatusBadge("NORMAL", "success")
                }
            }

            // Stock Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${item.stock} ${item.unit} in stock",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Safety Min: ${item.minThreshold} | Max: ${item.maxThreshold}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { item.stockHealthPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (item.isLowStock) StatusWarning else LinkoraPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Actions: Quick stock adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusError, modifier = Modifier.size(20.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick Adjust:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalButton(
                        onClick = { onAdjustStock(-10) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("adjust_minus_${item.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("-10")
                    }
                    Button(
                        onClick = { onAdjustStock(50) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("adjust_plus_${item.id}"),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("+50")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductionAndQualityTab(
    productionPlans: List<ProductionPlan>,
    qualityReports: List<QualityReport>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title = "Production Planning & Scheduling", subtitle = "Capacity planning & active machinery runs")
        }

        items(productionPlans) { plan ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("prod_plan_${plan.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(plan.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(plan.status, if (plan.status == "In Progress") "info" else "success")
                    }

                    Text("${plan.productLine} • ${plan.shift}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val progress = plan.completedUnits.toFloat() / plan.targetUnits.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = LinkoraPrimary
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${plan.completedUnits} / ${plan.targetUnits} units produced (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("${plan.machineUtilizationPercent}% Machine Load", style = MaterialTheme.typography.bodySmall, color = LinkoraAccentTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            SectionHeader(title = "Quality Management & Inspection Reports", subtitle = "Defect tracking and batch compliance")
        }

        items(qualityReports) { qr ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("quality_report_${qr.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${qr.batchCode} - ${qr.itemName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(qr.complianceStatus, if (qr.complianceStatus == "Compliant") "success" else "warning")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Inspected: ${qr.inspectedCount} units", style = MaterialTheme.typography.bodySmall)
                        Text("Defects: ${qr.defectCount} (${String.format("%.2f", qr.defectRate)}%)", style = MaterialTheme.typography.bodySmall, color = if (qr.defectCount > 5) StatusError else StatusSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuppliersMarketplaceTab(
    companies: List<Company>,
    onRequestRFQ: (Company) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(companies, searchQuery) {
        if (searchQuery.isBlank()) companies else companies.filter { it.companyName.contains(searchQuery, true) || it.industry.contains(searchQuery, true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Verified Supplier Marketplace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Audit ISO certified suppliers, compare performance and request direct RFQs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("supplier_search_input"),
                placeholder = { Text("Search by industry, name, or materials...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered) { company ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("supplier_card_${company.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(company.companyName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (company.isVerified) {
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = LinkoraPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("${company.industry} • ${company.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                            Text("${company.rating} (${company.reviewCount})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Certifications
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        company.certifications.forEach { cert ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(cert, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("GST: ${company.gst}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { onRequestRFQ(company) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_rfq_${company.id}")
                        ) {
                            Text("Request Quotation (RFQ)")
                        }
                    }
                }
            }
        }
    }
}

// Dialogs
@Composable
private fun CreatePODialog(
    companies: List<Company>,
    onDismiss: () -> Unit,
    onCreate: (Company, String, Int, Double, String) -> Unit
) {
    var selectedCompany by remember { mutableStateOf(companies.firstOrNull()) }
    var itemDesc by remember { mutableStateOf("High-Tensile Cold Rolled Coils") }
    var qtyText by remember { mutableStateOf("15") }
    var rateText by remember { mutableStateOf("78000") }
    var dueDate by remember { mutableStateOf("25 Oct 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Purchase Order (PO)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Verified Vendor:", style = MaterialTheme.typography.labelMedium)
                companies.forEach { comp ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCompany = comp }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = selectedCompany?.id == comp.id, onClick = { selectedCompany = comp })
                        Spacer(Modifier.width(8.dp))
                        Text(comp.companyName, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedTextField(
                    value = itemDesc,
                    onValueChange = { itemDesc = it },
                    label = { Text("Line Item Description") },
                    modifier = Modifier.fillMaxWidth().testTag("po_item_desc")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("po_item_qty")
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("po_item_rate")
                    )
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Required Delivery Date") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val comp = selectedCompany ?: return@Button
                    val qty = qtyText.toIntOrNull() ?: 1
                    val rate = rateText.toDoubleOrNull() ?: 1000.0
                    onCreate(comp, itemDesc, qty, rate, dueDate)
                },
                modifier = Modifier.testTag("submit_create_po_btn")
            ) {
                Text("Issue PO")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, InventoryCategory, Int, Int, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(InventoryCategory.RAW_MATERIALS) }
    var stockText by remember { mutableStateOf("50") }
    var minText by remember { mutableStateOf("20") }
    var maxText by remember { mutableStateOf("200") }
    var unit by remember { mutableStateOf("Units") }
    var location by remember { mutableStateOf("Bay B-04") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Inventory SKU", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("SKU / Material Name") },
                    modifier = Modifier.fillMaxWidth().testTag("inv_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    InventoryCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = { Text(cat.label, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text("Min Safety") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it },
                        label = { Text("Max Cap") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Warehouse Location") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val stock = stockText.toIntOrNull() ?: 10
                        val min = minText.toIntOrNull() ?: 5
                        val max = maxText.toIntOrNull() ?: 100
                        onAdd(name, selectedCat, stock, min, max, unit, location)
                    }
                },
                modifier = Modifier.testTag("submit_add_inventory_btn")
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RFQDialog(
    supplier: Company,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, Double) -> Unit
) {
    var product by remember { mutableStateOf("Precision Forged Shafts") }
    var qtyText by remember { mutableStateOf("500") }
    var targetRateText by remember { mutableStateOf("1250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request for Quotation (RFQ)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Vendor: ${supplier.companyName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Target Product / Component") },
                    modifier = Modifier.fillMaxWidth().testTag("rfq_product_input")
                )
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Required Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetRateText,
                    onValueChange = { targetRateText = it },
                    label = { Text("Target Rate per Unit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toIntOrNull() ?: 100
                    val rate = targetRateText.toDoubleOrNull() ?: 500.0
                    onSubmit(product, qty, rate)
                },
                modifier = Modifier.testTag("submit_rfq_btn")
            ) {
                Text("Send RFQ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
