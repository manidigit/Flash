package com.app.flashlearn.domain.model

data class LearningState(
    val conceptId: Long,
    val stage: ReviewStage,
    val difficulty: Difficulty,
    val nextReviewAt: Long?,
    val monthlyWrongCount: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val lastReviewedAt: Long?
)

enum class ReviewStage {
    DAILY, WEEKLY, MONTHLY, LEARNED
}

enum class Difficulty {
    EASY, MEDIUM, HARD, VERY_HARD
}
