package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import javax.inject.Inject

class AddConceptUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    suspend operator fun invoke(concept: Concept): Long {
        require(!conceptRepository.isDuplicate(concept.uuid)) { "Duplicate concept uuid" }
        val now = clock()
        val id = conceptRepository.addConcept(concept.copy(createdAt = now, updatedAt = now))
        learningStateRepository.upsertState(
            LearningState(conceptId = id, stage = ReviewStage.DAILY, difficulty = Difficulty.EASY, nextReviewAt = null)
        )
        return id
    }
}
