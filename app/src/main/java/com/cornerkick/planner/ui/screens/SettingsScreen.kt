package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.BuildConfig
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.ui.AppStrings
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.Labels
import com.cornerkick.planner.ui.components.DisclaimerCard
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    appViewModel: AppViewModel,
    onOpenMatchSettings: () -> Unit,
    onShowOnboarding: () -> Unit,
    onBack: () -> Unit,
) {
    val appData by appViewModel.appData.collectAsState()
    val settings = appData.settings

    var confirmDeleteSchemes by remember { mutableStateOf(false) }
    var confirmResetAll by remember { mutableStateOf(false) }

    // API status without ever exposing the token value.
    val token = BuildConfig.FOOTBALL_DATA_API_TOKEN.trim()
    val tokenConfigured = token.isNotBlank() && token.lowercase() != "your_api_token_here"
    val apiStatus = if (tokenConfigured) {
        "API token configured. Live data can be loaded."
    } else {
        "No API token configured. Match Schedule uses demo data."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StrongOrange,
                    titleContentColor = WhiteText,
                    navigationIconContentColor = WhiteText,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Tactics preferences ------------------------------------------------
            SectionHeader("Tactics preferences")
            Text("Default corner type", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = settings.defaultCornerType == null,
                    onClick = { appViewModel.updateSettings { it.copy(defaultCornerType = null) } },
                    label = { Text("None") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                )
                CornerType.entries.forEach { t ->
                    FilterChip(
                        selected = settings.defaultCornerType == t,
                        onClick = { appViewModel.updateSettings { it.copy(defaultCornerType = t) } },
                        label = { Text(Labels.cornerType(t)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Compact mode", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.compactMode,
                    onCheckedChange = { checked -> appViewModel.updateSettings { it.copy(compactMode = checked) } },
                )
            }

            // Match schedule -----------------------------------------------------
            SectionHeader("Match Schedule")
            SettingsActionRow("Match Schedule settings", onClick = onOpenMatchSettings)
            Text(apiStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { appViewModel.clearMatchCache() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Clear match cache") }

            // App section --------------------------------------------------------
            SectionHeader("App")
            OutlinedButton(
                onClick = onShowOnboarding,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Show onboarding again") }

            OutlinedButton(
                onClick = { confirmDeleteSchemes = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Delete all schemes", color = MaterialTheme.colorScheme.error) }

            OutlinedButton(
                onClick = { confirmResetAll = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Reset all local data", color = MaterialTheme.colorScheme.error) }

            // Info ---------------------------------------------------------------
            SectionHeader("App information")
            Text(
                "CornerKick Planner v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "A manual football corner-kick tactics planner.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionHeader("Tactics disclaimer")
            DisclaimerCard(text = AppStrings.TACTICS_DISCLAIMER)

            SectionHeader("Match schedule API disclaimer")
            DisclaimerCard(text = AppStrings.MATCH_DISCLAIMER)

            SectionHeader("Privacy note")
            DisclaimerCard(text = AppStrings.PRIVACY_NOTE)

            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmDeleteSchemes) {
        ConfirmDialog(
            title = "Delete all schemes?",
            message = "This permanently removes every saved corner scheme on this device.",
            onConfirm = {
                appViewModel.deleteAllSchemes()
                confirmDeleteSchemes = false
            },
            onDismiss = { confirmDeleteSchemes = false },
        )
    }

    if (confirmResetAll) {
        ConfirmDialog(
            title = "Reset all local data?",
            message = "This clears all schemes, settings, and cached match data on this device.",
            onConfirm = {
                appViewModel.resetAllLocalData()
                confirmResetAll = false
            },
            onDismiss = { confirmResetAll = false },
        )
    }
}

@Composable
private fun SettingsActionRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
        Text("›", style = MaterialTheme.typography.titleLarge, color = StrongOrange)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = WhiteText),
            ) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
