package com.cornerkick.planner.data.local

import com.cornerkick.planner.data.model.AppData
import com.cornerkick.planner.data.model.CornerScheme
import com.cornerkick.planner.data.model.MatchScheduleCache
import com.cornerkick.planner.data.model.MatchScheduleSettings
import com.cornerkick.planner.data.model.NormalizedMatch
import com.cornerkick.planner.data.model.Settings
import com.cornerkick.planner.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * The single repository for all LOCAL app data (schemes + settings + match
 * cache). It is a thin, safe wrapper over [AppDataStore]. Every mutation is
 * defensive and never throws into the UI layer.
 */
class AppRepository(private val store: AppDataStore) {

    val appData: Flow<AppData> = store.appData

    // ---------------------------------------------------------------------
    // Scheme CRUD
    // ---------------------------------------------------------------------

    suspend fun upsertScheme(scheme: CornerScheme) {
        store.update { data ->
            val now = DateUtils.nowTimestamp()
            val existingIndex = data.schemes.indexOfFirst { it.id == scheme.id }
            val normalized = scheme.copy(
                id = scheme.id.ifBlank { newId() },
                title = scheme.title.trim(),
                date = scheme.date.ifBlank { DateUtils.today() },
                createdAt = scheme.createdAt.ifBlank { now },
                updatedAt = now,
            )
            val newList = if (existingIndex >= 0) {
                data.schemes.toMutableList().also { it[existingIndex] = normalized }
            } else {
                data.schemes + normalized
            }
            data.copy(schemes = newList)
        }
    }

    suspend fun deleteScheme(id: String) {
        store.update { data ->
            data.copy(schemes = data.schemes.filterNot { it.id == id })
        }
    }

    suspend fun duplicateScheme(id: String) {
        store.update { data ->
            val original = data.schemes.firstOrNull { it.id == id } ?: return@update data
            val now = DateUtils.nowTimestamp()
            val copy = original.copy(
                id = newId(),
                title = (original.title.ifBlank { "Corner scheme" } + " (copy)").take(80),
                createdAt = now,
                updatedAt = now,
            )
            data.copy(schemes = data.schemes + copy)
        }
    }

    suspend fun deleteAllSchemes() {
        store.update { it.copy(schemes = emptyList()) }
    }

    // ---------------------------------------------------------------------
    // Settings
    // ---------------------------------------------------------------------

    suspend fun updateSettings(transform: (Settings) -> Settings) {
        store.update { data -> data.copy(settings = transform(data.settings)) }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        updateSettings { it.copy(onboardingCompleted = completed) }
    }

    suspend fun updateMatchScheduleSettings(transform: (MatchScheduleSettings) -> MatchScheduleSettings) {
        store.update { data ->
            data.copy(settings = data.settings.copy(matchSchedule = transform(data.settings.matchSchedule)))
        }
    }

    // ---------------------------------------------------------------------
    // Match schedule cache
    // ---------------------------------------------------------------------

    suspend fun saveMatchCache(
        matches: List<NormalizedMatch>,
        dateFrom: String,
        dateTo: String,
        error: String,
    ) {
        store.update { data ->
            data.copy(
                matchScheduleCache = MatchScheduleCache(
                    cachedMatches = matches,
                    lastUpdatedAt = DateUtils.nowTimestamp(),
                    lastError = error,
                    lastDateFrom = dateFrom,
                    lastDateTo = dateTo,
                )
            )
        }
    }

    suspend fun clearMatchCache() {
        store.update { it.copy(matchScheduleCache = MatchScheduleCache()) }
    }

    // ---------------------------------------------------------------------
    // Global reset
    // ---------------------------------------------------------------------

    suspend fun resetAllLocalData() {
        store.save(AppData())
    }

    private fun newId(): String = UUID.randomUUID().toString()
}
