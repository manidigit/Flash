package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.database.entity.ReviewSessionEntity
import com.app.flashlearn.domain.model.ReviewHistoryEntry
import com.app.flashlearn.domain.model.ReviewSession

fun ReviewHistoryEntity.toDomain(): ReviewHistoryEntry = ReviewHistoryEntry(
    id = id, conceptId = conceptId, sessionId = sessionId,
    reviewStage = reviewStage.toReviewStage(), reviewDate = reviewDate, isCorrect = isCorrect,
    previousStatus = previousStatus.toReviewStage(), newStatus = newStatus.toReviewStage(),
    previousDifficulty = previousDifficulty.toDifficulty(), newDifficulty = newDifficulty.toDifficulty(),
    responseTimeMs = responseTimeMs
)

fun ReviewHistoryEntry.toEntity(): ReviewHistoryEntity = ReviewHistoryEntity(
    id = id, conceptId = conceptId, sessionId = sessionId,
    reviewStage = reviewStage.toDbString(), reviewDate = reviewDate, isCorrect = isCorrect,
    previousStatus = previousStatus.toDbString(), newStatus = newStatus.toDbString(),
    previousDifficulty = previousDifficulty.toDbString(), newDifficulty = newDifficulty.toDbString(),
    responseTimeMs = responseTimeMs
)

fun ReviewSessionEntity.toDomain() = ReviewSession(id, startedAt, endedAt, reviewType)
fun ReviewSession.toEntity() = ReviewSessionEntity(id, startedAt, endedAt, reviewType)
