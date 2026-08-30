package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * تاریخچه کامل هر مرور. هرگز حذف نمی‌شود (حتی اگر Concept مرتبط بعدا حذف شود
 * conceptId را NO_ACTION نگه می‌داریم تا تاریخچه دست‌نخورده بماند؛ حذف واقعی Concept
 * در سطح Repository به‌صورت Soft-delete/Archive انجام می‌شود، نه حذف فیزیکی).
 */
@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(
            entity = ReviewSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["conceptId"]),
        Index(value = ["sessionId"]),
        Index(value = ["reviewDate"])
    ]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conceptId: Long,
    val sessionId: String? = null,
    // DAILY, WEEKLY, MONTHLY
    val reviewStage: String,
    val reviewDate: Long,
    val isCorrect: Boolean,
    val previousStatus: String,
    val newStatus: String,
    val previousDifficulty: String,
    val newDifficulty: String,
    val responseTimeMs: Long? = null
)
