package com.cornerkick.planner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cornerkick.planner.data.model.AttackSide
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.Labels
import com.cornerkick.planner.ui.components.EmptyState
import com.cornerkick.planner.ui.components.SchemeCard
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SchemeHistoryScreen(
    appViewModel: AppViewModel,
    onOpenScheme: (String) -> Unit,
    onBack: () -> Unit,
) {
    val appData by appViewModel.appData.collectAsState()
    var typeFilter by remember { mutableStateOf<CornerType?>(null) }
    var sideFilter by remember { mutableStateOf<AttackSide?>(null) }

    val filtered = appData.schemes
        .sortedByDescending { it.updatedAt }
        .filter { typeFilter == null || it.cornerType == typeFilter }
        .filter { sideFilter == null || it.attackSide == sideFilter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheme history") },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Filter by corner type", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = typeFilter == null,
                        onClick = { typeFilter = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                    )
                    CornerType.entries.forEach { t ->
                        FilterChip(
                            selected = typeFilter == t,
                            onClick = { typeFilter = if (typeFilter == t) null else t },
                            label = { Text(Labels.cornerType(t)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                        )
                    }
                }
            }
            item {
                Text("Filter by attack side", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = sideFilter == null,
                        onClick = { sideFilter = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                    )
                    AttackSide.entries.forEach { s ->
                        FilterChip(
                            selected = sideFilter == s,
                            onClick = { sideFilter = if (sideFilter == s) null else s },
                            label = { Text(Labels.attackSide(s)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StrongOrange, selectedLabelColor = WhiteText),
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        title = "No corner schemes yet.",
                        subtitle = "Create your first corner routine.",
                    )
                }
            } else {
                items(filtered, key = { it.id }) { scheme ->
                    SchemeRowWithMenu(
                        onOpen = { onOpenScheme(scheme.id) },
                        onEdit = { onOpenScheme(scheme.id) },
                        onDuplicate = { appViewModel.duplicateScheme(scheme.id) },
                        onDelete = { appViewModel.deleteScheme(scheme.id) },
                        content = {
                            SchemeCard(scheme = scheme, onClick = { onOpenScheme(scheme.id) })
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SchemeRowWithMenu(
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        content()
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            androidx.compose.foundation.layout.Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Scheme actions")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = { expanded = false; onOpen() })
                    DropdownMenuItem(text = { Text("Duplicate") }, onClick = { expanded = false; onDuplicate() })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
}
