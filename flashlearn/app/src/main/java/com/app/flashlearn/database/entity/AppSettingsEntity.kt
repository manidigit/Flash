package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تنظیمات ساده به‌صورت Key-Value (تم، تنظیمات AI بدون API Key، تنظیمات Review و ...).
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
