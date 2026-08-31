package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.entity.LearningStateEntity
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.LearningStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LearningStateRepositoryImpl @Inject constructor(
    private val learningStateDao: LearningStateDao
) : LearningStateRepository {

    override suspend fun insertOrUpdateLearningState(state: LearningState) {
        learningStateDao.insert(state.toEntity())
    }

    override suspend fun getLearningStateByConceptId(conceptId: Long): LearningState? {
        return learningStateDao.getByConceptId(conceptId)?.toDomain()
    }

    override fun getReadyForReview(stage: String): Flow<List<LearningState>> {
        return learningStateDao.getReadyForReview(stage, System.currentTimeMillis()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getByStage(stage: String): Flow<List<LearningState>> {
        return learningStateDao.getByStage(stage).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCountByStage(stage: String): Flow<Int> {
        return learningStateDao.getCountByStage(stage)
    }

    override fun getCountByDifficulty(difficulty: String): Flow<Int> {
        return learningStateDao.getCountByDifficulty(difficulty)
    }

    private fun LearningState.toEntity() = LearningStateEntity(
        conceptId = conceptId,
        stage = stage,
        difficulty = difficulty,
        nextReviewAt = nextReviewAt,
        monthlyWrongCount = monthlyWrongCount,
        totalCorrect = totalCorrect,
        totalWrong = totalWrong,
        lastReviewedAt = lastReviewedAt,
        hasFailedInCurrentCycle = hasFailedInCurrentCycle
    )

    private fun LearningStateEntity.toDomain() = LearningState(
        conceptId = conceptId,
        stage = stage,
        difficulty = difficulty,
        nextReviewAt = nextReviewAt,
        monthlyWrongCount = monthlyWrongCount,
        totalCorrect = totalCorrect,
        totalWrong = totalWrong,
        lastReviewedAt = lastReviewedAt,
        hasFailedInCurrentCycle = hasFailedInCurrentCycle
    )
}
