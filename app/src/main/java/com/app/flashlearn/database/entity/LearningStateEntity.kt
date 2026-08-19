package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * وضعیت مرور یک Concept. رابطه یک‌به‌یک با ConceptEntity.
 * stage و difficulty عمداً از هم جدا نگه داشته شده‌اند (طبق نیازمندی).
 */
@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stage"]),
        Index(value = ["difficulty"]),
        Index(value = ["nextReviewAt"])
    ]
)
data class LearningStateEntity(
    @PrimaryKey
    val conceptId: Long,
    // DAILY, WEEKLY, MONTHLY, LEARNED
    val stage: String = "DAILY",
    // EASY, MEDIUM, HARD, VERY_HARD
    val difficulty: String = "MEDIUM",
    val nextReviewAt: Long? = null,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    // مشخص می‌کند این Concept تا به حال حتی یک‌بار در هر مرحله‌ای اشتباه جواب داده شده یا نه؛
    // برای تعیین Difficulty=EASY در لحظه رسیدن به LEARNED لازم است (بند 26).
    val everFailed: Boolean = false
)
