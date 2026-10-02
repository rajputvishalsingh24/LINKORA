package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("stat_card_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AIInsightCard(
    prediction: AIPrediction,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("ai_insight_card_${prediction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    LinkoraAccentTeal.copy(alpha = 0.6f),
                    LinkoraAIIndigo.copy(alpha = 0.4f),
                    Color.Transparent
                )
            ),
            width = 1.5.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(LinkoraAccentTeal, LinkoraAIIndigo))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Intelligence",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = prediction.type.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LinkoraPrimary
                    )
                }

                Surface(
                    color = LinkoraAIIndigo.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = LinkoraAIIndigo,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${prediction.confidence}% Confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinkoraAIIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = prediction.headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = prediction.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = StatusWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = prediction.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (onActionClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prediction.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("ai_action_btn_${prediction.id}")
                    ) {
                        Text("Apply AI Recommendation", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    label: String,
    type: String = "info"
) {
    val (bgColor, textColor) = when (type.lowercase()) {
        "success", "delivered", "compliant", "approved", "paid" -> StatusSuccess.copy(alpha = 0.15f) to StatusSuccess
        "warning", "pending", "under review", "in transit", "delayed" -> StatusWarning.copy(alpha = 0.15f) to StatusWarning
        "error", "failed", "rejected", "high" -> StatusError.copy(alpha = 0.15f) to StatusError
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.primary
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.testTag("section_action_${title.take(8)}")
            ) {
                Text(actionText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun CostBreakdownChart(
    procurementCost: Double = 1420000.0,
    productionCost: Double = 980000.0,
    logisticsCost: Double = 340000.0,
    modifier: Modifier = Modifier
) {
    val total = procurementCost + productionCost + logisticsCost
    val procPct = (procurementCost / total).toFloat()
    val prodPct = (productionCost / total).toFloat()
    val logPct = (logisticsCost / total).toFloat()

    Card(
        modifier = modifier.testTag("cost_breakdown_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operational Cost Allocation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%,.0f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LinkoraPrimary
                )
            }

            // Visual Segmented Bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
            ) {
                val w = size.width
                val h = size.height
                val p1 = w * procPct
                val p2 = w * (procPct + prodPct)

                drawRect(color = LinkoraPrimary, topLeft = Offset(0f, 0f), size = Size(p1, h))
                drawRect(color = LinkoraAIIndigo, topLeft = Offset(p1, 0f), size = Size(p2 - p1, h))
                drawRect(color = LinkoraAccentTeal, topLeft = Offset(p2, 0f), size = Size(w - p2, h))
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CostLegendItem(label = "Procurement (${(procPct * 100).toInt()}%)", amount = procurementCost, color = LinkoraPrimary)
                CostLegendItem(label = "Production (${(prodPct * 100).toInt()}%)", amount = productionCost, color = LinkoraAIIndigo)
                CostLegendItem(label = "Logistics (${(logPct * 100).toInt()}%)", amount = logisticsCost, color = LinkoraAccentTeal)
            }
        }
    }
}

@Composable
private fun CostLegendItem(label: String, amount: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "₹${String.format("%,.0f", amount)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InteractiveLogisticsMap(
    origin: String,
    destination: String,
    currentCheckpoint: String,
    progress: Float,
    routeType: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("interactive_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS Active",
                        tint = LinkoraAccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Real-Time Fleet Corridor Telemetry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = LinkoraPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = routeType,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = LinkoraPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Canvas Corridor Map Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBg)
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val startX = 24.dp.toPx()
                    val endX = w - 24.dp.toPx()
                    val centerY = h / 2

                    // Grid lines
                    for (i in 1..4) {
                        val gx = w * (i / 5f)
                        drawLine(
                            color = Color(0x1538BDF8),
                            start = Offset(gx, 0f),
                            end = Offset(gx, h),
                            strokeWidth = 1f
                        )
                    }

                    // Background planned route line
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(startX, centerY),
                        end = Offset(endX, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Active progress line
                    val currentX = startX + (endX - startX) * progress
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                        ),
                        start = Offset(startX, centerY),
                        end = Offset(currentX, centerY),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Origin node
                    drawCircle(
                        color = Color(0xFF0284C7),
                        radius = 7.dp.toPx(),
                        center = Offset(startX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(startX, centerY)
                    )

                    // Destination node
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 7.dp.toPx(),
                        center = Offset(endX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(endX, centerY)
                    )

                    // Live vehicle position pulse
                    drawCircle(
                        color = Color(0x5506B6D4),
                        radius = 14.dp.toPx(),
                        center = Offset(currentX, centerY)
                    )
                    drawCircle(
                        color = Color(0xFF06B6D4),
                        radius = 8.dp.toPx(),
                        center = Offset(currentX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(currentX, centerY)
                    )
                }

                // Node Labels
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = origin,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = destination,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }

            // Current Checkpoint Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = LinkoraPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Current: $currentCheckpoint (${(progress * 100).toInt()}% En Route)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MetricBar(label: String, percent: Float, display: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(display, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
        }
        LinearProgressIndicator(
            progress = { percent },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = LinkoraPrimary
        )
    }
}

@Composable
fun POCard(
    po: PurchaseOrder,
    onApprove: (() -> Unit)?,
    onReject: (() -> Unit)?,
    onMarkDelivered: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("po_card_${po.poNumber}"),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(po.poNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(po.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(label = po.status.label, type = po.status.name)
            }

            Text(
                text = po.supplierName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Items list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                po.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("₹${String.format("%,.0f", item.total)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format("%,.0f", po.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LinkoraPrimary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (po.status == POStatus.PENDING && onApprove != null && onReject != null) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp).testTag("reject_po_${po.poNumber}")
                        ) {
                            Text("Reject", color = StatusError, style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                            modifier = Modifier.height(36.dp).testTag("approve_po_${po.poNumber}")
                        ) {
                            Text("Approve", style = MaterialTheme.typography.labelMedium)
                        }
                    } else if (po.status == POStatus.SHIPPED && onMarkDelivered != null) {
                        Button(
                            onClick = onMarkDelivered,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LinkoraPrimary),
                            modifier = Modifier.height(36.dp).testTag("receive_po_${po.poNumber}")
                        ) {
                            Text("Mark Delivered", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

