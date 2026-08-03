package com.cornerkick.planner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cornerkick.planner.data.local.AppRepository
import com.cornerkick.planner.data.model.MatchScheduleSettings
import com.cornerkick.planner.data.model.MatchSource
import com.cornerkick.planner.data.model.NormalizedMatch
import com.cornerkick.planner.data.remote.FootballDataRepository
import com.cornerkick.planner.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** UI state for the Match Schedule screen. */
data class MatchScheduleUiState(
    val loading: Boolean = false,
    val matches: List<NormalizedMatch> = emptyList(),
    val message: String = "",
    val isWarning: Boolean = false,
    val source: MatchSource = MatchSource.Demo,
    val lastUpdatedAt: String = "",
    val dateFrom: String = "",
    val dateTo: String = "",
    val usingDefaultWindow: Boolean = true,
)

class MatchScheduleViewModel(
    private val appRepository: AppRepository,
    private val footballRepository: FootballDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchScheduleUiState(loading = true))
    val uiState: StateFlow<MatchScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun resolveWindow(settings: MatchScheduleSettings): Pair<String, String> {
        val from = if (DateUtils.isValidIso(settings.dateFrom)) settings.dateFrom else DateUtils.today()
        val to = if (DateUtils.isValidIso(settings.dateTo)) settings.dateTo else DateUtils.todayPlus(9)
        return from to to
    }

    private fun isDefaultWindow(settings: MatchScheduleSettings): Boolean =
        settings.dateFrom.isBlank() && settings.dateTo.isBlank()

    /** On first open: show cache immediately, then refresh once if cache is old/empty. */
    private fun loadInitial() {
        viewModelScope.launch {
            val data = appRepository.appData.first()
            val settings = data.settings.matchSchedule
            val cache = data.matchScheduleCache
            val (from, to) = resolveWindow(settings)

            if (cache.cachedMatches.isNotEmpty()) {
                _uiState.value = MatchScheduleUiState(
                    loading = false,
                    matches = cache.cachedMatches,
                    message = if (cache.lastError.isNotBlank()) cache.lastError else "",
                    isWarning = cache.lastError.isNotBlank(),
                    source = MatchSource.Cache,
                    lastUpdatedAt = cache.lastUpdatedAt,
                    dateFrom = from,
                    dateTo = to,
                    usingDefaultWindow = isDefaultWindow(settings),
                )
                // Refresh only if the cache window differs or cache is stale (once per open).
                if (cache.lastDateFrom != from || cache.lastDateTo != to) {
                    refresh()
                }
            } else {
                refresh()
            }
        }
    }

    /** Manual refresh using the currently configured window. */
    fun refresh() {
        viewModelScope.launch {
            val data = appRepository.appData.first()
            val settings = data.settings.matchSchedule
            val (from, to) = resolveWindow(settings)
            _uiState.value = _uiState.value.copy(
                loading = true,
                dateFrom = from,
                dateTo = to,
                usingDefaultWindow = isDefaultWindow(settings),
            )

            // Respect the user's toggles.
            if (!settings.apiEnabled || settings.useDemoData) {
                val demo = com.cornerkick.planner.data.DemoData.demoMatches()
                appRepository.saveMatchCache(demo, from, to, "")
                _uiState.value = MatchScheduleUiState(
                    loading = false,
                    matches = demo,
                    message = if (settings.useDemoData)
                        "Demo data is enabled in settings."
                    else
                        "Match API is turned off in settings. Showing demo matches.",
                    isWarning = false,
                    source = MatchSource.Demo,
                    lastUpdatedAt = DateUtils.nowTimestamp(),
                    dateFrom = from,
                    dateTo = to,
                    usingDefaultWindow = isDefaultWindow(settings),
                )
                return@launch
            }

            val result = footballRepository.fetchMatches(from, to, settings.competitionCode)

            when {
                result.ok && result.usedDemoData -> {
                    appRepository.saveMatchCache(result.matches, from, to, result.error)
                    _uiState.value = MatchScheduleUiState(
                        loading = false,
                        matches = result.matches,
                        message = result.error,
                        isWarning = false,
                        source = MatchSource.Demo,
                        lastUpdatedAt = DateUtils.nowTimestamp(),
                        dateFrom = from,
                        dateTo = to,
                        usingDefaultWindow = isDefaultWindow(settings),
                    )
                }
                result.ok -> {
                    appRepository.saveMatchCache(result.matches, from, to, "")
                    _uiState.value = MatchScheduleUiState(
                        loading = false,
                        matches = result.matches,
                        message = if (result.matches.isEmpty())
                            "No matches available for this window."
                        else "",
                        isWarning = false,
                        source = MatchSource.Api,
                        lastUpdatedAt = DateUtils.nowTimestamp(),
                        dateFrom = from,
                        dateTo = to,
                        usingDefaultWindow = isDefaultWindow(settings),
                    )
                }
                else -> {
                    // API failed. Fall back to cache, then demo.
                    val cache = data.matchScheduleCache
                    if (cache.cachedMatches.isNotEmpty()) {
                        _uiState.value = MatchScheduleUiState(
                            loading = false,
                            matches = cache.cachedMatches,
                            message = "Could not load the latest matches. Showing cached data. (${result.error})",
                            isWarning = true,
                            source = MatchSource.Cache,
                            lastUpdatedAt = cache.lastUpdatedAt,
                            dateFrom = from,
                            dateTo = to,
                            usingDefaultWindow = isDefaultWindow(settings),
                        )
                    } else {
                        val demo = com.cornerkick.planner.data.DemoData.demoMatches()
                        _uiState.value = MatchScheduleUiState(
                            loading = false,
                            matches = demo,
                            message = "Could not load the latest matches. Showing demo data. (${result.error})",
                            isWarning = true,
                            source = MatchSource.Demo,
                            lastUpdatedAt = DateUtils.nowTimestamp(),
                            dateFrom = from,
                            dateTo = to,
                            usingDefaultWindow = isDefaultWindow(settings),
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(
            appRepository: AppRepository,
            footballRepository: FootballDataRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MatchScheduleViewModel(appRepository, footballRepository) as T
        }
    }
}
