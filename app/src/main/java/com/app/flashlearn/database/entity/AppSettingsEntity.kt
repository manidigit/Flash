package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 0, // فقط یک رکورد
    val streakDays: Int = 0, // روز پیوسته
    val lastReviewDate: Long? = null, // آخرین روزی که تمرین شد
    val appTheme: String = "DARK", // LIGHT / DARK / SYSTEM
    val appLanguage: String = "fa", // زبان رابط کاربری
    val sourceLanguage: String = "fa",
    val targetLanguage: String = "en"
)
