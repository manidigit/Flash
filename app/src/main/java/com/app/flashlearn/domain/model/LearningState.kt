package com.app.flashlearn.domain.model

data class LearningState(
    val conceptId: Long,
    val stage: String, // DAILY / WEEKLY / MONTHLY / LEARNED
    val difficulty: String, // EASY / MEDIUM / HARD / VERY_HARD
    val nextReviewAt: Long = 0,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    val hasFailedInCurrentCycle: Boolean = false
)
