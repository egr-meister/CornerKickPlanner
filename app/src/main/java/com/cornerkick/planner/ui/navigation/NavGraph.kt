package com.cornerkick.planner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cornerkick.planner.ui.theme.StrongOrange
import com.cornerkick.planner.ui.theme.WhiteText
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cornerkick.planner.AppContainer
import com.cornerkick.planner.ui.AppViewModel
import com.cornerkick.planner.ui.MatchScheduleViewModel
import com.cornerkick.planner.ui.screens.CornerBoardScreen
import com.cornerkick.planner.ui.screens.CreateEditSchemeScreen
import com.cornerkick.planner.ui.screens.HomeScreen
import com.cornerkick.planner.ui.screens.MatchScheduleScreen
import com.cornerkick.planner.ui.screens.MatchScheduleSettingsScreen
import com.cornerkick.planner.ui.screens.OnboardingScreen
import com.cornerkick.planner.ui.screens.SchemeDetailScreen
import com.cornerkick.planner.ui.screens.SchemeHistoryScreen
import com.cornerkick.planner.ui.screens.SettingsScreen

@Composable
fun CornerKickNavGraph(container: AppContainer) {
    val navController = rememberNavController()

    // Shared, activity-scoped local-data ViewModel.
    val appViewModel: AppViewModel = viewModel(
        factory = AppViewModel.factory(container.appRepository)
    )
    val appData by appViewModel.appData.collectAsState()
    val loaded by appViewModel.loaded.collectAsState()

    // Wait for DataStore to load once before deciding the start destination,
    // so returning users are not briefly routed to onboarding.
    if (!loaded) {
        Box(
            modifier = Modifier.fillMaxSize().background(StrongOrange),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = WhiteText)
        }
        return
    }

    // Show onboarding first only if it has not been completed yet.
    val startDestination = if (appData.settings.onboardingCompleted) {
        Routes.HOME
    } else {
        Routes.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    appViewModel.setOnboardingCompleted(true)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                appViewModel = appViewModel,
                onNewScheme = {
                    appViewModel.startNewScheme()
                    navController.navigate(Routes.createNew())
                },
                onQuickType = { type ->
                    appViewModel.startNewScheme(type)
                    navController.navigate(Routes.createNew())
                },
                onOpenScheme = { id -> navController.navigate(Routes.detail(id)) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenMatchSchedule = { navController.navigate(Routes.MATCH_SCHEDULE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = "${Routes.CREATE_EDIT}?schemeId={schemeId}",
            arguments = listOf(navArgument("schemeId") {
                type = NavType.StringType
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val schemeId = backStackEntry.arguments?.getString("schemeId").orEmpty()
            CreateEditSchemeScreen(
                appViewModel = appViewModel,
                incomingSchemeId = schemeId,
                onOpenBoard = { navController.navigate(Routes.BOARD) },
                onSaved = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onDeleted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.BOARD) {
            CornerBoardScreen(
                appViewModel = appViewModel,
                onDone = { navController.popBackStack() },
            )
        }

        composable(
            route = "${Routes.DETAIL}/{schemeId}",
            arguments = listOf(navArgument("schemeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val schemeId = backStackEntry.arguments?.getString("schemeId").orEmpty()
            SchemeDetailScreen(
                appViewModel = appViewModel,
                schemeId = schemeId,
                onEdit = { id ->
                    appViewModel.startEditScheme(id)
                    navController.navigate(Routes.editScheme(id))
                },
                onDeleted = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.HISTORY) {
            SchemeHistoryScreen(
                appViewModel = appViewModel,
                onOpenScheme = { id -> navController.navigate(Routes.detail(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MATCH_SCHEDULE) {
            val matchViewModel: MatchScheduleViewModel = viewModel(
                factory = MatchScheduleViewModel.factory(
                    container.appRepository,
                    container.footballRepository,
                )
            )
            MatchScheduleScreen(
                viewModel = matchViewModel,
                onOpenSettings = { navController.navigate(Routes.MATCH_SETTINGS) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.MATCH_SETTINGS) {
            MatchScheduleSettingsScreen(
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                appViewModel = appViewModel,
                onOpenMatchSettings = { navController.navigate(Routes.MATCH_SETTINGS) },
                onShowOnboarding = {
                    appViewModel.setOnboardingCompleted(false)
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
