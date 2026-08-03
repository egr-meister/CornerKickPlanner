package com.cornerkick.planner.ui.navigation

/** Central place for navigation route strings. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CREATE_EDIT = "create_edit" // optional arg ?schemeId=
    const val BOARD = "board"
    const val DETAIL = "detail" // /{schemeId}
    const val HISTORY = "history"
    const val MATCH_SCHEDULE = "match_schedule"
    const val MATCH_SETTINGS = "match_settings"
    const val SETTINGS = "settings"

    fun createNew(): String = "$CREATE_EDIT?schemeId="
    fun editScheme(id: String): String = "$CREATE_EDIT?schemeId=$id"
    fun detail(id: String): String = "$DETAIL/$id"
}
