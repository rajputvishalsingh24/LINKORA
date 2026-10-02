package com.example.ui.screens.logistics

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ai.LinkoraAIEngine
import com.example.data.models.Shipment
import com.example.data.models.ShipmentStatus
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsDashboardScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val shipments by repository.shipments.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview & Map", "Freight Market", "Route AI", "Fuel Analytics", "Fleet SLA")

    var selectedShipmentForMap by remember { mutableStateOf<Shipment?>(shipments.firstOrNull()) }

    Column(modifier = modifier.fillMaxSize().testTag("logistics_dashboard")) {
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
                    modifier = Modifier.testTag("logistics_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> LogisticsOverviewTab(
                shipments = shipments,
                selectedShipment = selectedShipmentForMap ?: shipments.firstOrNull(),
                onSelectShipment = { selectedShipmentForMap = it },
                onUpdateProgress = { shpId, prog, checkpoint ->
                    repository.updateShipmentProgress(shpId, prog, checkpoint, ShipmentStatus.IN_TRANSIT)
                }
            )
            1 -> FreightMarketplaceTab(
                shipments = shipments,
                onAccept = { id -> repository.acceptShipment(id) }
            )
            2 -> RouteOptimizationAITab()
            3 -> FuelAnalyticsTab()
            4 -> FleetPerformanceTab(shipments = shipments)
        }
    }
}

@Composable
private fun LogisticsOverviewTab(
    shipments: List<Shipment>,
    selectedShipment: Shipment?,
    onSelectShipment: (Shipment) -> Unit,
    onUpdateProgress: (String, Float, String) -> Unit
) {
    val activeCount = shipments.count { it.status == ShipmentStatus.IN_TRANSIT }
    val deliveredCount = shipments.count { it.status == ShipmentStatus.DELIVERED }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Active Fleet",
                    value = "$activeCount En Route",
                    subtitle = "All GPS Nodes Online",
                    icon = Icons.Default.DirectionsTransit,
                    accentColor = LinkoraPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "On-Time Rate",
                    value = "96.4%",
                    subtitle = "$deliveredCount Delivered Today",
                    icon = Icons.Default.Schedule,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Corridor Map
        if (selectedShipment != null) {
            item {
                SectionHeader(
                    title = "Live GPS Corridor Telemetry",
                    subtitle = "${selectedShipment.trackingNumber} • ${selectedShipment.carrierName}"
                )
                InteractiveLogisticsMap(
                    origin = selectedShipment.origin,
                    destination = selectedShipment.destination,
                    currentCheckpoint = selectedShipment.currentCheckpoint,
                    progress = selectedShipment.progressPercent,
                    routeType = selectedShipment.routeType
                )
            }

            // Quick Telemetry Ping Button
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulate GPS Ping", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Fast-forward vehicle down expressway", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                val nextProg = (selectedShipment.progressPercent + 0.15f).coerceAtMost(1f)
                                val check = if (nextProg >= 1f) "Delivered to Dock Gate" else "Waypoint Milestone (Advancing)"
                                onUpdateProgress(selectedShipment.id, nextProg, check)
                            },
                            modifier = Modifier.testTag("ping_gps_btn")
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Advance GPS")
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Fleet Consignments", subtitle = "Select a consignment to inspect live track")
        }

        items(shipments) { shp ->
            ShipmentItemCard(
                shipment = shp,
                isSelected = selectedShipment?.id == shp.id,
                onClick = { onSelectShipment(shp) }
            )
        }
    }
}

@Composable
private fun ShipmentItemCard(
    shipment: Shipment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shipment_card_${shipment.trackingNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LinkoraPrimary), width = 1.5.dp) else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(shipment.trackingNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadge(shipment.status.label, shipment.status.name)
            }

            Text("${shipment.origin} → ${shipment.destination}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Checkpoint: ${shipment.currentCheckpoint}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ETA: ${shipment.estimatedArrival}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                Text("${shipment.distanceKm} km • ₹${String.format("%,.0f", shipment.costEstimated)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FreightMarketplaceTab(
    shipments: List<Shipment>,
    onAccept: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Open Freight Consignment Marketplace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Claim open manufacturing freight loads and assign fleet drivers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(shipments) { shp ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("freight_open_${shp.trackingNumber}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(shp.trackingNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Freight Pay: ₹${String.format("%,.0f", shp.costEstimated)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }

                    Text("${shp.origin} ➔ ${shp.destination}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Total Haul: ${shp.distanceKm} km • Required: 24ft Multi-Axle Container", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Carrier: ${shp.carrierName}", style = MaterialTheme.typography.labelSmall)
                        Button(
                            onClick = { onAccept(shp.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("accept_freight_${shp.trackingNumber}")
                        ) {
                            Text("Accept Load")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteOptimizationAITab() {
    var origin by remember { mutableStateOf("Mumbai Port JNPT") }
    var destination by remember { mutableStateOf("Delhi NCR Logistics Park") }
    var weightTons by remember { mutableStateOf("22.5") }
    var routePreference by remember { mutableStateOf("Fastest Route") }

    var simulationResult by remember {
        mutableStateOf(LinkoraAIEngine.calculateRouteOptimization(origin, destination, 22.5, "Fastest Route"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("AI Route Optimization Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Calculate fastest vs lowest-cost corridor with live toll, diesel & carbon analytics", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = origin, onValueChange = { origin = it }, label = { Text("Origin Hub") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination Hub") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightTons,
                            onValueChange = { weightTons = it },
                            label = { Text("Cargo Weight (Tons)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Optimization Mode", style = MaterialTheme.typography.labelSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = routePreference == "Fastest Route",
                                    onClick = { routePreference = "Fastest Route" },
                                    label = { Text("Fastest") }
                                )
                                Spacer(Modifier.width(4.dp))
                                FilterChip(
                                    selected = routePreference == "Lowest Cost",
                                    onClick = { routePreference = "Lowest Cost" },
                                    label = { Text("Lowest Cost") }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val w = weightTons.toDoubleOrNull() ?: 20.0
                            simulationResult = LinkoraAIEngine.calculateRouteOptimization(origin, destination, w, routePreference)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("run_route_calc_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Calculate AI Optimum Corridor")
                    }
                }
            }
        }

        // Result Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("route_result_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(simulationResult.routeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                        Surface(color = StatusSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text("AI Verified", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = StatusSuccess, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(simulationResult.summary, style = MaterialTheme.typography.bodySmall)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem(label = "Transit Distance", value = "${String.format("%.0f", simulationResult.totalDistanceKm)} km")
                        MetricItem(label = "Transit Duration", value = "${String.format("%.1f", simulationResult.estimatedHours)} Hours")
                        MetricItem(label = "Fuel Expense", value = "₹${String.format("%,.0f", simulationResult.estimatedFuelCost)}")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem(label = "Estimated Tolls", value = "₹${String.format("%,.0f", simulationResult.tollCost)}")
                        MetricItem(label = "CO2 Abatement", value = "${simulationResult.carbonSavedKg} kg Saved")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FuelAnalyticsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Fleet Fuel & Efficiency Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Real-time telemetry from connected CAN bus vehicle sensors", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Avg Fuel Burn",
                    value = "₹28.4 / km",
                    subtitle = "-8.2% vs Industry Benchmark",
                    icon = Icons.Default.LocalGasStation,
                    accentColor = LinkoraPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Idle Time Waste",
                    value = "4.2%",
                    subtitle = "Reduced toll bottleneck wait",
                    icon = Icons.Default.Timer,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Driver Eco-Score & Green Transit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Truck MH-12-QZ-9022 • Driver: Ramesh Yadav", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    MetricBar(label = "Speed Compliance", percent = 0.94f, display = "94%")
                    MetricBar(label = "Harsh Braking Avoidance", percent = 0.91f, display = "91%")
                    MetricBar(label = "Engine Idling Threshold", percent = 0.96f, display = "96%")
                }
            }
        }
    }
}

@Composable
private fun FleetPerformanceTab(shipments: List<Shipment>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Carrier Delivery SLA Scorecard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Historical reliability across major industrial corridors", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Monthly SLA Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    MetricBar(label = "Western Expressway (Mumbai - Delhi)", percent = 0.98f, display = "98.2% On Time")
                    MetricBar(label = "Southern Corridor (Bengaluru - Chennai)", percent = 0.99f, display = "99.4% On Time")
                    MetricBar(label = "Northern Heavy Haul (Punjab - NCR)", percent = 0.92f, display = "92.0% (Weather Delays)")
                }
            }
        }
    }
}
