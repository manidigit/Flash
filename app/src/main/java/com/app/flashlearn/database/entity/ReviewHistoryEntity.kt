package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(entity = ConceptEntity::class, parentColumns = ["id"], childColumns = ["conceptId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = ReviewSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.SET_NULL, onUpdate = ForeignKey.CASCADE)
    ],
    indices = [
        Index("conceptId"),
        Index("sessionId"),
        Index("reviewDate")
    ]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conceptId: Long,
    val sessionId: String?,
    val reviewStage: String,
    val reviewDate: Long,
    val isCorrect: Boolean,
    val previousStatus: String,
    val newStatus: String,
    val previousDifficulty: String,
    val newDifficulty: String,
    val responseTimeMs: Long?
)
