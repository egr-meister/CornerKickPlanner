package com.cornerkick.planner.data

import com.cornerkick.planner.data.model.MatchSource
import com.cornerkick.planner.data.model.NormalizedMatch
import com.cornerkick.planner.util.DateUtils

/**
 * Built-in demo match data. Used when there is no API token, no cache and the
 * API cannot be reached. Team and competition names are plain generic text —
 * no official club/league logos or branding are used anywhere in this app.
 */
object DemoData {

    fun demoMatches(): List<NormalizedMatch> {
        val d0 = DateUtils.today()
        val d1 = DateUtils.todayPlus(1)
        val d3 = DateUtils.todayPlus(3)
        val d5 = DateUtils.todayPlus(5)
        val d8 = DateUtils.todayPlus(8)
        return listOf(
            NormalizedMatch(
                id = "demo-1",
                utcDate = "${d0}T18:00:00Z",
                date = d0,
                time = "18:00",
                competitionName = "Demo League",
                competitionCode = "DEMO",
                homeTeam = "North City FC",
                awayTeam = "River Town",
                status = "SCHEDULED",
                homeScore = null,
                awayScore = null,
                winner = "",
                source = MatchSource.Demo,
            ),
            NormalizedMatch(
                id = "demo-2",
                utcDate = "${d1}T20:45:00Z",
                date = d1,
                time = "20:45",
                competitionName = "Demo League",
                competitionCode = "DEMO",
                homeTeam = "Harbor United",
                awayTeam = "Valley Rovers",
                status = "SCHEDULED",
                homeScore = null,
                awayScore = null,
                winner = "",
                source = MatchSource.Demo,
            ),
            NormalizedMatch(
                id = "demo-3",
                utcDate = "${d3}T16:30:00Z",
                date = d3,
                time = "16:30",
                competitionName = "Demo Cup",
                competitionCode = "DCUP",
                homeTeam = "Eastside Athletic",
                awayTeam = "Lakeside FC",
                status = "SCHEDULED",
                homeScore = null,
                awayScore = null,
                winner = "",
                source = MatchSource.Demo,
            ),
            NormalizedMatch(
                id = "demo-4",
                utcDate = "${d5}T14:00:00Z",
                date = d5,
                time = "14:00",
                competitionName = "Demo League",
                competitionCode = "DEMO",
                homeTeam = "Old Mill Town",
                awayTeam = "South Park Rangers",
                status = "SCHEDULED",
                homeScore = null,
                awayScore = null,
                winner = "",
                source = MatchSource.Demo,
            ),
            NormalizedMatch(
                id = "demo-5",
                utcDate = "${d8}T19:15:00Z",
                date = d8,
                time = "19:15",
                competitionName = "Demo Cup",
                competitionCode = "DCUP",
                homeTeam = "Central Wanderers",
                awayTeam = "Coastline City",
                status = "SCHEDULED",
                homeScore = null,
                awayScore = null,
                winner = "",
                source = MatchSource.Demo,
            ),
        )
    }
}
