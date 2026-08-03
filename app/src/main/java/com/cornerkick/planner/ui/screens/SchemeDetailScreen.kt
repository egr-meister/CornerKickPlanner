package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.Labels
import com.cornerkick.planner.ui.components.CornerBoard
import com.cornerkick.planner.ui.components.EmptyState
import com.cornerkick.planner.ui.components.InfoChip
import com.cornerkick.planner.ui.components.MarkerColors
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SchemeDetailScreen(
    appViewModel: AppViewModel,
    schemeId: String,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    // Observe the data so the screen updates after duplicate/delete.
    val appData by appViewModel.appData.collectAsState()
    val scheme = appData.schemes.firstOrNull { it.id == schemeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheme detail") },
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
        if (scheme == null) {
            // Safe fallback for a missing/invalid id.
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
            ) {
                EmptyState(
                    title = "Scheme not found.",
                    subtitle = "It may have been deleted. Go back to see your schemes.",
                )
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
                ) { Text("Back") }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                scheme.title.ifBlank { "Untitled scheme" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(scheme.date.ifBlank { "No date" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoChip(Labels.cornerType(scheme.cornerType))
                InfoChip(Labels.attackSide(scheme.attackSide))
            }

            SectionHeader("Board preview")
            CornerBoard(
                markers = scheme.pitchScheme.markers,
                arrows = scheme.pitchScheme.arrows,
            )

            SectionHeader("Markers (${scheme.pitchScheme.markers.size})")
            if (scheme.pitchScheme.markers.isEmpty()) {
                Text("No markers in this scheme.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                scheme.pitchScheme.markers.forEach { marker ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(16.dp).clip(CircleShape).background(MarkerColors.forType(marker.type)),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            Labels.markerType(marker.type) +
                                (if (marker.label.isNotBlank()) " • ${marker.label}" else "") +
                                "  (${Labels.teamRole(marker.teamColorRole)})",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            SectionHeader("Arrows (${scheme.pitchScheme.arrows.size})")
            if (scheme.pitchScheme.arrows.isEmpty()) {
                Text("No arrows in this scheme.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                scheme.pitchScheme.arrows.forEach { arrow ->
                    Text(
                        "• " + Labels.arrowType(arrow.type) + (if (arrow.label.isNotBlank()) " — ${arrow.label}" else ""),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionHeader("Notes")
            Text(
                scheme.notes.ifBlank { "No notes." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onEdit(scheme.id) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) { Text("Edit", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                onClick = { appViewModel.duplicateScheme(scheme.id) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Duplicate") }

            OutlinedButton(
                onClick = {
                    appViewModel.deleteScheme(scheme.id)
                    onDeleted()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Delete", color = MaterialTheme.colorScheme.error) }

            Spacer(Modifier.height(16.dp))
        }
    }
}
