package com.app.flashlearn.data.repository

import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewHistory
import com.app.flashlearn.domain.model.ReviewTransition
import com.app.flashlearn.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val database: FlashLearnDatabase
) : ReviewRepository {

    override suspend fun getLearningState(conceptId: Long): LearningState? {
        return database.learningStateDao().findByConcept(conceptId)?.toDomain()
    }

    override suspend fun saveTransition(transition: ReviewTransition) {
        // اجرای تراکنش
        database.apply {
            // بروزرسانی learning_state با شرط optimistic lock
            val updatedRows = learningStateDao().updateWithExpectedState(
                conceptId = transition.conceptId,
                expectedStage = transition.previousState.stage.name,
                expectedDifficulty = transition.previousState.difficulty.name,
                newStage = transition.newState.stage.name,
                newDifficulty = transition.newState.difficulty.name,
                newNextReviewAt = transition.newState.nextReviewAt,
                newMonthlyWrongCount = transition.newState.monthlyWrongCount,
                newTotalCorrect = transition.newState.totalCorrect,
                newTotalWrong = transition.newState.totalWrong,
                newLastReviewedAt = transition.newState.lastReviewedAt
            )
            if (updatedRows == 0) {
                throw IllegalStateException("Concurrent modification detected")
            }

            // درج history
            val historyEntity = ReviewHistoryEntity(
                conceptId = transition.conceptId,
                sessionId = transition.sessionId,
                reviewAttemptId = transition.reviewAttemptId,
                reviewStage = transition.previousState.stage.name,
                reviewDate = transition.newState.lastReviewedAt ?: System.currentTimeMillis(),
                isCorrect = transition.isCorrect,
                previousStatus = transition.previousState.stage.name,
                newStatus = transition.newState.stage.name,
                previousDifficulty = transition.previousState.difficulty.name,
                newDifficulty = transition.newState.difficulty.name,
                responseTimeMs = transition.responseTimeMs
            )
            reviewHistoryDao().insert(historyEntity)
        }
    }

    override suspend fun getReviewHistoryForConcept(conceptId: Long): List<ReviewHistory> {
        return database.reviewHistoryDao().findByConcept(conceptId).map { entity ->
            ReviewHistory(
                id = entity.id,
                conceptId = entity.conceptId,
                sessionId = entity.sessionId,
                reviewAttemptId = entity.reviewAttemptId,
                reviewStage = entity.reviewStage,
                reviewDate = entity.reviewDate,
                isCorrect = entity.isCorrect,
                previousStatus = entity.previousStatus,
                newStatus = entity.newStatus,
                previousDifficulty = entity.previousDifficulty,
                newDifficulty = entity.newDifficulty,
                responseTimeMs = entity.responseTimeMs
            )
        }
    }
}
