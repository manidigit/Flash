package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.LearningStateRepository
import javax.inject.Inject

class LearningStateRepositoryImpl @Inject constructor(
    private val dao: LearningStateDao
) : LearningStateRepository {

    override suspend fun get(conceptId: Long): LearningState? =
        dao.getForConcept(conceptId)?.toDomain()

    override suspend fun save(state: LearningState) {
        dao.insert(state.toEntity())
    }

    override suspend fun getDue(stage: LearningStage, now: Long, limit: Int, categoryId: Long?): List<LearningState> =
        if (categoryId != null) {
            dao.getDueForStageInCategory(stage.name, now, categoryId, limit).map { it.toDomain() }
        } else {
            dao.getDueForStage(stage.name, now, limit).map { it.toDomain() }
        }

    override suspend fun countDue(stage: LearningStage, now: Long): Int =
        dao.countDueForStage(stage.name, now)

    override suspend fun getByDifficulty(difficulty: Difficulty, limit: Int, offset: Int, categoryId: Long?): List<LearningState> =
        if (categoryId != null) {
            dao.getByDifficultyInCategory(difficulty.name, categoryId, limit, offset).map { it.toDomain() }
        } else {
            dao.getByDifficulty(difficulty.name, limit, offset).map { it.toDomain() }
        }

    override suspend fun getLearned(limit: Int, offset: Int, categoryId: Long?): List<LearningState> =
        if (categoryId != null) {
            dao.getLearnedInCategory(categoryId, limit, offset).map { it.toDomain() }
        } else {
            dao.getLearned(limit, offset).map { it.toDomain() }
        }

    override suspend fun getDifficultySummary(): Map<Difficulty, Int> =
        dao.getDifficultySummary().associate { Difficulty.valueOf(it.difficulty) to it.count }

    override suspend fun getStageSummary(): Map<LearningStage, Int> =
        dao.getStageSummary().associate { LearningStage.valueOf(it.stage) to it.count }
}
