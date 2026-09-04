package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import kotlinx.coroutines.flow.Flow

interface ConceptRepository {
    suspend fun insert(concept: Concept): Long
    suspend fun update(concept: Concept)
    suspend fun deactivate(conceptId: Long)
    suspend fun findById(id: Long): Concept?
    fun observeAllActive(): Flow<List<Concept>>
    fun observeFavorites(): Flow<List<Concept>>
}
