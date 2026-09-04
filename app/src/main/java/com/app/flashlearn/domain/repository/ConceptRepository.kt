package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ReviewStage

interface ConceptRepository {
    suspend fun addConcept(concept: Concept): Long
    suspend fun updateConcept(concept: Concept)
    suspend fun getConceptById(id: Long): Concept?
    suspend fun getConceptsPaged(limit: Int, offset: Int): List<Concept>
    suspend fun searchConcepts(query: String): List<Concept>
    suspend fun isDuplicate(uuid: String): Boolean
    suspend fun deleteConcept(id: Long)
    suspend fun getConceptsByStage(stage: ReviewStage, limit: Int, offset: Int): List<Concept>
    suspend fun getNewConcepts(limit: Int, offset: Int): List<Concept>
    suspend fun getLearningConcepts(limit: Int, offset: Int): List<Concept>
}
