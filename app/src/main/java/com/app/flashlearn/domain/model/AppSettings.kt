package com.app.flashlearn.domain.model

data class AppSettings(
    val streakDays: Int = 0,
    val lastReviewDate: Long? = null,
    val appTheme: String = "DARK",
    val appLanguage: String = "fa",
    val sourceLanguage: String = "fa",
    val targetLanguage: String = "en"
)
