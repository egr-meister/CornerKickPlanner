package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.components.EmptyState
import com.cornerkick.planner.ui.components.SchemeCard
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.DarkSectionBackground
import com.cornerkick.planner.ui.theme.SoftOrangePanel
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onNewScheme: () -> Unit,
    onQuickType: (CornerType) -> Unit,
    onOpenScheme: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMatchSchedule: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val appData by appViewModel.appData.collectAsState()
    val schemes = appData.schemes.sortedByDescending { it.updatedAt }
    val latest = schemes.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        // Orange header.
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StrongOrange)
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSectionBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("⚑", color = StrongOrange, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CornerKick Planner",
                                style = MaterialTheme.typography.titleLarge,
                                color = WhiteText,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                "Corner-kick tactics planner",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhiteText,
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = WhiteText)
                    }
                }
            }
        }

        // Quick actions.
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                PrimaryActionButton(text = "New Scheme", onClick = onNewScheme)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickTypeButton("Near Post", Modifier.weight(1f)) { onQuickType(CornerType.NearPost) }
                    QuickTypeButton("Far Post", Modifier.weight(1f)) { onQuickType(CornerType.FarPost) }
                    QuickTypeButton("Short", Modifier.weight(1f)) { onQuickType(CornerType.ShortCorner) }
                }
            }
        }

        // Latest schemes.
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader("Latest schemes", modifier = Modifier.weight(1f))
                Text(
                    "View all",
                    style = MaterialTheme.typography.labelLarge,
                    color = StrongOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenHistory() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        if (latest.isEmpty()) {
            item {
                EmptyState(
                    title = "No corner schemes yet.",
                    subtitle = "Create your first set-piece plan.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            items(latest, key = { it.id }) { scheme ->
                SchemeCard(
                    scheme = scheme,
                    onClick = { onOpenScheme(scheme.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }

        // Secondary Match Schedule card (kept small and secondary).
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader("Extra")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SoftOrangePanel)
                        .clickable { onOpenMatchSchedule() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSectionBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = StrongOrange)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Match Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1F1F),
                        )
                        Text(
                            "Optional reference — upcoming fixtures (today + 9 days).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(StrongOrange)
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = WhiteText)
        Spacer(Modifier.width(10.dp))
        Text(text, color = WhiteText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickTypeButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSectionBackground)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = WhiteText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}
