package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_state",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("stage"),
        Index("nextReviewAt"),
        Index("difficulty")
    ]
)
data class LearningStateEntity(
    @PrimaryKey
    val conceptId: Long,
    val stage: String, // DAILY / WEEKLY / MONTHLY / LEARNED
    val difficulty: String = "EASY", // EASY / MEDIUM / HARD / VERY_HARD
    val nextReviewAt: Long = 0, // NOT NULL - مقدار پیش‌فرض 0 (بدان معنی بلافاصله تمرین شود)
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    val hasFailedInCurrentCycle: Boolean = false // اصلاح: برای تعیین firstTimeSuccessAllStages
)
