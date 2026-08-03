package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.cornerkick.planner.ui.AppStrings
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.components.DisclaimerCard
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText
import com.cornerkick.planner.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScheduleSettingsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val appData by appViewModel.appData.collectAsState()
    val settings = appData.settings.matchSchedule

    var apiEnabled by remember(settings.apiEnabled) { mutableStateOf(settings.apiEnabled) }
    var useDemo by remember(settings.useDemoData) { mutableStateOf(settings.useDemoData) }
    var dateFrom by remember(settings.dateFrom) { mutableStateOf(settings.dateFrom) }
    var dateTo by remember(settings.dateTo) { mutableStateOf(settings.dateTo) }
    var competition by remember(settings.competitionCode) { mutableStateOf(settings.competitionCode) }
    var message by remember { mutableStateOf("") }

    fun validationError(): String? {
        if (dateFrom.isNotBlank() && !DateUtils.isValidIso(dateFrom)) return "Date from must be empty or valid (YYYY-MM-DD)."
        if (dateTo.isNotBlank() && !DateUtils.isValidIso(dateTo)) return "Date to must be empty or valid (YYYY-MM-DD)."
        if (!DateUtils.fromNotAfterTo(dateFrom.ifBlank { DateUtils.today() }, dateTo.ifBlank { DateUtils.todayPlus(9) })) {
            return "Date to must not be earlier than date from."
        }
        return null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Schedule settings") },
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
            DisclaimerCard(text = AppStrings.MATCH_DISCLAIMER)

            SwitchRow("Match API enabled", apiEnabled) { apiEnabled = it }
            SwitchRow("Use demo data", useDemo) { useDemo = it }

            SectionHeader("Date window")
            Text(
                "Leave both dates empty to use the default 10-day window (today + 9 days).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = dateFrom,
                onValueChange = { dateFrom = it; message = "" },
                label = { Text("Date from (YYYY-MM-DD, optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = dateTo,
                onValueChange = { dateTo = it; message = "" },
                label = { Text("Date to (YYYY-MM-DD, optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = competition,
                onValueChange = { competition = it.uppercase().take(20) },
                label = { Text("Competition code filter (optional, e.g. PL)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(
                onClick = {
                    dateFrom = ""
                    dateTo = ""
                    message = "Reset to default 10-day window."
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Reset to default 10-day window") }

            OutlinedButton(
                onClick = {
                    appViewModel.clearMatchCache()
                    message = "Match cache cleared."
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Clear match cache") }

            if (message.isNotBlank()) {
                Text(message, color = StrongOrange, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    val err = validationError()
                    if (err != null) {
                        message = err
                        return@Button
                    }
                    appViewModel.updateSettings { s ->
                        s.copy(
                            matchSchedule = s.matchSchedule.copy(
                                apiEnabled = apiEnabled,
                                useDemoData = useDemo,
                                dateFrom = dateFrom.trim(),
                                dateTo = dateTo.trim(),
                                competitionCode = competition.trim(),
                            )
                        )
                    }
                    message = "Settings saved."
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) { Text("Save settings", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
