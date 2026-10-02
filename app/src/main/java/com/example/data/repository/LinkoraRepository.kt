package com.example.data.repository

import com.example.data.ai.LinkoraAIEngine
import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class LinkoraRepository {

    private val usersByRole = mapOf(
        UserRole.MANUFACTURER to User("usr_mfg_1", "Vikram Malhotra", "v.malhotra@apexforge.com", UserRole.MANUFACTURER, "Apex Industrial Systems", "comp_mfg_1", "VP of Operations"),
        UserRole.SUPPLIER to User("usr_sup_1", "Rajesh Singhania", "singhania@primealloys.in", UserRole.SUPPLIER, "Prime Alloy Forgings Ltd", "comp_sup_1", "Managing Director"),
        UserRole.LOGISTICS to User("usr_log_1", "Col. Amit Deshmukh", "amit.deshmukh@velocityfreight.com", UserRole.LOGISTICS, "Velocity Freight & Logistics", "comp_log_1", "Chief Fleet Controller"),
        UserRole.BUYER to User("usr_buy_1", "Ananya Sen", "ananya.sen@nexustech.io", UserRole.BUYER, "Nexus Mobility & Hardware", "comp_buy_1", "Global Procurement Head"),
        UserRole.ADMIN to User("usr_adm_1", "Siddharth Verma", "siddharth@linkora.ai", UserRole.ADMIN, "Linkora Global HQ", "comp_admin_1", "Platform Architect")
    )

    private val _currentUser = MutableStateFlow(usersByRole[UserRole.MANUFACTURER]!!)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _companies = MutableStateFlow(createSeedCompanies())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val _products = MutableStateFlow(createSeedProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _inventory = MutableStateFlow(createSeedInventory())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _purchaseOrders = MutableStateFlow(createSeedPurchaseOrders())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    private val _shipments = MutableStateFlow(createSeedShipments())
    val shipments: StateFlow<List<Shipment>> = _shipments.asStateFlow()

    private val _quotations = MutableStateFlow(createSeedQuotations())
    val quotations: StateFlow<List<Quotation>> = _quotations.asStateFlow()

    private val _invoices = MutableStateFlow(createSeedInvoices())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _aiPredictions = MutableStateFlow(LinkoraAIEngine.generateInitialPredictions())
    val aiPredictions: StateFlow<List<AIPrediction>> = _aiPredictions.asStateFlow()

    private val _productionPlans = MutableStateFlow(createSeedProductionPlans())
    val productionPlans: StateFlow<List<ProductionPlan>> = _productionPlans.asStateFlow()

    private val _qualityReports = MutableStateFlow(createSeedQualityReports())
    val qualityReports: StateFlow<List<QualityReport>> = _qualityReports.asStateFlow()

    private val _reviews = MutableStateFlow(createSeedReviews())
    val reviews: StateFlow<List<SupplierReview>> = _reviews.asStateFlow()

    private val _fraudAlerts = MutableStateFlow(createSeedFraudAlerts())
    val fraudAlerts: StateFlow<List<FraudAlert>> = _fraudAlerts.asStateFlow()

    private val _auditLogs = MutableStateFlow(createSeedAuditLogs())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _currentPlan = MutableStateFlow(
        SubscriptionPlan(
            id = "growth",
            name = "Growth Plan",
            priceMonthly = 4999,
            priceFormatted = "₹4,999/mo",
            userLimit = "Up to 50 Users",
            orderLimit = "Unlimited Orders/Month",
            features = listOf("Unlimited Orders", "AI Demand Forecasting", "Supplier Marketplace", "Logistics Tracking", "Smart Inventory", "Priority Telemetry"),
            isPopular = true
        )
    )
    val currentPlan: StateFlow<SubscriptionPlan> = _currentPlan.asStateFlow()

    // Platform settings
    private val _commissionRate = MutableStateFlow(2.0) // 2%
    val commissionRate: StateFlow<Double> = _commissionRate.asStateFlow()

    fun switchRole(role: UserRole) {
        val user = usersByRole[role] ?: return
        _currentUser.value = user
        addAuditLog("ROLE_SWITCH", "User switched active portal role to ${role.label}", role.label)
    }

    fun setCommissionRate(rate: Double) {
        _commissionRate.value = rate
        addAuditLog("CONFIG_UPDATE", "Marketplace commission rate updated to $rate%", "Admin")
    }

    // Inventory CRUD
    fun addInventoryItem(name: String, category: InventoryCategory, stock: Int, minThresh: Int, maxThresh: Int, unit: String, location: String) {
        val newItem = InventoryItem(
            id = "inv_${UUID.randomUUID().toString().take(6)}",
            companyId = _currentUser.value.companyId,
            name = name,
            category = category,
            stock = stock,
            minThreshold = minThresh,
            maxThreshold = maxThresh,
            unit = unit,
            warehouseLocation = location,
            lastUpdated = "Just now"
        )
        _inventory.value = listOf(newItem) + _inventory.value
        addAuditLog("INVENTORY_ADD", "Added $stock $unit of $name to $location", _currentUser.value.name)
    }

    fun updateInventoryStock(itemId: String, delta: Int) {
        _inventory.value = _inventory.value.map { item ->
            if (item.id == itemId) {
                val newStock = (item.stock + delta).coerceAtLeast(0)
                item.copy(stock = newStock, lastUpdated = "Just now")
            } else item
        }
    }

    fun deleteInventoryItem(itemId: String) {
        val item = _inventory.value.find { it.id == itemId }
        _inventory.value = _inventory.value.filterNot { it.id == itemId }
        if (item != null) {
            addAuditLog("INVENTORY_DELETE", "Removed inventory item ${item.name}", _currentUser.value.name)
        }
    }

    // Purchase Order CRUD & Status Flow
    fun createPurchaseOrder(supplier: Company, items: List<OrderItem>, dueDate: String): PurchaseOrder {
        val totalAmount = items.sumOf { it.total }
        val po = PurchaseOrder(
            id = "po_${UUID.randomUUID().toString().take(6)}",
            poNumber = "PO-${(9200..9999).random()}",
            manufacturerId = _currentUser.value.companyId,
            manufacturerName = _currentUser.value.companyName,
            supplierId = supplier.id,
            supplierName = supplier.companyName,
            amount = totalAmount,
            status = POStatus.PENDING,
            items = items,
            createdAt = "Today",
            deliveryDueDate = dueDate
        )
        _purchaseOrders.value = listOf(po) + _purchaseOrders.value
        addAuditLog("PO_CREATE", "Generated ${po.poNumber} for ${supplier.companyName} (₹${String.format("%,.0f", totalAmount)})", _currentUser.value.name)
        return po
    }

    fun updatePOStatus(poId: String, newStatus: POStatus) {
        _purchaseOrders.value = _purchaseOrders.value.map { po ->
            if (po.id == poId) {
                if (newStatus == POStatus.SHIPPED && po.status != POStatus.SHIPPED) {
                    createShipmentForPO(po)
                }
                po.copy(status = newStatus)
            } else po
        }
        addAuditLog("PO_STATUS", "PO $poId updated to ${newStatus.label}", _currentUser.value.name)
    }

    private fun createShipmentForPO(po: PurchaseOrder) {
        val newShipment = Shipment(
            id = "shp_${UUID.randomUUID().toString().take(6)}",
            trackingNumber = "LNK-TRK-${(80000..99999).random()}",
            poNumber = po.poNumber,
            carrierName = "Velocity Express Freight",
            origin = "Pune Manufacturing Hub",
            destination = "Sanand Auto Corridor, Gujarat",
            status = ShipmentStatus.IN_TRANSIT,
            estimatedArrival = "In 2 Days",
            routeType = "AI Optimized Expressway",
            distanceKm = 640.0,
            costEstimated = 18500.0,
            currentCheckpoint = "Thane Toll Plaza Gate 4",
            progressPercent = 0.25f,
            latitude = 19.2183,
            longitude = 72.9781
        )
        _shipments.value = listOf(newShipment) + _shipments.value
    }

    // Logistics Actions
    fun acceptShipment(shipmentId: String) {
        _shipments.value = _shipments.value.map { shp ->
            if (shp.id == shipmentId) {
                shp.copy(
                    status = ShipmentStatus.IN_TRANSIT,
                    carrierName = _currentUser.value.companyName,
                    progressPercent = 0.35f
                )
            } else shp
        }
        addAuditLog("SHIPMENT_ACCEPT", "Assigned shipment $shipmentId to ${_currentUser.value.companyName}", _currentUser.value.name)
    }

    fun updateShipmentProgress(shipmentId: String, newProgress: Float, checkpoint: String, status: ShipmentStatus) {
        _shipments.value = _shipments.value.map { shp ->
            if (shp.id == shipmentId) {
                shp.copy(
                    progressPercent = newProgress,
                    currentCheckpoint = checkpoint,
                    status = status
                )
            } else shp
        }
    }

    // Quotations / RFQs
    fun submitQuotation(rfqNumber: String, supplierName: String, buyerName: String, product: String, qty: Int, price: Double, leadDays: Int) {
        val quote = Quotation(
            id = "q_${UUID.randomUUID().toString().take(6)}",
            rfqNumber = rfqNumber,
            supplierId = _currentUser.value.companyId,
            supplierName = supplierName,
            buyerName = buyerName,
            productName = product,
            quantity = qty,
            offeredPrice = price,
            leadTimeDays = leadDays,
            status = "Submitted",
            date = "Today"
        )
        _quotations.value = listOf(quote) + _quotations.value
        addAuditLog("QUOTE_SUBMIT", "Offered ₹$price/unit for $qty of $product to $buyerName", _currentUser.value.name)
    }

    fun updateQuotationStatus(quoteId: String, status: String) {
        _quotations.value = _quotations.value.map { q ->
            if (q.id == quoteId) q.copy(status = status) else q
        }
    }

    // Products CRUD for Suppliers
    fun addProduct(name: String, category: String, price: Double, moq: Int, stock: Int, leadDays: Int, description: String) {
        val newProd = Product(
            id = "p_${UUID.randomUUID().toString().take(6)}",
            supplierId = _currentUser.value.companyId,
            supplierName = _currentUser.value.companyName,
            name = name,
            category = category,
            price = price,
            moq = moq,
            stockQuantity = stock,
            leadTimeDays = leadDays,
            description = description
        )
        _products.value = listOf(newProd) + _products.value
        addAuditLog("PRODUCT_ADD", "Listed new SKU $name in $category catalogue", _currentUser.value.name)
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    // Invoices
    fun generateInvoice(poNumber: String, buyerName: String, subtotal: Double) {
        val inv = Invoice(
            id = "inv_${UUID.randomUUID().toString().take(6)}",
            invoiceNumber = "INV-2026-${(1000..9999).random()}",
            poNumber = poNumber,
            supplierName = _currentUser.value.companyName,
            buyerName = buyerName,
            subtotal = subtotal,
            isPaid = false,
            dueDate = "Net 30 Days",
            issuedDate = "Today"
        )
        _invoices.value = listOf(inv) + _invoices.value
        addAuditLog("INVOICE_GENERATE", "Created tax invoice ${inv.invoiceNumber} for ₹${String.format("%,.0f", inv.totalAmount)}", _currentUser.value.name)
    }

    fun markInvoicePaid(invoiceId: String) {
        _invoices.value = _invoices.value.map { inv ->
            if (inv.id == invoiceId) inv.copy(isPaid = true) else inv
        }
        addAuditLog("PAYMENT_SUCCESS", "Invoice $invoiceId settled via Razorpay Auto-Clear", _currentUser.value.name)
    }

    // AI Predictions
    fun triggerCustomAIPrediction(commodity: String, horizon: String, marketSector: String): AIPrediction {
        val prediction = LinkoraAIEngine.analyzeCustomDemand(commodity, horizon, marketSector)
        _aiPredictions.value = listOf(prediction) + _aiPredictions.value
        addAuditLog("AI_PREDICTION", "Executed neural demand forecast for $commodity", _currentUser.value.name)
        return prediction
    }

    // Reviews
    fun addReview(supplierId: String, rating: Int, comment: String) {
        val rev = SupplierReview(
            id = "rev_${UUID.randomUUID().toString().take(6)}",
            supplierId = supplierId,
            reviewerName = _currentUser.value.name,
            company = _currentUser.value.companyName,
            rating = rating,
            comment = comment,
            date = "Today"
        )
        _reviews.value = listOf(rev) + _reviews.value
    }

    // Subscription Billing
    fun updatePlan(newPlan: SubscriptionPlan) {
        _currentPlan.value = newPlan
        addAuditLog("PLAN_UPGRADE", "Account upgraded to ${newPlan.name} (${newPlan.priceFormatted})", _currentUser.value.name)
    }

    // Audit Log
    fun addAuditLog(action: String, details: String, user: String, role: String = _currentUser.value.role.label) {
        val log = AuditLog(
            id = "log_${UUID.randomUUID().toString().take(6)}",
            action = action,
            performedBy = user,
            role = role,
            timestamp = "Just now",
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value.take(40)
    }

    // Seed Data Factories
    private fun createSeedCompanies(): List<Company> = listOf(
        Company("comp_sup_1", "Prime Alloy Forgings Ltd", "Steel & Metallurgy", "27AAACP0123M1Z8", "Pune, Maharashtra", 4.9, 84, true, listOf("ISO 9001", "AS9100D", "IATF 16949")),
        Company("comp_mfg_1", "Apex Industrial Systems", "Manufacturing & Heavy Ops", "24AABCA9876C1Z3", "Vadodara, Gujarat", 4.8, 62, true, listOf("ISO 14001", "CE Certified")),
        Company("comp_log_1", "Velocity Freight & Logistics", "Logistics & Fleet", "29AADCV4567L1Z2", "Navi Mumbai, Maharashtra", 4.7, 115, true, listOf("AEO-T1", "ISO 28000")),
        Company("comp_buy_1", "Nexus Mobility & Hardware", "Automotive & EV", "07AAACN5544B1Z9", "Gurugram, NCR", 4.9, 47, true, listOf("IATF 16949", "RoHS")),
        Company("comp_sup_2", "ElectroSilicon Components", "Electronics & Semi", "33AABBE1122D1ZP", "Bengaluru, Karnataka", 4.6, 93, true, listOf("ISO 9001", "IPC-A-610")),
        Company("comp_sup_3", "Vardhman Polymer & Composites", "Textiles & Polymers", "03AABCV7788P1ZR", "Ludhiana, Punjab", 4.5, 38, true, listOf("OEKO-TEX", "ISO 9001"))
    )

    private fun createSeedProducts(): List<Product> = listOf(
        Product("p_1", "comp_sup_1", "Prime Alloy Forgings Ltd", "Cold-Rolled Coil Sheet (CRCA Grade D)", "Steel", 78000.0, 5, 450, 4, "Tons", 4.9, "Automotive grade deep-drawing steel coils with anti-rust oil passivated coat."),
        Product("p_2", "comp_sup_1", "Prime Alloy Forgings Ltd", "Alloy Steel Round Bar (EN19/4140)", "Steel", 92500.0, 3, 280, 5, "Tons", 4.8, "Hardened and tempered alloy steel bars for shafts, gears, and structural tooling."),
        Product("p_3", "comp_sup_2", "ElectroSilicon Components", "Industrial Microcontroller MCU-32 ARM", "Electronics", 420.0, 250, 15000, 7, "Units", 4.9, "32-bit Cortex-M4 microcontroller with dual CAN FD and integrated crypto engine."),
        Product("p_4", "comp_sup_2", "ElectroSilicon Components", "Automotive Grade Solid-State Relay 60A", "Electronics", 890.0, 100, 3400, 3, "Units", 4.7, "Opto-isolated fast switching relay certified for high-vibration engine bay compartments."),
        Product("p_5", "comp_sup_3", "Vardhman Polymer & Composites", "High-Density Polyethylene Granules (HDPE)", "Polymers", 112000.0, 2, 600, 6, "Tons", 4.6, "Blow molding grade resin with optimal environmental stress crack resistance (ESCR)."),
        Product("p_6", "comp_sup_1", "Prime Alloy Forgings Ltd", "Precision CNC Forged Flanges ANSI 150#", "Manufacturing", 3800.0, 25, 1200, 4, "Pieces", 4.9, "Forged carbon steel weld neck flanges with 100% ultrasonic defect verification.")
    )

    private fun createSeedInventory(): List<InventoryItem> = listOf(
        InventoryItem("inv_1", "comp_mfg_1", "CRCA Cold-Rolled Steel Sheet", InventoryCategory.RAW_MATERIALS, 18, 25, 100, "Tons", "Warehouse Bay A-12", "10m ago"),
        InventoryItem("inv_2", "comp_mfg_1", "Aluminum Billet 6061-T6", InventoryCategory.RAW_MATERIALS, 8, 20, 80, "Tons", "Warehouse Bay A-14", "1h ago"),
        InventoryItem("inv_3", "comp_mfg_1", "Automotive Steering Knuckle Castings", InventoryCategory.FINISHED_GOODS, 420, 100, 800, "Pieces", "Assembly Bay F-02", "30m ago"),
        InventoryItem("inv_4", "comp_mfg_1", "Hydraulic Cylinder Actuators", InventoryCategory.FINISHED_GOODS, 65, 30, 200, "Units", "Assembly Bay F-05", "2h ago"),
        InventoryItem("inv_5", "comp_mfg_1", "M10 High-Tensile Flange Bolts", InventoryCategory.WAREHOUSE_STOCK, 4500, 1500, 10000, "Pieces", "Hardware Bin C-09", "Yesterday"),
        InventoryItem("inv_6", "comp_mfg_1", "Synthetic Cutting Fluid Coolant", InventoryCategory.WAREHOUSE_STOCK, 120, 150, 600, "Liters", "Chemical Drum Rack D", "3h ago")
    )

    private fun createSeedPurchaseOrders(): List<PurchaseOrder> = listOf(
        PurchaseOrder(
            id = "po_101",
            poNumber = "PO-9842",
            manufacturerId = "comp_mfg_1",
            manufacturerName = "Apex Industrial Systems",
            supplierId = "comp_sup_1",
            supplierName = "Prime Alloy Forgings Ltd",
            amount = 780000.0,
            status = POStatus.SHIPPED,
            items = listOf(OrderItem("Cold-Rolled Coil Sheet", 10, 78000.0)),
            createdAt = "12 Oct 2026",
            deliveryDueDate = "18 Oct 2026"
        ),
        PurchaseOrder(
            id = "po_102",
            poNumber = "PO-9843",
            manufacturerId = "comp_mfg_1",
            manufacturerName = "Apex Industrial Systems",
            supplierId = "comp_sup_2",
            supplierName = "ElectroSilicon Components",
            amount = 210000.0,
            status = POStatus.APPROVED,
            items = listOf(OrderItem("Industrial Microcontroller MCU-32 ARM", 500, 420.0)),
            createdAt = "14 Oct 2026",
            deliveryDueDate = "24 Oct 2026"
        ),
        PurchaseOrder(
            id = "po_103",
            poNumber = "PO-9844",
            manufacturerId = "comp_mfg_1",
            manufacturerName = "Apex Industrial Systems",
            supplierId = "comp_sup_3",
            supplierName = "Vardhman Polymer & Composites",
            amount = 448000.0,
            status = POStatus.PENDING,
            items = listOf(OrderItem("High-Density Polyethylene Granules", 4, 112000.0)),
            createdAt = "15 Oct 2026",
            deliveryDueDate = "29 Oct 2026"
        ),
        PurchaseOrder(
            id = "po_104",
            poNumber = "PO-9830",
            manufacturerId = "comp_mfg_1",
            manufacturerName = "Apex Industrial Systems",
            supplierId = "comp_sup_1",
            supplierName = "Prime Alloy Forgings Ltd",
            amount = 380000.0,
            status = POStatus.DELIVERED,
            items = listOf(OrderItem("Precision CNC Forged Flanges", 100, 3800.0)),
            createdAt = "02 Oct 2026",
            deliveryDueDate = "09 Oct 2026"
        )
    )

    private fun createSeedShipments(): List<Shipment> = listOf(
        Shipment(
            id = "shp_1",
            trackingNumber = "LNK-TRK-98421",
            poNumber = "PO-9842",
            carrierName = "Velocity Express Freight",
            origin = "Prime Forge Plant, Pune",
            destination = "Apex Facility, Vadodara",
            status = ShipmentStatus.IN_TRANSIT,
            estimatedArrival = "Tomorrow, 14:30 IST",
            routeType = "AI Fastest Route (Via NH-48 Bypass)",
            distanceKm = 520.0,
            costEstimated = 14200.0,
            currentCheckpoint = "Bharuch Narmada Bridge Toll (380 km covered)",
            progressPercent = 0.72f,
            latitude = 21.7051,
            longitude = 72.9959
        ),
        Shipment(
            id = "shp_2",
            trackingNumber = "LNK-TRK-98305",
            poNumber = "PO-9830",
            carrierName = "Velocity Express Freight",
            origin = "Bengaluru Tech Park",
            destination = "Apex Facility, Vadodara",
            status = ShipmentStatus.DELIVERED,
            estimatedArrival = "Delivered on 09 Oct",
            routeType = "Lowest Cost Multi-Modal",
            distanceKm = 1240.0,
            costEstimated = 32800.0,
            currentCheckpoint = "Inbound Receiving Dock 2 - Signed Off",
            progressPercent = 1.0f,
            latitude = 22.3072,
            longitude = 73.1812
        ),
        Shipment(
            id = "shp_3",
            trackingNumber = "LNK-TRK-77192",
            poNumber = "PO-9850",
            carrierName = "Direct Haul Transports",
            origin = "Ludhiana Industrial Area",
            destination = "Nexus Mobility, Gurugram",
            status = ShipmentStatus.DELAYED,
            estimatedArrival = "ETA Revised: +6 Hours",
            routeType = "Express Freight",
            distanceKm = 310.0,
            costEstimated = 9500.0,
            currentCheckpoint = "Panipat Junction (Dense Fog Slowdown)",
            progressPercent = 0.48f,
            latitude = 29.3909,
            longitude = 76.9635
        )
    )

    private fun createSeedQuotations(): List<Quotation> = listOf(
        Quotation("q_1", "RFQ-4102", "comp_sup_1", "Prime Alloy Forgings Ltd", "Apex Industrial Systems", "Cold-Rolled Coil Sheet Grade D", 20, 76500.0, 3, "Submitted", "14 Oct 2026"),
        Quotation("q_2", "RFQ-4099", "comp_sup_2", "ElectroSilicon Components", "Apex Industrial Systems", "ARM MCU-32 Microcontrollers", 1000, 395.0, 5, "Accepted", "12 Oct 2026"),
        Quotation("q_3", "RFQ-4085", "comp_sup_3", "Vardhman Polymer & Composites", "Nexus Mobility", "Automotive Polymer Compound TPE", 15, 98000.0, 7, "Negotiating", "11 Oct 2026")
    )

    private fun createSeedInvoices(): List<Invoice> = listOf(
        Invoice("inv_1", "INV-2026-9041", "PO-9842", "Prime Alloy Forgings Ltd", "Apex Industrial Systems", 780000.0, 0.18, false, "18 Nov 2026", "14 Oct 2026"),
        Invoice("inv_2", "INV-2026-8910", "PO-9830", "Prime Alloy Forgings Ltd", "Apex Industrial Systems", 380000.0, 0.18, true, "09 Nov 2026", "02 Oct 2026"),
        Invoice("inv_3", "INV-2026-8802", "PO-9799", "ElectroSilicon Components", "Nexus Mobility", 195000.0, 0.18, true, "25 Oct 2026", "25 Sep 2026")
    )

    private fun createSeedProductionPlans(): List<ProductionPlan> = listOf(
        ProductionPlan("pp_1", "Batch 44-A: EV Subframe Chassis", "Chassis Welding Line 2", 500, 340, "In Progress", "Morning Shift (06:00 - 14:00)", 88),
        ProductionPlan("pp_2", "Batch 12-C: Precision CNC Pinions", "Robotic CNC Cell 5", 1200, 890, "In Progress", "Dual Continuous Shift", 94),
        ProductionPlan("pp_3", "Batch 08-F: Hydraulic Manifold Blocks", "Milling Line 1", 300, 45, "Scheduled", "Night Shift", 62)
    )

    private fun createSeedQualityReports(): List<QualityReport> = listOf(
        QualityReport("qr_1", "LOT-9921", "Cold-Rolled Steel Thickness Gauge", 1500, 4, "Compliant", "14 Oct 2026"),
        QualityReport("qr_2", "LOT-9918", "CNC Shaft Micro-Crack Ultrasonic", 800, 1, "Compliant", "13 Oct 2026"),
        QualityReport("qr_3", "LOT-9905", "Die-Cast Housing Porosity Test", 450, 18, "Under Review", "12 Oct 2026")
    )

    private fun createSeedReviews(): List<SupplierReview> = listOf(
        SupplierReview("rev_1", "comp_sup_1", "Vikram Malhotra", "Apex Industrial Systems", 5, "Exceptional metallurgical consistency and zero defect shipments across all Q3 deliveries.", "10 Oct 2026"),
        SupplierReview("rev_2", "comp_sup_1", "Sunil Kulkarni", "Mahindra Auto Component Division", 5, "On-time delivery index of 98.4%. Highly recommended for high-spec forge alloys.", "05 Oct 2026"),
        SupplierReview("rev_3", "comp_sup_2", "Ananya Sen", "Nexus Mobility & Hardware", 4, "Great microcontroller firmware stability. Slight transit delay during monsoon season.", "28 Sep 2026")
    )

    private fun createSeedFraudAlerts(): List<FraudAlert> = listOf(
        FraudAlert("fa_1", "Suspicious Multi-Bank Split Payment Detected", "HIGH", "Invoice INV-2026-9110 split across 4 unverified shell bank accounts with rapid velocity.", "Apex FinOps Vault", "42m ago"),
        FraudAlert("fa_2", "Duplicate GSTIN Registry Flag", "MEDIUM", "Supplier profile claimed GST 27AABCT9988C1Z4 which collides with inactive SEZ registry.", "Compliance Engine", "2h ago"),
        FraudAlert("fa_3", "Phantom Cargo Weight Telemetry Anomaly", "LOW", "Weighbridge delta +12.4% vs bill of lading tolerance on Truck MH-12-QZ-9022.", "Vadodara Gate 1", "5h ago")
    )

    private fun createSeedAuditLogs(): List<AuditLog> = listOf(
        AuditLog("log_1", "PO_APPROVED", "Vikram Malhotra", "Manufacturer", "10m ago", "PO-9843 approved for ₹2,10,000 to ElectroSilicon Components"),
        AuditLog("log_2", "AI_TRIGGER", "Linkora Neural Core", "System AI", "25m ago", "Automatic reorder point notification dispatched for Aluminum 6061"),
        AuditLog("log_3", "GATEWAY_PAYMENT", "Razorpay Webhook", "Billing", "1h ago", "Auto-renewal succeeded for Growth Plan (₹4,999)"),
        AuditLog("log_4", "SHIPMENT_CHECKPOINT", "GPS Telemetry Node #12", "Logistics", "2h ago", "Shipment LNK-TRK-98421 arrived at Bharuch Narmada Bridge Toll")
    )
}
