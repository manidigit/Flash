package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.domain.model.ReviewOutcome
import com.app.flashlearn.domain.repository.ReviewHistoryRepository
import javax.inject.Inject

class ReviewHistoryRepositoryImpl @Inject constructor(
    private val dao: ReviewHistoryDao
) : ReviewHistoryRepository {

    override suspend fun record(
        conceptId: Long,
        sessionId: String?,
        outcome: ReviewOutcome,
        isCorrect: Boolean,
        reviewDate: Long,
        responseTimeMs: Long?
    ) {
        dao.insert(
            ReviewHistoryEntity(
                conceptId = conceptId,
                sessionId = sessionId,
                reviewStage = outcome.previousStage.name,
                reviewDate = reviewDate,
                isCorrect = isCorrect,
                previousStatus = outcome.previousStage.name,
                newStatus = outcome.newStage.name,
                previousDifficulty = outcome.previousDifficulty.name,
                newDifficulty = outcome.newDifficulty.name,
                responseTimeMs = responseTimeMs
            )
        )
    }

    override suspend fun countCorrectBetween(from: Long, to: Long): Int =
        dao.countCorrectBetween(from, to)

    override suspend fun countTotalBetween(from: Long, to: Long): Int =
        dao.countTotalBetween(from, to)
}
