package com.cornerkick.planner.data.remote

import com.cornerkick.planner.BuildConfig
import com.cornerkick.planner.data.DemoData
import com.cornerkick.planner.data.model.FootballApiResult
import com.cornerkick.planner.data.model.MatchSource
import com.cornerkick.planner.data.model.NormalizedMatch
import com.cornerkick.planner.data.remote.dto.MatchDto
import com.cornerkick.planner.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * The ONLY place that talks to football-data.org. Everything is isolated here so
 * the rest of the app never sees Retrofit/OkHttp types. Key safety rules:
 *  - reads the token & base URL from BuildConfig (never hardcoded);
 *  - returns demo data when the token is missing / placeholder;
 *  - always returns a [FootballApiResult] and never throws to the caller;
 *  - never logs the raw token.
 */
class FootballDataRepository(
    private val apiToken: String = BuildConfig.FOOTBALL_DATA_API_TOKEN,
    private val baseUrl: String = BuildConfig.FOOTBALL_API_BASE_URL,
) {

    private val placeholderTokens = setOf("", "your_api_token_here", "null")

    private fun tokenConfigured(): Boolean =
        apiToken.trim().lowercase() !in placeholderTokens && apiToken.isNotBlank()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val service: FootballDataApiService? by lazy {
        try {
            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Auth-Token", apiToken)
                    .build()
                chain.proceed(request)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val contentType = "application/json".toMediaType()
            Retrofit.Builder()
                .baseUrl(normalizedBase)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(FootballDataApiService::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch matches for the given window. If [dateFrom]/[dateTo] are blank the
     * default 10-day window (today .. today+9) is used.
     */
    suspend fun fetchMatches(
        dateFrom: String,
        dateTo: String,
        competitionCode: String,
    ): FootballApiResult = withContext(Dispatchers.IO) {
        val from = if (DateUtils.isValidIso(dateFrom)) dateFrom else DateUtils.today()
        val to = if (DateUtils.isValidIso(dateTo)) dateTo else DateUtils.todayPlus(9)

        if (!tokenConfigured()) {
            return@withContext FootballApiResult(
                ok = true,
                matches = DemoData.demoMatches(),
                error = "API token is not configured. Showing demo matches.",
                usedDemoData = true,
            )
        }

        val api = service
            ?: return@withContext FootballApiResult(
                ok = false,
                matches = emptyList(),
                error = "Match service could not be initialized.",
                usedDemoData = false,
            )

        try {
            val competitions = competitionCode.trim().ifBlank { null }
            val response = api.getMatches(from, to, competitions)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    val normalized = (body?.matches ?: emptyList())
                        .mapNotNull { normalize(it) }
                    FootballApiResult(
                        ok = true,
                        matches = normalized,
                        error = "",
                        usedDemoData = false,
                    )
                }
                response.code() == 429 -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "API request limit reached. Please try again later.",
                    usedDemoData = false,
                )
                response.code() == 401 || response.code() == 403 -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "API access was rejected. Check your API token in settings.",
                    usedDemoData = false,
                )
                else -> FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = "Could not load matches (server responded ${response.code()}).",
                    usedDemoData = false,
                )
            }
        } catch (e: java.net.UnknownHostException) {
            FootballApiResult(false, emptyList(), "No internet connection.", false)
        } catch (e: java.net.SocketTimeoutException) {
            FootballApiResult(false, emptyList(), "The request timed out. Try again.", false)
        } catch (e: Exception) {
            // Covers invalid response format, serialization issues, etc.
            FootballApiResult(false, emptyList(), "Could not load the latest matches.", false)
        }
    }

    /** Convert a raw [MatchDto] to a safe [NormalizedMatch]. Returns null only if
     * there is literally nothing usable, so the list never contains junk. */
    private fun normalize(dto: MatchDto?): NormalizedMatch? {
        if (dto == null) return null
        val utc = dto.utcDate.orEmpty()
        val home = dto.homeTeam?.name?.takeIf { it.isNotBlank() } ?: "Unknown"
        val away = dto.awayTeam?.name?.takeIf { it.isNotBlank() } ?: "Unknown"
        return NormalizedMatch(
            id = dto.id?.toString() ?: (home + away + utc).hashCode().toString(),
            utcDate = utc,
            date = DateUtils.dateFromUtc(utc),
            time = DateUtils.timeFromUtc(utc),
            competitionName = dto.competition?.name?.takeIf { it.isNotBlank() } ?: "Unknown competition",
            competitionCode = dto.competition?.code.orEmpty(),
            homeTeam = home,
            awayTeam = away,
            status = dto.status?.takeIf { it.isNotBlank() } ?: "UNKNOWN",
            homeScore = dto.score?.fullTime?.home,
            awayScore = dto.score?.fullTime?.away,
            winner = dto.score?.winner.orEmpty(),
            source = MatchSource.Api,
        )
    }
}
