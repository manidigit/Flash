package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.domain.model.ReviewHistory

fun ReviewHistoryEntity.toDomain(): ReviewHistory = ReviewHistory(
    id = id,
    conceptId = conceptId,
    sessionId = sessionId,
    reviewStage = reviewStage,
    reviewDate = reviewDate,
    isCorrect = isCorrect,
    previousStatus = previousStatus,
    newStatus = newStatus,
    previousDifficulty = previousDifficulty,
    newDifficulty = newDifficulty,
    responseTimeMs = responseTimeMs
)
