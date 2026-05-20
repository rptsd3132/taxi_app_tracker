package com.rptsd.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rptsd.app.ui.components.AppButton

private val targetApps = listOf("PICKME", "UBER", "BOLT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val rules = uiState.rules
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            snackbarHostState.showSnackbar("Settings saved")
            viewModel.clearSavedSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Rule Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Min Price ─────────────────────────────────────────────────────
            SectionLabel("Minimum Price (Rs.)")
            var minPriceText by remember(rules.minPrice) { mutableStateOf(rules.minPrice.toInt().toString()) }
            OutlinedTextField(
                value = minPriceText,
                onValueChange = { v ->
                    minPriceText = v
                    v.toDoubleOrNull()?.let { viewModel.updateMinPrice(it) }
                },
                label = { Text("Min price") },
                suffix = { Text("Rs.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Max Pickup Distance ───────────────────────────────────────────
            SectionLabel("Max Pickup Distance (km)")
            var distanceText by remember(rules.maxPickupDistance) { mutableStateOf(rules.maxPickupDistance.toString()) }
            OutlinedTextField(
                value = distanceText,
                onValueChange = { v ->
                    distanceText = v
                    v.toDoubleOrNull()?.let { viewModel.updateMaxDistance(it) }
                },
                label = { Text("Max distance") },
                suffix = { Text("km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Working Hours ─────────────────────────────────────────────────
            SectionLabel("Working Hours (24h HH:mm)")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                var startText by remember(rules.workingHoursStart) { mutableStateOf(rules.workingHoursStart) }
                OutlinedTextField(
                    value = startText,
                    onValueChange = { v ->
                        startText = v
                        if (v.matches(Regex("\\d{2}:\\d{2}"))) viewModel.updateWorkingStart(v)
                    },
                    label = { Text("Start") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                var endText by remember(rules.workingHoursEnd) { mutableStateOf(rules.workingHoursEnd) }
                OutlinedTextField(
                    value = endText,
                    onValueChange = { v ->
                        endText = v
                        if (v.matches(Regex("\\d{2}:\\d{2}"))) viewModel.updateWorkingEnd(v)
                    },
                    label = { Text("End") },
                    placeholder = { Text("22:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Target App ────────────────────────────────────────────────────
            SectionLabel("Target App")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                targetApps.forEach { app ->
                    FilterChip(
                        selected = rules.targetApp == app,
                        onClick = { viewModel.updateTargetApp(app) },
                        label = { Text(app) },
                    )
                }
            }

            // ── Random Skip ───────────────────────────────────────────────────
            SectionLabel("Random Skip — ${rules.randomSkipPercent}% (humanness)")
            Slider(
                value = rules.randomSkipPercent.toFloat(),
                onValueChange = { viewModel.updateRandomSkipPercent(it.toInt()) },
                valueRange = 0f..30f,
                steps = 29,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("0%", style = MaterialTheme.typography.labelSmall)
                Text("30%", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            AppButton(
                text = "Save Settings",
                onClick = { viewModel.save() },
                isLoading = uiState.isSaving,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}
