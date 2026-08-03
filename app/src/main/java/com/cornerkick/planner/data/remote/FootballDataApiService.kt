package com.cornerkick.planner.data.remote

import com.cornerkick.planner.data.remote.dto.MatchesResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the football-data.org API v4. Only the read-only
 * `/matches` endpoint is used. No odds, predictions, bookmaker or betting
 * endpoints are referenced anywhere. The X-Auth-Token header is added by an
 * OkHttp interceptor (see [FootballDataRepository]).
 */
interface FootballDataApiService {

    @GET("matches")
    suspend fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("competitions") competitions: String? = null,
    ): Response<MatchesResponseDto>
}
