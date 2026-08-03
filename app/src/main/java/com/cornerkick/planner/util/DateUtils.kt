package com.cornerkick.planner.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Small, defensive date helpers. Everything works on plain YYYY-MM-DD strings so
 * we never depend on a native date picker. All parsing is null/exception safe.
 */
object DateUtils {

    private const val ISO_DATE = "yyyy-MM-dd"

    private fun isoFormatter(): SimpleDateFormat =
        SimpleDateFormat(ISO_DATE, Locale.US).apply { isLenient = false }

    /** Today's date as YYYY-MM-DD in the device time zone. */
    fun today(): String = isoFormatter().format(Date())

    /** Today + [days] as YYYY-MM-DD. Negative values also work. */
    fun todayPlus(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return isoFormatter().format(cal.time)
    }

    /** Add [days] to a YYYY-MM-DD string. Returns [fallback] on any problem. */
    fun addDays(dateIso: String, days: Int, fallback: String = today()): String {
        val parsed = parseOrNull(dateIso) ?: return fallback
        val cal = Calendar.getInstance()
        cal.time = parsed
        cal.add(Calendar.DAY_OF_YEAR, days)
        return isoFormatter().format(cal.time)
    }

    /** True if [dateIso] is a valid YYYY-MM-DD date. */
    fun isValidIso(dateIso: String): Boolean = parseOrNull(dateIso) != null

    private fun parseOrNull(dateIso: String): Date? {
        if (dateIso.isBlank()) return null
        return try {
            isoFormatter().parse(dateIso.trim())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns true when [from] <= [to]. If either date is invalid returns true so
     * validation focuses on the "not valid date" message instead of ordering.
     */
    fun fromNotAfterTo(from: String, to: String): Boolean {
        val f = parseOrNull(from) ?: return true
        val t = parseOrNull(to) ?: return true
        return !f.after(t)
    }

    /** Extracts YYYY-MM-DD from an ISO-8601 UTC timestamp like 2026-07-30T18:00:00Z. */
    fun dateFromUtc(utc: String): String {
        if (utc.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cleaned = utc.replace("Z", "").take(19)
            val d = parser.parse(cleaned) ?: return utc.take(10)
            val local = SimpleDateFormat(ISO_DATE, Locale.US)
            local.format(d)
        } catch (e: Exception) {
            utc.take(10)
        }
    }

    /** Extracts local HH:mm from an ISO-8601 UTC timestamp. */
    fun timeFromUtc(utc: String): String {
        if (utc.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cleaned = utc.replace("Z", "").take(19)
            val d = parser.parse(cleaned) ?: return ""
            val local = SimpleDateFormat("HH:mm", Locale.US)
            local.format(d)
        } catch (e: Exception) {
            ""
        }
    }

    /** Current timestamp for "last updated" style labels. */
    fun nowTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
}
