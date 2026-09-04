package com.app.flashlearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.flashlearn.presentation.add.AddConceptScreen
import com.app.flashlearn.presentation.backup.BackupRestoreScreen
import com.app.flashlearn.presentation.conceptdetail.ConceptDetailScreen
import com.app.flashlearn.presentation.home.HomeScreen
import com.app.flashlearn.presentation.onboarding.OnboardingScreen
import com.app.flashlearn.presentation.review.ReviewSessionScreen
import com.app.flashlearn.presentation.settings.AISettingsScreen
import com.app.flashlearn.presentation.settings.SettingsScreen
import com.app.flashlearn.presentation.statistics.StatisticsScreen
import com.app.flashlearn.presentation.vocabulary.VocabularyListScreen

@Composable
fun FlashLearnNavGraph(
    navController: NavHostController,
    isFirstLaunch: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) Screen.Onboarding.route else Screen.Home.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToReview = { stage -> navController.navigate(Screen.ReviewSession.createRoute(stage)) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) }
            )
        }

        composable(
            Screen.ReviewSession.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            ReviewSessionScreen(onFinished = { navController.popBackStack() })
        }

        composable(Screen.Vocabulary.route) {
            VocabularyListScreen(
                onConceptClick = { id -> navController.navigate(Screen.ConceptDetail.createRoute(id)) },
                onAddClick = { navController.navigate(Screen.AddConcept.route) }
            )
        }

        composable(
            Screen.ConceptDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            ConceptDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AddConcept.route) {
            AddConceptScreen(onSaved = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAI = { navController.navigate(Screen.AISettings.route) },
                onNavigateToBackup = { navController.navigate(Screen.BackupRestore.route) }
            )
        }

        composable(Screen.AISettings.route) { AISettingsScreen() }
        composable(Screen.BackupRestore.route) { BackupRestoreScreen() }
        composable(Screen.Statistics.route) { StatisticsScreen() }
    }
}
