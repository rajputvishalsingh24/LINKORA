package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AIPrediction
import com.example.data.models.AIPredictionType
import com.example.data.repository.LinkoraRepository
import com.example.ui.components.AIInsightCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIIntelligenceHubScreen(
    repository: LinkoraRepository,
    modifier: Modifier = Modifier
) {
    val aiPredictions by repository.aiPredictions.collectAsState()
    var selectedTypeFilter by remember { mutableStateOf<AIPredictionType?>(null) }

    var commodityInput by remember { mutableStateOf("Hot-Rolled Steel Coils") }
    var horizonInput by remember { mutableStateOf("Next Quarter (Q4)") }
    var sectorInput by remember { mutableStateOf("Automotive & Heavy Industry") }

    var isSimulating by remember { mutableStateOf(false) }

    val filtered = remember(aiPredictions, selectedTypeFilter) {
        if (selectedTypeFilter == null) aiPredictions else aiPredictions.filter { it.type == selectedTypeFilter }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp).testTag("ai_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Command Center Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(LinkoraAccentTeal, LinkoraAIIndigo, LinkoraPrimary)),
                    width = 2.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(LinkoraAccentTeal, LinkoraAIIndigo))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("LINKORA NEURAL INTELLIGENCE ENGINE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary, letterSpacing = 1.sp)
                            Text("Predictive Supply Chain Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Continuous neural analysis of historical purchase order flow, commodity prices, supplier delay telemetry, and transit bottlenecks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Custom Neural Simulation Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("ai_simulator_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Execute Custom AI Demand & Price Forecast", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = commodityInput,
                        onValueChange = { commodityInput = it },
                        label = { Text("Commodity / Raw Material") },
                        modifier = Modifier.fillMaxWidth().testTag("ai_commodity_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = horizonInput,
                            onValueChange = { horizonInput = it },
                            label = { Text("Forecast Horizon") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sectorInput,
                            onValueChange = { sectorInput = it },
                            label = { Text("Sector Focus") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            isSimulating = true
                            repository.triggerCustomAIPrediction(commodityInput, horizonInput, sectorInput)
                            isSimulating = false
                        },
                        modifier = Modifier.fillMaxWidth().testTag("run_ai_forecast_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = LinkoraPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Synthesize Neural Prediction")
                    }
                }
            }
        }

        // Filter Pills for 5 AI Pillars
        item {
            Text("AI PILLARS & MODELS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
            Spacer(Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("All (${aiPredictions.size})") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == AIPredictionType.DEMAND_FORECAST,
                        onClick = { selectedTypeFilter = AIPredictionType.DEMAND_FORECAST },
                        label = { Text("Demand") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == AIPredictionType.SMART_INVENTORY,
                        onClick = { selectedTypeFilter = AIPredictionType.SMART_INVENTORY },
                        label = { Text("Inventory") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTypeFilter == AIPredictionType.SUPPLIER_RISK,
                        onClick = { selectedTypeFilter = AIPredictionType.SUPPLIER_RISK },
                        label = { Text("Supplier Risk") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == AIPredictionType.PRICE_FORECAST,
                        onClick = { selectedTypeFilter = AIPredictionType.PRICE_FORECAST },
                        label = { Text("Price Forecast") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == AIPredictionType.ROUTE_OPTIMIZATION,
                        onClick = { selectedTypeFilter = AIPredictionType.ROUTE_OPTIMIZATION },
                        label = { Text("Routes") }
                    )
                }
            }
        }

        items(filtered) { prediction ->
            AIInsightCard(prediction = prediction, onActionClick = {})
        }
    }
}
