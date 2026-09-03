package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import kotlinx.coroutines.flow.Flow

interface LearningStateRepository {
    suspend fun get(conceptId: Long): LearningState?
    suspend fun save(state: LearningState)

    suspend fun getDue(stage: LearningStage, now: Long, limit: Int, categoryId: Long?): List<LearningState>
    suspend fun getByDifficulty(difficulty: Difficulty, limit: Int, offset: Int, categoryId: Long?): List<LearningState>
    suspend fun getLearned(limit: Int, offset: Int, categoryId: Long?): List<LearningState>

    suspend fun countTotal(stage: LearningStage, categoryId: Long?): Int
    suspend fun countDue(stage: LearningStage, now: Long, categoryId: Long?): Int
    suspend fun countByDifficulty(difficulty: Difficulty, categoryId: Long?): Int
    suspend fun countLearned(categoryId: Long?): Int

    suspend fun getStageSummary(): Map<LearningStage, Int>
    suspend fun getDifficultySummary(): Map<Difficulty, Int>

    fun getLearnedCount(): Flow<Int>
    fun getReadyForReview(stage: String, now: Long): Flow<List<LearningState>>
}
