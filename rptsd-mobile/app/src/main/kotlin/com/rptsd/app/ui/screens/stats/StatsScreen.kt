package com.rptsd.app.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rptsd.app.data.repository.RideHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.summary
    val primary = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Date range tabs ───────────────────────────────────────────────────
        item {
            Text("Statistics", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateRange.values().forEach { range ->
                    FilterChip(
                        selected = uiState.selectedRange == range,
                        onClick = { viewModel.selectRange(range) },
                        label = { Text(range.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }

        // ── Summary row ───────────────────────────────────────────────────────
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SummaryItem("Total", stats.totalRides.toString(), MaterialTheme.colorScheme.onSurface)
                    SummaryItem("Accepted", stats.accepted.toString(), Color(0xFF2E7D32))
                    SummaryItem("Skipped", stats.skipped.toString(), errorColor)
                    SummaryItem("Earnings", "Rs.${stats.totalEarnings.toInt()}", primary)
                }
            }
        }

        // ── Bar chart ─────────────────────────────────────────────────────────
        if (uiState.barData.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Accepted vs Skipped", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        BarChart(
                            bars = uiState.barData,
                            acceptedColor = Color(0xFF2E7D32),
                            skippedColor = errorColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                        )
                    }
                }
            }
        }

        // ── Full history list ─────────────────────────────────────────────────
        if (uiState.history.isNotEmpty()) {
            item {
                Text("Ride History", style = MaterialTheme.typography.titleMedium)
            }
            items(uiState.history) { ride ->
                RideRow(ride)
            }
        } else if (!uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No rides in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BarChart(
    bars: List<DayBar>,
    acceptedColor: Color,
    skippedColor: Color,
    modifier: Modifier = Modifier,
) {
    val maxVal = bars.maxOf { it.accepted + it.skipped }.coerceAtLeast(1).toFloat()

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val barGroupWidth = size.width / bars.size
            val barWidth = barGroupWidth * 0.35f
            val gap = barGroupWidth * 0.05f
            val cornerRadius = CornerRadius(4.dp.toPx())

            bars.forEachIndexed { i, bar ->
                val groupLeft = i * barGroupWidth + barGroupWidth * 0.1f

                // Accepted bar
                val acceptedH = (bar.accepted / maxVal) * size.height
                if (acceptedH > 0) {
                    drawRoundRect(
                        color = acceptedColor,
                        topLeft = Offset(groupLeft, size.height - acceptedH),
                        size = Size(barWidth, acceptedH),
                        cornerRadius = cornerRadius,
                    )
                }

                // Skipped bar
                val skippedH = (bar.skipped / maxVal) * size.height
                if (skippedH > 0) {
                    drawRoundRect(
                        color = skippedColor,
                        topLeft = Offset(groupLeft + barWidth + gap, size.height - skippedH),
                        size = Size(barWidth, skippedH),
                        cornerRadius = cornerRadius,
                    )
                }
            }
        }

        // Labels row
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(acceptedColor, "Accepted")
            LegendDot(skippedColor, "Skipped")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RideRow(ride: RideHistory) {
    val isAccepted = ride.decision == "ACCEPTED"
    val timeStr = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(ride.detectedAt))
    val decisionColor = if (isAccepted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    HorizontalDivider(thickness = 0.5.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = ride.decision,
            style = MaterialTheme.typography.labelMedium,
            color = decisionColor,
            modifier = Modifier.width(64.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ride.pickupLocation.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall)
            Text(text = ride.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "Rs.${ride.price.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(text = timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
