package com.example.ui.screens.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun BuyerDashboardScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val products by repository.products.collectAsState()
    val companies by repository.companies.collectAsState()
    val shipments by repository.shipments.collectAsState()
    val reviews by repository.reviews.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Discovery", "Comparison", "Bulk Orders", "Track Shipment", "Reviews")

    var showOrderCheckoutDialog by remember { mutableStateOf<Product?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().testTag("buyer_dashboard")) {
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
                    modifier = Modifier.testTag("buyer_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> BuyerDiscoveryTab(
                products = products,
                companies = companies,
                onOrderProduct = { prod -> showOrderCheckoutDialog = prod }
            )
            1 -> BuyerComparisonTab(products = products)
            2 -> BuyerBulkOrdersTab(
                products = products,
                onOrderProduct = { prod -> showOrderCheckoutDialog = prod }
            )
            3 -> BuyerShipmentTrackingTab(shipments = shipments)
            4 -> BuyerReviewsTab(
                reviews = reviews,
                companies = companies,
                onWriteReview = { showReviewDialog = true }
            )
        }
    }

    if (showOrderCheckoutDialog != null) {
        OrderCheckoutDialog(
            product = showOrderCheckoutDialog!!,
            companies = companies,
            onDismiss = { showOrderCheckoutDialog = null },
            onConfirmOrder = { prod, qty, supplierComp ->
                repository.createPurchaseOrder(
                    supplier = supplierComp,
                    items = listOf(OrderItem(prod.name, qty, prod.price)),
                    dueDate = "10 Days"
                )
                showOrderCheckoutDialog = null
            }
        )
    }

    if (showReviewDialog) {
        AddReviewDialog(
            companies = companies.filter { it.id.contains("sup") },
            onDismiss = { showReviewDialog = false },
            onSubmit = { supId, rating, comment ->
                repository.addReview(supId, rating, comment)
                showReviewDialog = false
            }
        )
    }
}

@Composable
private fun BuyerDiscoveryTab(
    products: List<Product>,
    companies: List<Company>,
    onOrderProduct: (Product) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filtered = remember(products, search, selectedCategory) {
        products.filter { prod ->
            val matchSearch = search.isBlank() || prod.name.contains(search, true) || prod.category.contains(search, true) || prod.supplierName.contains(search, true)
            val matchCat = selectedCategory == "All" || prod.category.equals(selectedCategory, true)
            matchSearch && matchCat
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("B2B Sourcing & Supplier Discovery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Search directly verified OEM manufacturers & Tier-1 material suppliers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search by component, material, alloy, or vendor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("buyer_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
            val categories = listOf("All", "Steel", "Electronics", "Polymers", "Manufacturing")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filtered) { prod ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("buyer_product_${prod.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                            Text("${prod.rating}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Supplier: ${prod.supplierName}", style = MaterialTheme.typography.bodySmall, color = LinkoraPrimary, fontWeight = FontWeight.SemiBold)
                    Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wholesale Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.0f", prod.price)} / ${prod.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        }

                        Button(
                            onClick = { onOrderProduct(prod) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("buy_product_btn_${prod.id}")
                        ) {
                            Text("Order / RFQ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerComparisonTab(products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Supplier Sourcing Comparison Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Compare prices, lead times, ratings, and MOQs side by side", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Top Ranked Raw Steel Options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    ComparisonRow(
                        vendor = "Prime Alloy Forgings",
                        material = "Cold-Rolled Sheet (CRCA)",
                        price = "₹78,000 / Ton",
                        leadDays = "4 Days",
                        rating = "4.9 ⭐",
                        moq = "5 Tons"
                    )
                    HorizontalDivider()
                    ComparisonRow(
                        vendor = "Jindal Precision Steel",
                        material = "Commercial Grade Cold Coil",
                        price = "₹81,500 / Ton",
                        leadDays = "7 Days",
                        rating = "4.7 ⭐",
                        moq = "10 Tons"
                    )
                    HorizontalDivider()
                    ComparisonRow(
                        vendor = "Tata Structurals SEZ",
                        material = "Hot-Dipped Galvanized Sheet",
                        price = "₹84,000 / Ton",
                        leadDays = "3 Days",
                        rating = "4.8 ⭐",
                        moq = "8 Tons"
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(vendor: String, material: String, price: String, leadDays: String, rating: String, moq: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(vendor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(price, fontWeight = FontWeight.Bold, color = LinkoraPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        Text(material, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lead: $leadDays", style = MaterialTheme.typography.labelSmall)
            Text("Rating: $rating", style = MaterialTheme.typography.labelSmall)
            Text("MOQ: $moq", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun BuyerBulkOrdersTab(
    products: List<Product>,
    onOrderProduct: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Bulk Order Sourcing & Quantity Discounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Access tiered container-load contract pricing with escrow protection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Standard Rate: ₹${String.format("%,.0f", prod.price)} | MOQ: ${prod.moq} ${prod.unit}", style = MaterialTheme.typography.bodySmall)

                    Surface(color = StatusSuccess.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "Bulk Incentive: 6% discount on orders exceeding 5x MOQ",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = { onOrderProduct(prod) }) {
                            Text("Place Bulk Order")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerShipmentTrackingTab(shipments: List<Shipment>) {
    var searchTrackingNumber by remember { mutableStateOf("") }
    val displayedShipment = remember(shipments, searchTrackingNumber) {
        if (searchTrackingNumber.isBlank()) shipments.firstOrNull() else shipments.find { it.trackingNumber.contains(searchTrackingNumber, true) } ?: shipments.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Real-Time Consignment Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Live GPS telemetry directly from transit fleet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = searchTrackingNumber,
                onValueChange = { searchTrackingNumber = it },
                label = { Text("Enter Tracking Number (e.g., LNK-TRK-98421)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("buyer_tracking_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (displayedShipment != null) {
            item {
                InteractiveLogisticsMap(
                    origin = displayedShipment.origin,
                    destination = displayedShipment.destination,
                    currentCheckpoint = displayedShipment.currentCheckpoint,
                    progress = displayedShipment.progressPercent,
                    routeType = displayedShipment.routeType
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Consignment Details: ${displayedShipment.trackingNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Carrier: ${displayedShipment.carrierName}", style = MaterialTheme.typography.bodySmall)
                        Text("Status: ${displayedShipment.status.label} • ETA: ${displayedShipment.estimatedArrival}", fontWeight = FontWeight.SemiBold, color = LinkoraPrimary)
                        Text("Associated PO: ${displayedShipment.poNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyerReviewsTab(
    reviews: List<SupplierReview>,
    companies: List<Company>,
    onWriteReview: () -> Unit
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
                    Text("Supplier Ratings & Quality Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Verified manufacturer peer feedback", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onWriteReview, modifier = Modifier.testTag("write_review_btn")) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Review")
                }
            }
        }

        items(reviews) { rev ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${rev.reviewerName} • ${rev.company}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Row {
                            repeat(rev.rating) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
                    Text(rev.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OrderCheckoutDialog(
    product: Product,
    companies: List<Company>,
    onDismiss: () -> Unit,
    onConfirmOrder: (Product, Int, Company) -> Unit
) {
    var qtyText by remember { mutableStateOf("${product.moq * 2}") }
    val supplierComp = remember(product, companies) {
        companies.find { it.id == product.supplierId } ?: companies.first { it.id.contains("sup") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout Purchase Order", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Product: ${product.name}", fontWeight = FontWeight.SemiBold)
                Text("Supplier: ${product.supplierName}")
                Text("Unit Rate: ₹${String.format("%,.0f", product.price)}")

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("checkout_qty_input")
                )

                val qty = qtyText.toIntOrNull() ?: 1
                val total = qty * product.price
                Text("Estimated Total: ₹${String.format("%,.0f", total)}", fontWeight = FontWeight.Bold, color = LinkoraPrimary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toIntOrNull() ?: product.moq
                    onConfirmOrder(product, qty, supplierComp)
                },
                modifier = Modifier.testTag("confirm_order_checkout")
            ) {
                Text("Place Order")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddReviewDialog(
    companies: List<Company>,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, String) -> Unit
) {
    var selectedComp by remember { mutableStateOf(companies.firstOrNull()) }
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("Excellent delivery reliability and defect-free raw materials.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Supplier Review", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Vendor:")
                companies.forEach { comp ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedComp = comp }.padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = selectedComp?.id == comp.id, onClick = { selectedComp = comp })
                        Spacer(Modifier.width(8.dp))
                        Text(comp.companyName, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text("Rating: $rating / 5 Stars")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= rating) StatusWarning else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Quality and SLA Feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = selectedComp ?: return@Button
                    onSubmit(c.id, rating, comment)
                }
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
