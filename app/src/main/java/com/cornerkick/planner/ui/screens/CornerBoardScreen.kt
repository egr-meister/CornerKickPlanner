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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.data.model.ArrowType
import com.cornerkick.planner.data.model.MarkerType
import com.cornerkick.planner.data.model.TeamColorRole
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.Labels
import com.cornerkick.planner.ui.components.CornerBoard
import com.cornerkick.planner.ui.components.MarkerColors
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

private sealed interface PlacementMode {
    data object None : PlacementMode
    data class AwaitMarker(val type: MarkerType, val role: TeamColorRole, val label: String) : PlacementMode
    data class AwaitArrowStart(val type: ArrowType, val label: String) : PlacementMode
    data class AwaitArrowEnd(val type: ArrowType, val label: String, val startX: Float, val startY: Float) : PlacementMode
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CornerBoardScreen(
    appViewModel: AppViewModel,
    onDone: () -> Unit,
) {
    val draft = appViewModel.draft
    var placement by remember { mutableStateOf<PlacementMode>(PlacementMode.None) }
    var showMarkerSheet by remember { mutableStateOf(false) }
    var showArrowSheet by remember { mutableStateOf(false) }
    val markerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val arrowSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hint = when (val p = placement) {
        is PlacementMode.AwaitMarker -> "Tap the board to place the ${Labels.markerType(p.type)} marker."
        is PlacementMode.AwaitArrowStart -> "Tap the board to set the arrow START point."
        is PlacementMode.AwaitArrowEnd -> "Tap the board to set the arrow END point."
        PlacementMode.None -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Corner board") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
            if (hint.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StrongOrange)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(hint, color = WhiteText, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { placement = PlacementMode.None }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = WhiteText)
                    }
                }
            }

            CornerBoard(
                markers = draft.pitchScheme.markers,
                arrows = draft.pitchScheme.arrows,
                onTap = { x, y ->
                    when (val p = placement) {
                        is PlacementMode.AwaitMarker -> {
                            appViewModel.addMarker(p.type, p.role, p.label, x, y)
                            placement = PlacementMode.None
                        }
                        is PlacementMode.AwaitArrowStart -> {
                            placement = PlacementMode.AwaitArrowEnd(p.type, p.label, x, y)
                        }
                        is PlacementMode.AwaitArrowEnd -> {
                            appViewModel.addArrow(p.type, p.label, p.startX, p.startY, x, y)
                            placement = PlacementMode.None
                        }
                        PlacementMode.None -> Unit
                    }
                },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showMarkerSheet = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
                ) { Text("Add marker", fontWeight = FontWeight.SemiBold) }
                Button(
                    onClick = { showArrowSheet = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252525), contentColor = WhiteText),
                ) { Text("Add arrow", fontWeight = FontWeight.SemiBold) }
            }
            OutlinedButton(
                onClick = { appViewModel.clearBoard() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Clear board") }

            // Markers list.
            SectionHeader("Markers (${draft.pitchScheme.markers.size})")
            if (draft.pitchScheme.markers.isEmpty()) {
                Text(
                    "No markers yet. Add your first corner marker.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                draft.pitchScheme.markers.forEach { marker ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MarkerColors.forType(marker.type)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                Labels.markerType(marker.type) + (if (marker.label.isNotBlank()) " • ${marker.label}" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                Labels.teamRole(marker.teamColorRole),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { appViewModel.deleteMarker(marker.id) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete marker")
                        }
                    }
                }
            }

            // Arrows list.
            SectionHeader("Arrows (${draft.pitchScheme.arrows.size})")
            if (draft.pitchScheme.arrows.isEmpty()) {
                Text(
                    "No arrows yet. Add a movement arrow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                draft.pitchScheme.arrows.forEach { arrow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                Labels.arrowType(arrow.type) + (if (arrow.label.isNotBlank()) " • ${arrow.label}" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        IconButton(onClick = { appViewModel.deleteArrow(arrow.id) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete arrow")
                        }
                    }
                }
            }

            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) { Text("Done", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showMarkerSheet) {
        MarkerPickerSheet(
            sheetState = markerSheetState,
            onDismiss = { showMarkerSheet = false },
            onPlaceByTap = { type, role, label ->
                showMarkerSheet = false
                placement = PlacementMode.AwaitMarker(type, role, label)
            },
        )
    }

    if (showArrowSheet) {
        ArrowPickerSheet(
            sheetState = arrowSheetState,
            onDismiss = { showArrowSheet = false },
            onPlaceByTap = { type, label ->
                showArrowSheet = false
                placement = PlacementMode.AwaitArrowStart(type, label)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MarkerPickerSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onPlaceByTap: (MarkerType, TeamColorRole, String) -> Unit,
) {
    var type by remember { mutableStateOf(MarkerType.Attacker) }
    var role by remember { mutableStateOf(Labels.defaultRole(MarkerType.Attacker)) }
    var label by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add marker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SectionHeader("Marker type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkerType.entries.forEach { m ->
                    FilterChip(
                        selected = type == m,
                        onClick = { type = m; role = Labels.defaultRole(m) },
                        label = { Text(Labels.markerType(m)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText,
                        ),
                    )
                }
            }

            SectionHeader("Team role")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TeamColorRole.entries.forEach { r ->
                    FilterChip(
                        selected = role == r,
                        onClick = { role = r },
                        label = { Text(Labels.teamRole(r)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText,
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(8) },
                label = { Text("Short label (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { onPlaceByTap(type, role, label) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) { Text("Place on board (tap a spot)", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ArrowPickerSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onPlaceByTap: (ArrowType, String) -> Unit,
) {
    var type by remember { mutableStateOf(ArrowType.Run) }
    var label by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add arrow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SectionHeader("Arrow type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArrowType.entries.forEach { a ->
                    FilterChip(
                        selected = type == a,
                        onClick = { type = a },
                        label = { Text(Labels.arrowType(a)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText,
                        ),
                    )
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(20) },
                label = { Text("Label (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { onPlaceByTap(type, label) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) { Text("Set start & end (tap two spots)", fontWeight = FontWeight.Bold) }
        }
    }
}
