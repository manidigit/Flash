package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "language_pairs",
    foreignKeys = [
        ForeignKey(entity = LanguageEntity::class, parentColumns = ["code"], childColumns = ["sourceLanguage"]),
        ForeignKey(entity = LanguageEntity::class, parentColumns = ["code"], childColumns = ["targetLanguage"])
    ]
)
data class LanguagePairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceLanguage: String,
    val targetLanguage: String,
    val isActive: Boolean = false
)
