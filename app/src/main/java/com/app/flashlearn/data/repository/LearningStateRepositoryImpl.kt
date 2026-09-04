package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDbString
import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.LearningStateRepository
import javax.inject.Inject

class LearningStateRepositoryImpl @Inject constructor(
    private val dao: LearningStateDao
) : LearningStateRepository {
    override suspend fun getState(conceptId: Long) = dao.getByConceptId(conceptId)?.toDomain()
    override suspend fun upsertState(state: LearningState) = dao.upsert(state.toEntity())
    override suspend fun getDueConcepts(stage: ReviewStage, now: Long) =
        dao.getDueConcepts(stage.toDbString(), now).map { it.toDomain() }
    override suspend fun countByStage(stage: ReviewStage) = dao.countByStage(stage.toDbString())
}
