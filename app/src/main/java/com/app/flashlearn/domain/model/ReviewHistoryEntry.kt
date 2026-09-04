package com.app.flashlearn.domain.model

data class ReviewHistoryEntry(
    val id: Long = 0,
    val conceptId: Long,
    val sessionId: String,
    val reviewStage: ReviewStage,
    val reviewDate: Long,
    val isCorrect: Boolean,
    val previousStatus: ReviewStage,
    val newStatus: ReviewStage,
    val previousDifficulty: Difficulty,
    val newDifficulty: Difficulty,
    val responseTimeMs: Long
)
