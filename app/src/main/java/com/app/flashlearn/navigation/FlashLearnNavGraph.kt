package com.app.flashlearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.flashlearn.presentation.home.HomeScreen
import com.app.flashlearn.presentation.review.ReviewScreen
import com.app.flashlearn.presentation.review.ReviewTypeSelectScreen
import com.app.flashlearn.presentation.vocabulary.VocabularyListScreen

@Composable
fun FlashLearnNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.ReviewTypeSelect.route) {
            ReviewTypeSelectScreen(navController)
        }
        composable(Screen.ReviewSession.route) { backStackEntry ->
            val reviewType = backStackEntry.arguments?.getString("reviewType") ?: "DAILY"
            ReviewScreen(reviewType = reviewType)
        }
        composable(Screen.VocabularyList.route) {
            VocabularyListScreen(navController)
        }
        // سایر صفحات ...
    }
}
