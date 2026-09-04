package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewHistoryEntry
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewRepository
import javax.inject.Inject

class ProcessReviewAnswerUseCase @Inject constructor(
    private val learningStateRepository: LearningStateRepository,
    private val reviewRepository: ReviewRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    companion object {
        private val SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000L
        private val THIRTY_DAYS = 30 * 24 * 60 * 60 * 1000L
    }

    suspend operator fun invoke(
        conceptId: Long,
        sessionId: String,
        isCorrect: Boolean,
        responseTimeMs: Long
    ): LearningState {
        val now = clock()
        val current = learningStateRepository.getState(conceptId)
            ?: LearningState(conceptId, ReviewStage.DAILY, Difficulty.EASY, null)

        val previousStage = current.stage
        val previousDifficulty = current.difficulty

        val next = when (current.stage) {
            ReviewStage.DAILY -> if (isCorrect) {
                current.copy(stage = ReviewStage.WEEKLY, nextReviewAt = now + SEVEN_DAYS)
            } else {
                current
            }

            ReviewStage.WEEKLY -> if (isCorrect) {
                current.copy(stage = ReviewStage.MONTHLY, nextReviewAt = now + THIRTY_DAYS)
            } else {
                current.copy(
                    stage = ReviewStage.DAILY,
                    nextReviewAt = null,
                    difficulty = maxOf(current.difficulty, Difficulty.MEDIUM)
                )
            }

            ReviewStage.MONTHLY -> if (isCorrect) {
                val firstTimeSuccess = current.monthlyWrongCount == 0
                current.copy(
                    stage = ReviewStage.LEARNED,
                    nextReviewAt = null,
                    difficulty = if (firstTimeSuccess) Difficulty.EASY else current.difficulty
                )
            } else {
                val wrongCount = current.monthlyWrongCount + 1
                current.copy(
                    stage = ReviewStage.DAILY,
                    nextReviewAt = null,
                    monthlyWrongCount = wrongCount,
                    difficulty = if (wrongCount > 1) Difficulty.VERY_HARD else Difficulty.HARD
                )
            }

            ReviewStage.LEARNED -> current
        }.let {
            it.copy(
                totalCorrect = it.totalCorrect + if (isCorrect) 1 else 0,
                totalWrong = it.totalWrong + if (isCorrect) 0 else 1,
                lastReviewedAt = now
            )
        }

        learningStateRepository.upsertState(next)

        reviewRepository.recordHistory(
            ReviewHistoryEntry(
                conceptId = conceptId,
                sessionId = sessionId,
                reviewStage = previousStage,
                reviewDate = now,
                isCorrect = isCorrect,
                previousStatus = previousStage,
                newStatus = next.stage,
                previousDifficulty = previousDifficulty,
                newDifficulty = next.difficulty,
                responseTimeMs = responseTimeMs
            )
        )

        return next
    }
}
