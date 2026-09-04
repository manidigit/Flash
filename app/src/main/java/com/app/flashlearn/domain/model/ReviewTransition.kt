package com.app.flashlearn.domain.model

data class ReviewTransition(
    val conceptId: Long,
    val previousState: LearningState,
    val newState: LearningState,
    val answer: ReviewAnswer,
    val isCorrect: Boolean,
    val responseTimeMs: Long?,
    val sessionId: String,
    val reviewAttemptId: String
)
