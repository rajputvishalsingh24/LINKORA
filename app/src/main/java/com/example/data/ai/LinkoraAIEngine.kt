package com.example.data.ai

import com.example.data.models.AIPrediction
import com.example.data.models.AIPredictionType
import java.util.UUID

object LinkoraAIEngine {

    fun generateInitialPredictions(): List<AIPrediction> {
        return listOf(
            AIPrediction(
                id = UUID.randomUUID().toString(),
                type = AIPredictionType.DEMAND_FORECAST,
                headline = "Industrial Steel demand surging +15% next month",
                summary = "Automotive OEM assembly ramp-up across Western clusters is projected to drive structural plate demand to 4,200 metric tons.",
                confidence = 94,
                recommendation = "Lock in forward purchase contracts before Q4 supplier price revisions.",
                metricTag = "+15.2% MoM",
                timestamp = "Updated 10m ago"
            ),
            AIPrediction(
                id = UUID.randomUUID().toString(),
                type = AIPredictionType.SMART_INVENTORY,
                headline = "Reorder Trigger: High-Grade Aluminum Billet 6061",
                summary = "Current run rate of 42 units/day will deplete safety buffer in 12 days based on actual CNC machine utilization.",
                confidence = 91,
                recommendation = "Order 500 units immediately to maintain uninterrupted production schedules.",
                metricTag = "12 Days Remaining",
                timestamp = "Updated 25m ago"
            ),
            AIPrediction(
                id = UUID.randomUUID().toString(),
                type = AIPredictionType.SUPPLIER_RISK,
                headline = "TechSteel Forgings: 78% Logistics Delay Probability",
                summary = "Port container congestion and regional haulage strikes in Gujarat corridor indicate high delivery slippage for PO-9842.",
                confidence = 88,
                recommendation = "Activate secondary logistics routing or dispatch partial batch via air freight.",
                metricTag = "Risk Index: High (78%)",
                timestamp = "Updated 1h ago"
            ),
            AIPrediction(
                id = UUID.randomUUID().toString(),
                type = AIPredictionType.PRICE_FORECAST,
                headline = "Hot-Rolled Coil Steel prices expected to rise 8% next quarter",
                summary = "Iron ore benchmark spikes and coking coal import tariffs are pushing mill offer prices upwards across Asian exchanges.",
                confidence = 86,
                recommendation = "Hedge raw material procurement or negotiate quarterly price caps with preferred vendors.",
                metricTag = "+8.4% Q4 Est.",
                timestamp = "Updated 2h ago"
            ),
            AIPrediction(
                id = UUID.randomUUID().toString(),
                type = AIPredictionType.ROUTE_OPTIMIZATION,
                headline = "AI Multi-Modal Route saves ₹34,200 & 4.5 Hours",
                summary = "NH-48 bottleneck bypass via Western Dedicated Freight Corridor reduces transit time by 28% and cuts fleet diesel consumption by 110L.",
                confidence = 96,
                recommendation = "Reroute Mumbai-to-Delhi convoy via Expressway Segment B-4.",
                metricTag = "₹34.2K Saved",
                timestamp = "Updated 3h ago"
            )
        )
    }

    data class RouteSimulationResult(
        val routeName: String,
        val totalDistanceKm: Double,
        val estimatedHours: Double,
        val estimatedFuelCost: Double,
        val tollCost: Double,
        val carbonSavedKg: Double,
        val summary: String
    )

    fun calculateRouteOptimization(origin: String, destination: String, cargoWeightTons: Double, preference: String): RouteSimulationResult {
        val isFastest = preference.contains("Fastest", ignoreCase = true)
        val baseDist = if (origin.contains("Mumbai", true) && destination.contains("Delhi", true)) 1420.0 else 680.0
        val dist = if (isFastest) baseDist * 0.96 else baseDist * 1.04
        val hours = if (isFastest) (dist / 68.0) else (dist / 52.0)
        val fuelPerKm = 0.28 + (cargoWeightTons * 0.015)
        val fuelLiters = dist * fuelPerKm
        val fuelCost = fuelLiters * 92.5 // ~₹92.5/L diesel
        val tolls = if (isFastest) 3200.0 else 1650.0

        return RouteSimulationResult(
            routeName = if (isFastest) "Smart Expressway Corridor (AI-Fastest)" else "Economic Multi-Modal Transit (AI-Lowest Cost)",
            totalDistanceKm = dist,
            estimatedHours = hours,
            estimatedFuelCost = fuelCost,
            tollCost = tolls,
            carbonSavedKg = if (isFastest) 85.0 else 145.0,
            summary = if (isFastest)
                "Bypasses 3 urban toll bottlenecks using smart highway telemetry with real-time ETA guarantees."
            else
                "Optimized for minimal gradient incline and lower toll charges, saving ₹${String.format("%.0f", (fuelCost * 0.14))} in net logistics expenses."
        )
    }

    fun analyzeCustomDemand(commodity: String, horizon: String, marketSector: String): AIPrediction {
        val changePct = when {
            commodity.contains("Steel", true) -> "+14.8%"
            commodity.contains("Copper", true) -> "+9.2%"
            commodity.contains("Semiconductor", true) -> "+22.4%"
            commodity.contains("Polymer", true) -> "-4.1%"
            else -> "+11.5%"
        }

        return AIPrediction(
            id = UUID.randomUUID().toString(),
            type = AIPredictionType.DEMAND_FORECAST,
            headline = "$commodity Demand Analysis ($horizon): Projected $changePct",
            summary = "Predictive telemetry across $marketSector indicates steady order backlog accumulation with 92% confidence based on rolling 18-month historical consumption.",
            confidence = (87..96).random(),
            recommendation = "Adjust production capacity batching and initiate Supplier RFQs 2 weeks ahead of historical schedule.",
            metricTag = "$changePct $horizon",
            timestamp = "Just now"
        )
    }
}
