package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یک زبان پشتیبانی‌شده در اپلیکیشن.
 * code از استاندارد ISO 639-1 پیروی می‌کند (مثلاً "fa", "es", "en").
 */
@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey
    val code: String,
    val displayName: String,
    val isActive: Boolean = true
)
