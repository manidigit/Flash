package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "language")
data class LanguageEntity(
    @PrimaryKey
    val code: String, // fa, en, es
    val displayName: String,
    val flagEmoji: String // 🇮🇷, 🇬🇧, 🇪🇸
)
