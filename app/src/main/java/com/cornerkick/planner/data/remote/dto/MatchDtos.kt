package com.cornerkick.planner.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for the football-data.org API v4 `/matches` response. Every field is
 * nullable with a default so a missing or changed field never causes a
 * serialization crash. Only the safe subset we actually display is modelled;
 * unknown keys are ignored by the Json parser configuration.
 */

@Serializable
data class MatchesResponseDto(
    val matches: List<MatchDto>? = null,
)

@Serializable
data class MatchDto(
    val id: Long? = null,
    val utcDate: String? = null,
    val status: String? = null,
    val competition: CompetitionDto? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null,
)

@Serializable
data class CompetitionDto(
    val id: Long? = null,
    val name: String? = null,
    val code: String? = null,
)

@Serializable
data class TeamDto(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,
)

@Serializable
data class ScoreDto(
    val winner: String? = null,
    val fullTime: ScoreValueDto? = null,
)

@Serializable
data class ScoreValueDto(
    val home: Int? = null,
    val away: Int? = null,
)
