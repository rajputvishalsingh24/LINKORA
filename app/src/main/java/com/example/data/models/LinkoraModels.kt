package com.example.data.models

enum class UserRole(val label: String, val roleCode: String) {
    MANUFACTURER("Manufacturer", "ROLE_MANUFACTURER"),
    SUPPLIER("Supplier", "ROLE_SUPPLIER"),
    LOGISTICS("Logistics Partner", "ROLE_LOGISTICS"),
    BUYER("Buyer", "ROLE_BUYER"),
    ADMIN("Platform Admin", "ROLE_ADMIN")
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val companyName: String,
    val companyId: String,
    val designation: String = "Operations Lead"
)

data class Company(
    val id: String,
    val companyName: String,
    val industry: String,
    val gst: String,
    val location: String,
    val rating: Double,
    val reviewCount: Int,
    val isVerified: Boolean = true,
    val certifications: List<String> = listOf("ISO 9001", "RoHS", "GMP")
)

data class Product(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val name: String,
    val category: String,
    val price: Double,
    val moq: Int, // Minimum Order Quantity
    val stockQuantity: Int,
    val leadTimeDays: Int,
    val unit: String = "units",
    val rating: Double = 4.8,
    val description: String = ""
)

enum class InventoryCategory(val label: String) {
    RAW_MATERIALS("Raw Materials"),
    FINISHED_GOODS("Finished Goods"),
    WAREHOUSE_STOCK("Warehouse Stock")
}

data class InventoryItem(
    val id: String,
    val companyId: String,
    val name: String,
    val category: InventoryCategory,
    val stock: Int,
    val minThreshold: Int,
    val maxThreshold: Int,
    val unit: String,
    val warehouseLocation: String,
    val lastUpdated: String = "Today"
) {
    val isLowStock: Boolean get() = stock <= minThreshold
    val stockHealthPercent: Float get() = ((stock.toFloat() / maxThreshold.toFloat()) * 100f).coerceIn(0f, 100f)
}

enum class POStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    REJECTED("Rejected")
}

data class OrderItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

data class PurchaseOrder(
    val id: String,
    val poNumber: String,
    val manufacturerId: String,
    val manufacturerName: String,
    val supplierId: String,
    val supplierName: String,
    val amount: Double,
    val status: POStatus,
    val items: List<OrderItem>,
    val createdAt: String,
    val deliveryDueDate: String
)

enum class ShipmentStatus(val label: String) {
    SCHEDULED("Scheduled"),
    IN_TRANSIT("In Transit"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    DELAYED("Delayed")
}

data class Shipment(
    val id: String,
    val trackingNumber: String,
    val poNumber: String,
    val carrierName: String,
    val origin: String,
    val destination: String,
    val status: ShipmentStatus,
    val estimatedArrival: String,
    val routeType: String = "Fastest Route (AI)",
    val distanceKm: Double,
    val costEstimated: Double,
    val currentCheckpoint: String,
    val progressPercent: Float, // 0.0 to 1.0
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777
)

data class Quotation(
    val id: String,
    val rfqNumber: String,
    val supplierId: String,
    val supplierName: String,
    val buyerName: String,
    val productName: String,
    val quantity: Int,
    val offeredPrice: Double,
    val leadTimeDays: Int,
    val status: String = "Submitted", // Submitted, Accepted, Negotiating, Rejected
    val date: String
)

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val poNumber: String,
    val supplierName: String,
    val buyerName: String,
    val subtotal: Double,
    val gstRate: Double = 0.18,
    val isPaid: Boolean,
    val dueDate: String,
    val issuedDate: String
) {
    val gstAmount: Double get() = subtotal * gstRate
    val totalAmount: Double get() = subtotal + gstAmount
}

enum class AIPredictionType(val label: String) {
    DEMAND_FORECAST("Demand Forecasting"),
    SMART_INVENTORY("Smart Inventory AI"),
    SUPPLIER_RISK("Supplier Risk AI"),
    PRICE_FORECAST("Price Forecast AI"),
    ROUTE_OPTIMIZATION("Route Optimization AI")
}

data class AIPrediction(
    val id: String,
    val type: AIPredictionType,
    val headline: String,
    val summary: String,
    val confidence: Int, // e.g. 92%
    val recommendation: String,
    val metricTag: String,
    val timestamp: String
)

data class ProductionPlan(
    val id: String,
    val title: String,
    val productLine: String,
    val targetUnits: Int,
    val completedUnits: Int,
    val status: String, // In Progress, Scheduled, Completed
    val shift: String,
    val machineUtilizationPercent: Int
)

data class QualityReport(
    val id: String,
    val batchCode: String,
    val itemName: String,
    val inspectedCount: Int,
    val defectCount: Int,
    val complianceStatus: String, // Compliant, Under Review, Failed
    val date: String
) {
    val defectRate: Double get() = if (inspectedCount > 0) (defectCount.toDouble() / inspectedCount) * 100 else 0.0
}

data class SupplierReview(
    val id: String,
    val supplierId: String,
    val reviewerName: String,
    val company: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class FraudAlert(
    val id: String,
    val title: String,
    val riskLevel: String, // HIGH, MEDIUM, LOW
    val description: String,
    val entityAffected: String,
    val detectedTime: String,
    val status: String = "Active Monitoring"
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val priceMonthly: Int,
    val priceFormatted: String,
    val userLimit: String,
    val orderLimit: String,
    val features: List<String>,
    val isPopular: Boolean = false
)

data class AuditLog(
    val id: String,
    val action: String,
    val performedBy: String,
    val role: String,
    val timestamp: String,
    val details: String
)
