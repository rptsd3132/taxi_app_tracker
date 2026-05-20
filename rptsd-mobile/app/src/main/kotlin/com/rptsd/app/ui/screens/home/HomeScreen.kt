package com.rptsd.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rptsd.app.data.repository.RideHistory
import com.rptsd.app.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rules = uiState.rules
    val stats = uiState.todayStats
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-check notification permission each time the screen is resumed
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.checkPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val statusColor = when {
        !uiState.isNotificationPermissionGranted -> Color(0xFFF57C00)
        !rules.isAutoAcceptEnabled -> MaterialTheme.colorScheme.error
        else -> Color(0xFF2E7D32)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("RPTSD Driver", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        // ── Notification Permission Banner ────────────────────────────────────
        item {
            if (!uiState.isNotificationPermissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(24.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notification permission required",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Auto-accept won't work without it",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFBF360C),
                            )
                        }
                        TextButton(
                            onClick = { navController.navigate(Screen.Permissions.route) },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("Fix") }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = if (rules.isAutoAcceptEnabled)
                                "Watching for rides…"
                            else
                                "Notification listening active — toggle ON to watch",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1B5E20),
                        )
                    }
                }
            }
        }

        // ── Master Toggle ─────────────────────────────────────────────────────
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (rules.isAutoAcceptEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        )
                    }
                    Switch(
                        checked = rules.isAutoAcceptEnabled,
                        onCheckedChange = { viewModel.toggleAutoAccept(it) },
                        enabled = uiState.isNotificationPermissionGranted,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF2E7D32),
                            checkedTrackColor = Color(0xFF2E7D32).copy(alpha = 0.4f),
                        ),
                    )
                    if (!uiState.isNotificationPermissionGranted) {
                        Text(
                            text = "Grant notification permission to enable",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Today's Stats ─────────────────────────────────────────────────────
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatItem("Accepted", stats.accepted.toString(), Color(0xFF2E7D32))
                        StatItem("Skipped", stats.skipped.toString(), MaterialTheme.colorScheme.error)
                        StatItem("Missed", stats.missed.toString(), Color(0xFFF57C00))
                        StatItem("Earnings", "Rs.${stats.totalEarnings.toInt()}", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // ── Active Rules ──────────────────────────────────────────────────────
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Rules",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    RuleRow("Min Price", "Rs. ${rules.minPrice.toInt()}")
                    RuleRow("Max Distance", "${rules.maxPickupDistance} km")
                    RuleRow("Hours", "${rules.workingHoursStart} – ${rules.workingHoursEnd}")
                    RuleRow("App", rules.targetApp)
                    RuleRow("Random Skip", "${rules.randomSkipPercent}%")
                }
            }
        }

        // ── Recent Rides ──────────────────────────────────────────────────────
        if (uiState.recentRides.isNotEmpty()) {
            item {
                Text("Recent Rides", style = MaterialTheme.typography.titleMedium)
            }
            items(uiState.recentRides) { ride -> RideHistoryCard(ride) }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No rides recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RuleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RideHistoryCard(ride: RideHistory) {
    val isAccepted = ride.decision == "ACCEPTED"
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ride.detectedAt))
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isAccepted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ride.pickupLocation.ifBlank { ride.sourceApp }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(ride.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Rs.${ride.price.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
