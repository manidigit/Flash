package com.app.flashlearn.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ReviewTypeSelect : Screen("reviewTypeSelect")
    object ReviewSession : Screen("reviewSession/{reviewType}") {
        fun pass(reviewType: String) = "reviewSession/$reviewType"
    }
    object VocabularyList : Screen("vocabularyList")
    object AddManual : Screen("addManual")
    object Settings : Screen("settings")
}
