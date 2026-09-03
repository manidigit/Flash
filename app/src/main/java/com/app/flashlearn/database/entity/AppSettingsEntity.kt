package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val streakDays: Int = 0,
    val lastReviewDate: Long? = null,
    val appTheme: String = "DARK",
    val appLanguage: String = "fa",
    val sourceLanguage: String = "fa",
    val targetLanguage: String = "en",
    val onboardingCompleted: Boolean = false
)
