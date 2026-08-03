package com.cornerkick.planner.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.data.model.AttackSide
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.Labels
import com.cornerkick.planner.ui.components.SectionHeader
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateEditSchemeScreen(
    appViewModel: AppViewModel,
    incomingSchemeId: String,
    onOpenBoard: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
) {
    // Initialize the draft only when needed (safe against process recreation).
    LaunchedEffect(incomingSchemeId) {
        if (incomingSchemeId.isNotBlank() && appViewModel.draft.id != incomingSchemeId) {
            appViewModel.startEditScheme(incomingSchemeId)
        } else if (incomingSchemeId.isBlank() && appViewModel.draft.id.isBlank()) {
            appViewModel.startNewScheme()
        }
    }

    val draft = appViewModel.draft
    var errorText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (appViewModel.editingExisting) "Edit scheme" else "New scheme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (appViewModel.editingExisting) {
                        IconButton(onClick = {
                            appViewModel.deleteScheme(draft.id)
                            onDeleted()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = draft.title,
                onValueChange = { appViewModel.updateTitle(it); errorText = "" },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = draft.date,
                onValueChange = { appViewModel.updateDate(it); errorText = "" },
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader("Corner type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CornerType.entries.forEach { type ->
                    FilterChip(
                        selected = draft.cornerType == type,
                        onClick = { appViewModel.updateCornerType(type) },
                        label = { Text(Labels.cornerType(type)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StrongOrange,
                            selectedLabelColor = WhiteText,
                        ),
                    )
                }
            }

            SectionHeader("Attack side")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttackSide.entries.forEach { side ->
                    FilterChip(
                        selected = draft.attackSide == side,
                        onClick = { appViewModel.updateAttackSide(side) },
                        label = { Text(Labels.attackSide(side)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StrongOrange,
                            selectedLabelColor = WhiteText,
                        ),
                    )
                }
            }

            SectionHeader("Tactical notes")
            OutlinedTextField(
                value = draft.notes,
                onValueChange = { appViewModel.updateNotes(it) },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. Near post run by striker.") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
            )

            OutlinedButton(
                onClick = onOpenBoard,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    "Open corner board  •  ${draft.pitchScheme.markers.size} markers, ${draft.pitchScheme.arrows.size} arrows",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (errorText.isNotBlank()) {
                Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    val error = appViewModel.validate()
                    if (error != null) {
                        errorText = error
                    } else {
                        appViewModel.saveDraft(onDone = onSaved)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StrongOrange, contentColor = WhiteText),
            ) {
                Text("Save scheme", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
