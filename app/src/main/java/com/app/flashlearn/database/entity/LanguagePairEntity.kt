package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * یک جهت یادگیری (مثلا ES -> FA). تغییر جهت فعال هیچ داده‌ای از Concept/Content
 * را تغییر نمی‌دهد، فقط مشخص می‌کند کدام جفت زبان در UI فعال است.
 */
@Entity(
    tableName = "language_pairs",
    foreignKeys = [
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["sourceLanguage"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["targetLanguage"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceLanguage", "targetLanguage"], unique = true),
        Index(value = ["targetLanguage"])
    ]
)
data class LanguagePairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceLanguage: String,
    val targetLanguage: String,
    val isActive: Boolean = false
)
