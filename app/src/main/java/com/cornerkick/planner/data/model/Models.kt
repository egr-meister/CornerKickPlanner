package com.cornerkick.planner.data.model

import kotlinx.serialization.Serializable

/**
 * All app data models. Every class is annotated with @Serializable so it can be
 * stored as a JSON string in DataStore and read back safely. Enums use safe
 * fallbacks (see [safeCornerType] etc.) so unknown values never crash the app.
 */

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

@Serializable
enum class CornerType { NearPost, FarPost, ShortCorner, Custom }

@Serializable
enum class AttackSide { LeftCorner, RightCorner }

@Serializable
enum class MarkerType { Attacker, Defender, Goalkeeper, Ball, Target }

@Serializable
enum class TeamColorRole { Attack, Defense, Neutral }

@Serializable
enum class ArrowType { Run, Pass, Cross, DummyMove, PressingArrow }

@Serializable
enum class MatchSource { Api, Cache, Demo }

// ---------------------------------------------------------------------------
// Corner scheme models
// ---------------------------------------------------------------------------

@Serializable
data class CornerMarker(
    val id: String = "",
    val type: MarkerType = MarkerType.Attacker,
    val label: String = "",
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val teamColorRole: TeamColorRole = TeamColorRole.Neutral,
)

@Serializable
data class CornerArrow(
    val id: String = "",
    val type: ArrowType = ArrowType.Run,
    val startX: Float = 0.3f,
    val startY: Float = 0.3f,
    val endX: Float = 0.6f,
    val endY: Float = 0.6f,
    val label: String = "",
)

@Serializable
data class CornerPitchScheme(
    val id: String = "",
    val markers: List<CornerMarker> = emptyList(),
    val arrows: List<CornerArrow> = emptyList(),
)

@Serializable
data class CornerScheme(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val cornerType: CornerType = CornerType.NearPost,
    val attackSide: AttackSide = AttackSide.LeftCorner,
    val notes: String = "",
    val pitchScheme: CornerPitchScheme = CornerPitchScheme(),
    val createdAt: String = "",
    val updatedAt: String = "",
)

// ---------------------------------------------------------------------------
// Match schedule models
// ---------------------------------------------------------------------------

@Serializable
data class NormalizedMatch(
    val id: String = "",
    val utcDate: String = "",
    val date: String = "",
    val time: String = "",
    val competitionName: String = "",
    val competitionCode: String = "",
    val homeTeam: String = "",
    val awayTeam: String = "",
    val status: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winner: String = "",
    val source: MatchSource = MatchSource.Demo,
)

@Serializable
data class MatchScheduleSettings(
    val apiEnabled: Boolean = true,
    val useDemoData: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val competitionCode: String = "",
)

@Serializable
data class Settings(
    val onboardingCompleted: Boolean = false,
    val compactMode: Boolean = false,
    val defaultCornerType: CornerType? = null,
    val matchSchedule: MatchScheduleSettings = MatchScheduleSettings(),
)

@Serializable
data class MatchScheduleCache(
    val cachedMatches: List<NormalizedMatch> = emptyList(),
    val lastUpdatedAt: String = "",
    val lastError: String = "",
    val lastDateFrom: String = "",
    val lastDateTo: String = "",
)

@Serializable
data class AppData(
    val schemes: List<CornerScheme> = emptyList(),
    val settings: Settings = Settings(),
    val matchScheduleCache: MatchScheduleCache = MatchScheduleCache(),
)

// ---------------------------------------------------------------------------
// Result object returned by the football-data.org repository
// ---------------------------------------------------------------------------

data class FootballApiResult(
    val ok: Boolean,
    val matches: List<NormalizedMatch>,
    val error: String,
    val usedDemoData: Boolean,
)
