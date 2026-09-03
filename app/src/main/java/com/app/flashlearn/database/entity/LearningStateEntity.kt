package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(entity = ConceptEntity::class, parentColumns = ["id"], childColumns = ["conceptId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)
    ],
    indices = [
        Index("stage"),
        Index("nextReviewAt"),
        Index("difficulty")
    ]
)
data class LearningStateEntity(
    @PrimaryKey val conceptId: Long,
    val stage: String = "DAILY",
    val difficulty: String = "EASY",
    val nextReviewAt: Long? = null,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    val everFailed: Boolean = false,
    val dailyReviewCount: Int = 0,
    val dailyCorrectCount: Int = 0,
    val dailyIncorrectCount: Int = 0,
    val weeklyReviewCount: Int = 0,
    val weeklyCorrectCount: Int = 0,
    val weeklyIncorrectCount: Int = 0,
    val monthlyReviewCount: Int = 0,
    val monthlyCorrectCount: Int = 0,
    val monthlyIncorrectCount: Int = 0,
    val consecutiveCorrect: Int = 0,
    val consecutiveIncorrect: Int = 0,
    val highestStageReached: String = "DAILY",
    val weeklyToDailyReturns: Int = 0,
    val monthlyToDailyReturns: Int = 0,
    val monthlyCompletions: Int = 0,
    val learnedCount: Int = 0,
    val lastReviewResult: Boolean? = null,
    val difficultyScore: Int = 0
)
