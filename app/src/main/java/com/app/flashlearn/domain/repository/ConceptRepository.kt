package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import kotlinx.coroutines.flow.Flow

interface ConceptRepository {
    suspend fun insertConcept(concept: Concept): Long
    suspend fun updateConcept(concept: Concept)
    suspend fun deleteConcept(concept: Concept)
    suspend fun getConceptById(id: Long): Concept?
    fun getAllActiveConcepts(): Flow<List<Concept>>
    fun getActiveConceptCount(): Flow<Int>
}
