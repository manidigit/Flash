package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.LearningState
import kotlinx.coroutines.flow.Flow

interface LearningStateRepository {
    suspend fun insertOrUpdateLearningState(state: LearningState)
    suspend fun getLearningStateByConceptId(conceptId: Long): LearningState?
    fun getReadyForReview(stage: String): Flow<List<LearningState>>
    fun getByStage(stage: String): Flow<List<LearningState>>
    fun getCountByStage(stage: String): Flow<Int>
    fun getCountByDifficulty(difficulty: String): Flow<Int>
    suspend fun resetStreakIfNotReviewedYesterday(yesterdayMidnight: Long)
}
