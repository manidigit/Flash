package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.LearningStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LearningStateRepositoryImpl @Inject constructor(
    private val dao: LearningStateDao
) : LearningStateRepository {

    override suspend fun get(conceptId: Long): LearningState? = dao.get(conceptId)?.toDomain()

    override suspend fun save(state: LearningState) = dao.insert(state.toEntity())

    override suspend fun getDue(stage: LearningStage, now: Long, limit: Int, categoryId: Long?): List<LearningState> =
        dao.getDue(stage.name, now, limit, categoryId).map { it.toDomain() }

    override suspend fun getByDifficulty(difficulty: Difficulty, limit: Int, offset: Int, categoryId: Long?): List<LearningState> =
        dao.getByDifficulty(difficulty.name, limit, offset, categoryId).map { it.toDomain() }

    override suspend fun getLearned(limit: Int, offset: Int, categoryId: Long?): List<LearningState> =
        dao.getLearned(limit, offset, categoryId).map { it.toDomain() }

    override suspend fun countTotal(stage: LearningStage, categoryId: Long?): Int =
        dao.countTotal(stage.name, categoryId)

    override suspend fun countDue(stage: LearningStage, now: Long, categoryId: Long?): Int =
        dao.countDue(stage.name, now, categoryId)

    override suspend fun countByDifficulty(difficulty: Difficulty, categoryId: Long?): Int =
        dao.countByDifficulty(difficulty.name, categoryId)

    override suspend fun countLearned(categoryId: Long?): Int =
        dao.countLearned(categoryId)

    override suspend fun getStageSummary(): Map<LearningStage, Int> =
        dao.getStageSummaryRaw().mapNotNull { row ->
            runCatching { LearningStage.valueOf(row.stage) }.getOrNull()?.let { it to row.count }
        }.toMap()

    override suspend fun getDifficultySummary(): Map<Difficulty, Int> =
        dao.getDifficultySummaryRaw().mapNotNull { row ->
            runCatching { Difficulty.valueOf(row.difficulty) }.getOrNull()?.let { it to row.count }
        }.toMap()

    override fun getLearnedCount(): Flow<Int> = dao.getLearnedCount()

    override fun getReadyForReview(stage: String, now: Long): Flow<List<LearningState>> =
        dao.getReadyForReview(stage, now).map { list -> list.map { it.toDomain() } }
}
