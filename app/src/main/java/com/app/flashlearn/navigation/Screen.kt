package com.app.flashlearn.navigation

import com.app.flashlearn.domain.model.ReviewStage

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ReviewSession : Screen("review_session/{type}") {
        fun createRoute(type: ReviewStage) = "review_session/${type.name}"
    }
    data object Vocabulary : Screen("vocabulary")
    data object ConceptDetail : Screen("concept_detail/{id}") {
        fun createRoute(id: Long) = "concept_detail/$id"
    }
    data object AddConcept : Screen("add_concept")
    data object Settings : Screen("settings")
    data object AISettings : Screen("ai_settings")
    data object BackupRestore : Screen("backup_restore")
    data object Statistics : Screen("statistics")
    data object Onboarding : Screen("onboarding")
}
