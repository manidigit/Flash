package com.app.flashlearn.domain.model

data class LearningState(
    val conceptId: Long,
    val stage: ReviewStage,
    val difficulty: Difficulty,
    val nextReviewAt: Long?,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null
)
