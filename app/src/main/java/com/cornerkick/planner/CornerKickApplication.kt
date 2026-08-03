package com.cornerkick.planner

import android.app.Application
import com.cornerkick.planner.data.local.AppDataStore
import com.cornerkick.planner.data.local.AppRepository
import com.cornerkick.planner.data.remote.FootballDataRepository

/**
 * Application class acting as a tiny manual service locator. No third-party DI
 * framework is used to keep the project simple and stable.
 */
class CornerKickApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Holds app-wide singletons. */
class AppContainer(application: Application) {
    private val dataStore = AppDataStore(application.applicationContext)
    val appRepository = AppRepository(dataStore)
    val footballRepository = FootballDataRepository()
}
