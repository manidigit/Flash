package com.app.flashlearn.data.repository

import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConceptRepositoryImpl @Inject constructor(
    private val database: FlashLearnDatabase
) : ConceptRepository {

    override suspend fun insert(concept: Concept): Long {
        return database.conceptDao().insert(concept.toEntity())
    }

    override suspend fun update(concept: Concept) {
        database.conceptDao().update(concept.toEntity())
    }

    override suspend fun deactivate(conceptId: Long) {
        database.conceptDao().deactivate(conceptId)
    }

    override suspend fun findById(id: Long): Concept? {
        return database.conceptDao().findById(id)?.toDomain()
    }

    override fun observeAllActive(): Flow<List<Concept>> {
        return database.conceptDao().observeAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun observeFavorites(): Flow<List<Concept>> {
        return database.conceptDao().observeFavorites().map { list -> list.map { it.toDomain() } }
    }
}
