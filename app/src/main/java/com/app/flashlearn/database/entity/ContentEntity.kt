package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** بدون Unique روی (conceptId, languageCode): یک کلمه می‌تواند چند معنی در یک زبان داشته باشد (بند 64). */
@Entity(
    tableName = "contents",
    foreignKeys = [
        ForeignKey(entity = ConceptEntity::class, parentColumns = ["id"], childColumns = ["conceptId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = LanguageEntity::class, parentColumns = ["code"], childColumns = ["languageCode"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)
    ],
    indices = [
        Index("conceptId", "languageCode"),
        Index("languageCode"),
        Index("text")
    ]
)
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conceptId: Long,
    val languageCode: String,
    val text: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val grammarNote: String? = null,
    val usageNote: String? = null
)
