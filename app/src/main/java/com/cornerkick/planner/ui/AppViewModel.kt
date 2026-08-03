package com.cornerkick.planner.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cornerkick.planner.data.local.AppRepository
import com.cornerkick.planner.data.model.AppData
import com.cornerkick.planner.data.model.ArrowType
import com.cornerkick.planner.data.model.AttackSide
import com.cornerkick.planner.data.model.CornerArrow
import com.cornerkick.planner.data.model.CornerMarker
import com.cornerkick.planner.data.model.CornerPitchScheme
import com.cornerkick.planner.data.model.CornerScheme
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.data.model.MarkerType
import com.cornerkick.planner.data.model.Settings
import com.cornerkick.planner.data.model.TeamColorRole
import com.cornerkick.planner.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Shared ViewModel for local corner-scheme data and the in-memory editor draft.
 * The Create/Edit screen and Corner Board screen both operate on [draft].
 */
class AppViewModel(private val repository: AppRepository) : ViewModel() {

    val appData: StateFlow<AppData> = repository.appData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppData())

    /** False until the first value has been loaded from DataStore. Used to gate
     * the start-destination decision so returning users are not shown onboarding. */
    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        viewModelScope.launch {
            // Wait for the first real emission, then mark ready.
            repository.appData.first()
            _loaded.value = true
        }
    }

    /** The scheme currently being created or edited. */
    var draft by mutableStateOf(CornerScheme())
        private set

    /** True when editing an existing scheme (affects delete availability). */
    var editingExisting by mutableStateOf(false)
        private set

    // ---------------------------------------------------------------------
    // Draft lifecycle
    // ---------------------------------------------------------------------

    fun startNewScheme() {
        val defaultType = appData.value.settings.defaultCornerType ?: CornerType.NearPost
        draft = CornerScheme(
            id = UUID.randomUUID().toString(),
            title = "",
            date = DateUtils.today(),
            cornerType = defaultType,
            attackSide = AttackSide.LeftCorner,
            notes = "",
            pitchScheme = CornerPitchScheme(id = UUID.randomUUID().toString()),
        )
        editingExisting = false
    }

    fun startNewScheme(cornerType: CornerType) {
        startNewScheme()
        draft = draft.copy(cornerType = cornerType)
    }

    /** Loads an existing scheme into the draft; falls back to a new one if missing. */
    fun startEditScheme(id: String?) {
        val existing = id?.let { schemeId -> appData.value.schemes.firstOrNull { it.id == schemeId } }
        if (existing != null) {
            draft = existing
            editingExisting = true
        } else {
            startNewScheme()
        }
    }

    fun updateTitle(value: String) { draft = draft.copy(title = value) }
    fun updateDate(value: String) { draft = draft.copy(date = value) }
    fun updateCornerType(value: CornerType) { draft = draft.copy(cornerType = value) }
    fun updateAttackSide(value: AttackSide) { draft = draft.copy(attackSide = value) }
    fun updateNotes(value: String) { draft = draft.copy(notes = value) }

    // ---------------------------------------------------------------------
    // Board editing (markers & arrows on the draft)
    // ---------------------------------------------------------------------

    fun addMarker(type: MarkerType, role: TeamColorRole, label: String, x: Float, y: Float) {
        val marker = CornerMarker(
            id = UUID.randomUUID().toString(),
            type = type,
            label = label.trim(),
            x = x.coerceIn(0f, 1f),
            y = y.coerceIn(0f, 1f),
            teamColorRole = role,
        )
        val scheme = draft.pitchScheme
        draft = draft.copy(pitchScheme = scheme.copy(markers = scheme.markers + marker))
    }

    fun updateMarkerLabel(id: String, label: String) {
        val scheme = draft.pitchScheme
        val updated = scheme.markers.map { if (it.id == id) it.copy(label = label.trim()) else it }
        draft = draft.copy(pitchScheme = scheme.copy(markers = updated))
    }

    fun deleteMarker(id: String) {
        val scheme = draft.pitchScheme
        draft = draft.copy(pitchScheme = scheme.copy(markers = scheme.markers.filterNot { it.id == id }))
    }

    fun addArrow(type: ArrowType, label: String, startX: Float, startY: Float, endX: Float, endY: Float) {
        val arrow = CornerArrow(
            id = UUID.randomUUID().toString(),
            type = type,
            startX = startX.coerceIn(0f, 1f),
            startY = startY.coerceIn(0f, 1f),
            endX = endX.coerceIn(0f, 1f),
            endY = endY.coerceIn(0f, 1f),
            label = label.trim(),
        )
        val scheme = draft.pitchScheme
        draft = draft.copy(pitchScheme = scheme.copy(arrows = scheme.arrows + arrow))
    }

    fun deleteArrow(id: String) {
        val scheme = draft.pitchScheme
        draft = draft.copy(pitchScheme = scheme.copy(arrows = scheme.arrows.filterNot { it.id == id }))
    }

    fun clearBoard() {
        val scheme = draft.pitchScheme
        draft = draft.copy(pitchScheme = scheme.copy(markers = emptyList(), arrows = emptyList()))
    }

    // ---------------------------------------------------------------------
    // Validation & save
    // ---------------------------------------------------------------------

    /** Returns an error message, or null when the draft is valid and saved. */
    fun validate(): String? {
        if (draft.title.isBlank()) return "Please enter a title for this scheme."
        if (!DateUtils.isValidIso(draft.date)) return "Please enter a valid date (YYYY-MM-DD)."
        val hasMarkers = draft.pitchScheme.markers.isNotEmpty()
        val hasNotes = draft.notes.isNotBlank()
        if (!hasMarkers && !hasNotes) {
            return "Add at least one marker or some notes before saving."
        }
        return null
    }

    fun saveDraft(onDone: () -> Unit = {}) {
        val error = validate()
        if (error != null) return
        viewModelScope.launch {
            repository.upsertScheme(draft)
            onDone()
        }
    }

    // ---------------------------------------------------------------------
    // History operations
    // ---------------------------------------------------------------------

    fun getScheme(id: String?): CornerScheme? =
        id?.let { schemeId -> appData.value.schemes.firstOrNull { it.id == schemeId } }

    fun deleteScheme(id: String) {
        viewModelScope.launch { repository.deleteScheme(id) }
    }

    fun duplicateScheme(id: String) {
        viewModelScope.launch { repository.duplicateScheme(id) }
    }

    fun deleteAllSchemes() {
        viewModelScope.launch { repository.deleteAllSchemes() }
    }

    // ---------------------------------------------------------------------
    // Settings
    // ---------------------------------------------------------------------

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { repository.setOnboardingCompleted(completed) }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    fun clearMatchCache() {
        viewModelScope.launch { repository.clearMatchCache() }
    }

    fun resetAllLocalData() {
        viewModelScope.launch { repository.resetAllLocalData() }
    }

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AppViewModel(repository) as T
            }
    }
}
