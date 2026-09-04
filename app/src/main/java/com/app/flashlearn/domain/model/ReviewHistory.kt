package com.app.flashlearn.domain.model

data class ReviewHistory(
    val id: Long,
    val conceptId: Long,
    val sessionId: String,
    val reviewAttemptId: String,
    val reviewStage: String,
    val reviewDate: Long,
    val isCorrect: Boolean,
    val previousStatus: String,
    val newStatus: String,
    val previousDifficulty: String,
    val newDifficulty: String,
    val responseTimeMs: Long?
)
