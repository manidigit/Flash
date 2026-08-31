package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "language_pair",
    foreignKeys = [
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["sourceLanguage"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["targetLanguage"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sourceLanguage"),
        Index("targetLanguage")
    ]
)
data class LanguagePairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceLanguage: String, // fa, en, es
    val targetLanguage: String, // fa, en, es
    val isActive: Boolean = false
)
