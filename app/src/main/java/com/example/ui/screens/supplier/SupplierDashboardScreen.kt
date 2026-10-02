package com.example.ui.screens.supplier

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.models.*
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDashboardScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val products by repository.products.collectAsState()
    val purchaseOrders by repository.purchaseOrders.collectAsState()
    val quotations by repository.quotations.collectAsState()
    val invoices by repository.invoices.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Products", "Quotes (RFQ)", "Inbound Orders", "Invoices", "Profile")

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showGenerateInvoiceDialog by remember { mutableStateOf(false) }
    var invoiceTargetPO by remember { mutableStateOf<PurchaseOrder?>(null) }
    var showInvoiceDetail by remember { mutableStateOf<Invoice?>(null) }

    Column(modifier = modifier.fillMaxSize().testTag("supplier_dashboard")) {
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
                    modifier = Modifier.testTag("supplier_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> SupplierOverviewTab(
                products = products,
                purchaseOrders = purchaseOrders,
                quotations = quotations,
                invoices = invoices
            )
            1 -> SupplierCatalogueTab(
                products = products,
                onAddProduct = { showAddProductDialog = true },
                onDeleteProduct = { id -> repository.deleteProduct(id) }
            )
            2 -> SupplierQuotationsTab(
                quotations = quotations,
                onAcceptQuote = { id -> repository.updateQuotationStatus(id, "Accepted") },
                onRejectQuote = { id -> repository.updateQuotationStatus(id, "Rejected") }
            )
            3 -> SupplierOrdersTab(
                purchaseOrders = purchaseOrders,
                onAcceptOrder = { id -> repository.updatePOStatus(id, POStatus.APPROVED) },
                onRejectOrder = { id -> repository.updatePOStatus(id, POStatus.REJECTED) },
                onDispatchOrder = { id -> repository.updatePOStatus(id, POStatus.SHIPPED) },
                onGenerateInvoice = { po ->
                    invoiceTargetPO = po
                    showGenerateInvoiceDialog = true
                }
            )
            4 -> SupplierInvoicesTab(
                invoices = invoices,
                onViewInvoice = { inv -> showInvoiceDetail = inv },
                onMarkPaid = { id -> repository.markInvoicePaid(id) }
            )
            5 -> SupplierProfileTab(currentUser = currentUser)
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onAdd = { name, cat, price, moq, stock, lead, desc ->
                repository.addProduct(name, cat, price, moq, stock, lead, desc)
                showAddProductDialog = false
            }
        )
    }

    if (showGenerateInvoiceDialog && invoiceTargetPO != null) {
        GenerateInvoiceDialog(
            po = invoiceTargetPO!!,
            onDismiss = { showGenerateInvoiceDialog = false },
            onGenerate = { subtotal ->
                repository.generateInvoice(
                    poNumber = invoiceTargetPO!!.poNumber,
                    buyerName = invoiceTargetPO!!.manufacturerName,
                    subtotal = subtotal
                )
                showGenerateInvoiceDialog = false
            }
        )
    }

    if (showInvoiceDetail != null) {
        InvoiceDetailDialog(
            invoice = showInvoiceDetail!!,
            onDismiss = { showInvoiceDetail = null },
            onMarkPaid = {
                repository.markInvoicePaid(showInvoiceDetail!!.id)
                showInvoiceDetail = null
            }
        )
    }
}

@Composable
private fun SupplierOverviewTab(
    products: List<Product>,
    purchaseOrders: List<PurchaseOrder>,
    quotations: List<Quotation>,
    invoices: List<Invoice>
) {
    val pendingOrders = purchaseOrders.count { it.status == POStatus.PENDING || it.status == POStatus.APPROVED }
    val uncollectedRevenue = invoices.filter { !it.isPaid }.sumOf { it.totalAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Pending Fulfillment",
                    value = "$pendingOrders Orders",
                    subtitle = "Action required",
                    icon = Icons.Default.PendingActions,
                    accentColor = LinkoraPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Catalogue SKUs",
                    value = "${products.size} Active",
                    subtitle = "B2B Marketplace",
                    icon = Icons.Default.Category,
                    accentColor = LinkoraAccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Pending Invoices",
                    value = "₹${String.format("%,.0f", uncollectedRevenue)}",
                    subtitle = "Unpaid Receivables",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "RFQs Received",
                    value = "${quotations.size} Inquiries",
                    subtitle = "Active Bids",
                    icon = Icons.Default.QuestionAnswer,
                    accentColor = LinkoraAIIndigo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Supplier Performance Metrics Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("supplier_performance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Supplier SLA & Reliability Scorecard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    MetricBar(label = "On-Time Dispatch Rate", percent = 0.98f, display = "98.4%")
                    MetricBar(label = "Quality Compliance Index", percent = 0.99f, display = "99.1%")
                    MetricBar(label = "Quotation Response Time", percent = 0.92f, display = "Avg 2.4 Hours")
                }
            }
        }

        item {
            SectionHeader(title = "Recent Inbound Purchase Orders", subtitle = "Direct orders from manufacturers and buyers")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                purchaseOrders.take(3).forEach { po ->
                    POCard(po = po, onApprove = null, onReject = null, onMarkDelivered = null)
                }
            }
        }
    }
}

@Composable
private fun SupplierCatalogueTab(
    products: List<Product>,
    onAddProduct: () -> Unit,
    onDeleteProduct: (String) -> Unit
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
                    Text("Product Catalogue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Publish B2B listings with pricing, MOQ, and lead times", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = onAddProduct, shape = RoundedCornerShape(10.dp), modifier = Modifier.testTag("add_product_btn")) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("List SKU")
                }
            }
        }

        items(products) { prod ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("product_item_${prod.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${prod.category} • MOQ: ${prod.moq} ${prod.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteProduct(prod.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusError, modifier = Modifier.size(20.dp))
                        }
                    }

                    if (prod.description.isNotBlank()) {
                        Text(prod.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wholesale Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.0f", prod.price)} / ${prod.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Lead Time: ${prod.leadTimeDays} Days", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierQuotationsTab(
    quotations: List<Quotation>,
    onAcceptQuote: (String) -> Unit,
    onRejectQuote: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Quotation & RFQ Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Negotiate terms and confirm orders with buyers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(quotations) { q ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("quotation_card_${q.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(q.rfqNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(q.status, if (q.status == "Accepted") "success" else "warning")
                    }

                    Text("Buyer: ${q.buyerName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Product: ${q.quantity} units of ${q.productName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quote: ₹${String.format("%,.0f", q.offeredPrice)} / unit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        Text("Lead Time: ${q.leadTimeDays} days", style = MaterialTheme.typography.bodySmall)
                    }

                    if (q.status == "Submitted") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(onClick = { onRejectQuote(q.id) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                Text("Decline", color = StatusError)
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { onAcceptQuote(q.id) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                Text("Accept Bid")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierOrdersTab(
    purchaseOrders: List<PurchaseOrder>,
    onAcceptOrder: (String) -> Unit,
    onRejectOrder: (String) -> Unit,
    onDispatchOrder: (String) -> Unit,
    onGenerateInvoice: (PurchaseOrder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Inbound Purchase Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Confirm customer orders and transition to dispatch", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(purchaseOrders) { po ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("supplier_order_${po.poNumber}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${po.poNumber} • ${po.manufacturerName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(po.status.label, po.status.name)
                    }

                    Text("Amount: ₹${String.format("%,.0f", po.amount)} | Delivery Due: ${po.deliveryDueDate}", style = MaterialTheme.typography.bodySmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onGenerateInvoice(po) }, modifier = Modifier.testTag("gen_inv_btn_${po.poNumber}")) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Create GST Invoice")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (po.status == POStatus.PENDING) {
                                Button(onClick = { onAcceptOrder(po.id) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                    Text("Accept Order")
                                }
                            } else if (po.status == POStatus.APPROVED) {
                                Button(
                                    onClick = { onDispatchOrder(po.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LinkoraAccentTeal),
                                    modifier = Modifier.height(36.dp).testTag("dispatch_po_${po.poNumber}")
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Dispatch")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierInvoicesTab(
    invoices: List<Invoice>,
    onViewInvoice: (Invoice) -> Unit,
    onMarkPaid: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Tax Invoices & Receivables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Track payment reconciliation and GST compliance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(invoices) { inv ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("invoice_card_${inv.invoiceNumber}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(inv.invoiceNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        StatusBadge(if (inv.isPaid) "PAID" else "PENDING", if (inv.isPaid) "success" else "warning")
                    }

                    Text("Billed to: ${inv.buyerName} • Ref: ${inv.poNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total (inc. 18% GST)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format("%,.0f", inv.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = { onViewInvoice(inv) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                Text("View PDF")
                            }
                            if (!inv.isPaid) {
                                Button(onClick = { onMarkPaid(inv.id) }, shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                                    Text("Mark Paid")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierProfileTab(currentUser: User) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("supplier_profile_card")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Supplier Profile & GST Compliance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ProfileItem(label = "Company Name", value = currentUser.companyName)
                    ProfileItem(label = "Authorized Representative", value = "${currentUser.name} (${currentUser.designation})")
                    ProfileItem(label = "Corporate Email", value = currentUser.email)
                    ProfileItem(label = "GSTIN", value = "27AAACP0123M1Z8 (Verified Active)")
                    ProfileItem(label = "Industry Classification", value = "Alloy Forging & Precision Metallurgy")
                    ProfileItem(label = "Certifications", value = "ISO 9001:2015, IATF 16949, AS9100D Aerospace")
                    ProfileItem(label = "Compliance Status", value = "Tier-1 AEO-T2 Verified Vendor")
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Int, Int, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Steel") }
    var priceText by remember { mutableStateOf("85000") }
    var moqText by remember { mutableStateOf("5") }
    var stockText by remember { mutableStateOf("300") }
    var leadText by remember { mutableStateOf("4") }
    var desc by remember { mutableStateOf("Precision industrial grade product with certified metallurgy.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List SKU in Catalogue", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth().testTag("add_product_name"))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = moqText, onValueChange = { moqText = it }, label = { Text("MOQ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = stockText, onValueChange = { stockText = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = leadText, onValueChange = { leadText = it }, label = { Text("Lead (Days)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Specifications") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val p = priceText.toDoubleOrNull() ?: 1000.0
                        val m = moqText.toIntOrNull() ?: 1
                        val s = stockText.toIntOrNull() ?: 50
                        val l = leadText.toIntOrNull() ?: 3
                        onAdd(name, category, p, m, s, l, desc)
                    }
                },
                modifier = Modifier.testTag("submit_add_product")
            ) { Text("Publish SKU") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GenerateInvoiceDialog(
    po: PurchaseOrder,
    onDismiss: () -> Unit,
    onGenerate: (Double) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate GST Tax Invoice", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PO Number: ${po.poNumber}")
                Text("Billed To: ${po.manufacturerName}")
                Text("Subtotal Amount: ₹${String.format("%,.0f", po.amount)}")
                Text("Applicable GST (18%): ₹${String.format("%,.0f", po.amount * 0.18)}")
                Text("Total Invoice Value: ₹${String.format("%,.0f", po.amount * 1.18)}", fontWeight = FontWeight.Bold, color = LinkoraPrimary)
            }
        },
        confirmButton = {
            Button(onClick = { onGenerate(po.amount) }, modifier = Modifier.testTag("confirm_gen_invoice")) {
                Text("Create Invoice")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun InvoiceDetailDialog(
    invoice: Invoice,
    onDismiss: () -> Unit,
    onMarkPaid: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tax Invoice: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Supplier: ${invoice.supplierName}")
                Text("Buyer: ${invoice.buyerName}")
                Text("Purchase Order Ref: ${invoice.poNumber}")
                HorizontalDivider()
                Text("Subtotal: ₹${String.format("%,.0f", invoice.subtotal)}")
                Text("CGST (9%) + SGST (9%): ₹${String.format("%,.0f", invoice.gstAmount)}")
                Text("Total Payable: ₹${String.format("%,.0f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                Text("Payment Status: ${if (invoice.isPaid) "PAID" else "PENDING NET 30"}", color = if (invoice.isPaid) StatusSuccess else StatusWarning, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            if (!invoice.isPaid) {
                Button(onClick = onMarkPaid) { Text("Reconcile Payment") }
            } else {
                Button(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
