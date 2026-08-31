package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConceptRepositoryImpl @Inject constructor(
    private val conceptDao: ConceptDao
) : ConceptRepository {
    
    override suspend fun insertConcept(concept: Concept): Long {
        return conceptDao.insert(concept.toEntity())
    }

    override suspend fun updateConcept(concept: Concept) {
        conceptDao.update(concept.toEntity())
    }

    override suspend fun deleteConcept(concept: Concept) {
        conceptDao.delete(concept.toEntity())
    }

    override suspend fun getConceptById(id: Long): Concept? {
        return conceptDao.getById(id)?.toDomain()
    }

    override fun getAllActiveConcepts(): Flow<List<Concept>> {
        return conceptDao.getAllActive().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveConceptCount(): Flow<Int> {
        return conceptDao.getActiveCount()
    }

    private fun Concept.toEntity() = ConceptEntity(
        id = id,
        uuid = uuid,
        contentType = contentType,
        categoryId = categoryId,
        favorite = favorite,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun ConceptEntity.toDomain() = Concept(
        id = id,
        uuid = uuid,
        contentType = contentType,
        categoryId = categoryId,
        favorite = favorite,
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
