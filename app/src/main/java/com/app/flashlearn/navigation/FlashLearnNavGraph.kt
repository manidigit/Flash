package com.app.flashlearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.presentation.add.AddAIScreen
import com.app.flashlearn.presentation.add.AddHomeScreen
import com.app.flashlearn.presentation.add.AddImportFileScreen
import com.app.flashlearn.presentation.add.AddManualScreen
import com.app.flashlearn.presentation.add.AddPasteTextScreen
import com.app.flashlearn.presentation.home.HomeScreen
import com.app.flashlearn.presentation.onboarding.OnboardingScreen
import com.app.flashlearn.presentation.review.ReviewSessionScreen
import com.app.flashlearn.presentation.review.ReviewTypeSelectScreen
import com.app.flashlearn.presentation.settings.AISettingsScreen
import com.app.flashlearn.presentation.settings.BackupRestoreScreen
import com.app.flashlearn.presentation.settings.SettingsHomeScreen
import com.app.flashlearn.presentation.statistics.StatisticsScreen
import com.app.flashlearn.presentation.vocabulary.ConceptDetailScreen
import com.app.flashlearn.presentation.vocabulary.DuplicateWordsScreen
import com.app.flashlearn.presentation.vocabulary.VocabularyListScreen

@Composable
fun FlashLearnNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onStartReview = { stage ->
                    navController.navigate(Routes.reviewSession(stage.name))
                },
                onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                onAddWord = { navController.navigate(Routes.ADD_HOME) }
            )
        }

        composable(Routes.REVIEW_TYPE_SELECT) {
            ReviewTypeSelectScreen(
                onStart = { type, categoryId, mode ->
                    navController.navigate(Routes.reviewSession(type, categoryId, mode))
                }
            )
        }

        composable(
            route = Routes.REVIEW_SESSION,
            arguments = listOf(
                navArgument("reviewType") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType; defaultValue = "" },
                navArgument("reviewMode") { type = NavType.StringType; defaultValue = "FLASHCARD" }
            )
        ) { backStackEntry ->
            val reviewType = backStackEntry.arguments?.getString("reviewType") ?: LearningStage.DAILY.name
            ReviewSessionScreen(
                reviewType = reviewType,
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBackToTypeSelect = { navController.popBackStack() }
            )
        }

        composable(Routes.VOCABULARY_LIST) {
            VocabularyListScreen(
                onConceptClick = { conceptId -> navController.navigate(Routes.conceptDetail(conceptId)) },
                onDuplicateWordsClick = { navController.navigate(Routes.DUPLICATE_WORDS) }
            )
        }

        composable(Routes.DUPLICATE_WORDS) {
            DuplicateWordsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.CONCEPT_DETAIL,
            arguments = listOf(navArgument("conceptId") { type = NavType.LongType })
        ) {
            ConceptDetailScreen(
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_HOME) {
            AddHomeScreen(
                onManualSelected = { navController.navigate(Routes.ADD_MANUAL) },
                onAISelected = { navController.navigate(Routes.ADD_AI) },
                onPasteTextSelected = { navController.navigate(Routes.ADD_PASTE_TEXT) },
                onImportFileSelected = { navController.navigate(Routes.ADD_IMPORT_FILE) }
            )
        }

        composable(Routes.ADD_MANUAL) {
            AddManualScreen(
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_AI) {
            AddAIScreen(
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_PASTE_TEXT) {
            AddPasteTextScreen(
                onImported = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_IMPORT_FILE) {
            AddImportFileScreen(
                onImported = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_HOME) {
            SettingsHomeScreen(
                onChangeLanguagePair = { navController.navigate(Routes.ONBOARDING) },
                onOpenBackupRestore = { navController.navigate(Routes.BACKUP_RESTORE) },
                onOpenAISettings = { navController.navigate(Routes.AI_SETTINGS) }
            )
        }

        composable(Routes.BACKUP_RESTORE) {
            BackupRestoreScreen()
        }

        composable(Routes.AI_SETTINGS) {
            AISettingsScreen()
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen()
        }
    }
}
