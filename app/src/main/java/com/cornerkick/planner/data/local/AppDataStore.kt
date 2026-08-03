package com.cornerkick.planner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cornerkick.planner.data.model.AppData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Single DataStore instance for the whole app. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cornerkick_planner")

/**
 * Stores the entire [AppData] object as a single JSON string in Preferences
 * DataStore. Every read is wrapped so corrupted / missing JSON falls back to a
 * fresh default [AppData] instead of crashing. Unknown JSON keys are ignored,
 * and missing keys use the data-class defaults, so schema changes are safe.
 */
class AppDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    private val appDataKey = stringPreferencesKey("app_data_json")

    val appData: Flow<AppData> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[appDataKey]
            decode(raw)
        }

    private fun decode(raw: String?): AppData {
        if (raw.isNullOrBlank()) return AppData()
        return try {
            json.decodeFromString(AppData.serializer(), raw)
        } catch (e: Exception) {
            // Corrupted JSON — return safe defaults so the app still starts.
            AppData()
        }
    }

    /** Read the current value once (used when we only need a snapshot). */
    suspend fun currentSnapshot(current: AppData): AppData = current

    /** Persist a full [AppData] object. */
    suspend fun save(data: AppData) {
        val encoded = try {
            json.encodeToString(AppData.serializer(), data)
        } catch (e: Exception) {
            // If encoding ever fails, store an empty default rather than crash.
            json.encodeToString(AppData.serializer(), AppData())
        }
        context.dataStore.edit { prefs ->
            prefs[appDataKey] = encoded
        }
    }

    /** Atomically update stored data using the previous value. */
    suspend fun update(transform: (AppData) -> AppData) {
        context.dataStore.edit { prefs ->
            val current = decode(prefs[appDataKey])
            val updated = try {
                transform(current)
            } catch (e: Exception) {
                current
            }
            prefs[appDataKey] = try {
                json.encodeToString(AppData.serializer(), updated)
            } catch (e: Exception) {
                json.encodeToString(AppData.serializer(), current)
            }
        }
    }
}
