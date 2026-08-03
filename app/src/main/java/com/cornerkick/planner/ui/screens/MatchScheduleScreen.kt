package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.data.model.MatchSource
import com.cornerkick.planner.data.model.NormalizedMatch
import com.cornerkick.planner.ui.AppStrings
import com.cornerkick.planner.ui.MatchScheduleViewModel
import com.cornerkick.planner.ui.components.DisclaimerCard
import com.cornerkick.planner.ui.components.EmptyState
import com.cornerkick.planner.ui.components.InfoChip
import com.cornerkick.planner.ui.theme.ErrorRed
import com.cornerkick.planner.ui.theme.SoftOrangePanel
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteCard
import com.cornerkick.planner.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScheduleScreen(
    viewModel: MatchScheduleViewModel,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Match settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StrongOrange,
                    titleContentColor = WhiteText,
                    navigationIconContentColor = WhiteText,
                    actionIconContentColor = WhiteText,
                ),
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { DisclaimerCard(text = AppStrings.MATCH_SHORT_NOTE) }

            item {
                Column {
                    val windowLabel = if (state.usingDefaultWindow) {
                        "Today + 9 days (${state.dateFrom} → ${state.dateTo})"
                    } else {
                        "${state.dateFrom} → ${state.dateTo}"
                    }
                    Text("Active window: $windowLabel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (state.lastUpdatedAt.isNotBlank()) {
                        Text("Last updated: ${state.lastUpdatedAt}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val sourceLabel = when (state.source) {
                            MatchSource.Api -> "Live data"
                            MatchSource.Cache -> "Cached data"
                            MatchSource.Demo -> "Demo data"
                        }
                        InfoChip(sourceLabel)
                    }
                }
            }

            if (state.message.isNotBlank()) {
                item {
                    val bg = if (state.isWarning) SoftOrangePanel else SoftOrangePanel
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isWarning) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    )
                }
            }

            when {
                state.loading -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = StrongOrange)
                            Spacer(Modifier.width(8.dp))
                            Text("Loading matches…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                state.matches.isEmpty() -> {
                    item {
                        EmptyState(
                            title = "No matches available.",
                            subtitle = "Try refreshing or check API settings.",
                        )
                    }
                }
                else -> {
                    items(state.matches, key = { it.id }) { match ->
                        MatchCard(match)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchCard(match: NormalizedMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    match.competitionName.ifBlank { "Unknown competition" } +
                        (if (match.competitionCode.isNotBlank()) " (${match.competitionCode})" else ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                InfoChip(match.status.ifBlank { "UNKNOWN" })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    match.homeTeam.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                val scoreText = if (match.homeScore != null && match.awayScore != null) {
                    "  ${match.homeScore} : ${match.awayScore}  "
                } else {
                    "  vs  "
                }
                Text(scoreText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = StrongOrange)
                Text(
                    match.awayTeam.ifBlank { "Unknown" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }

            val dateTime = listOf(match.date, match.time).filter { it.isNotBlank() }.joinToString("  •  ")
            if (dateTime.isNotBlank()) {
                Text(dateTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
