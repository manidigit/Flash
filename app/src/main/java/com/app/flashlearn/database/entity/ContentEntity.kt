package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * متن یک Concept در یک زبان مشخص (چه زبان مبدا چه مقصد؛ از نظر Schema فرقی ندارند).
 * هر Concept حداکثر یک رکورد Content به‌ازای هر زبان دارد (Index یکتای ترکیبی).
 */
@Entity(
    tableName = "contents",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LanguageEntity::class,
            parentColumns = ["code"],
            childColumns = ["languageCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conceptId", "languageCode"], unique = true),
        Index(value = ["languageCode"]),
        Index(value = ["text"])
    ]
)
data class ContentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conceptId: Long,
    val languageCode: String,
    val text: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val grammarNote: String? = null,
    val usageNote: String? = null
)
