package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.LearningStateRepository
import javax.inject.Inject

class GetDueConceptsUseCase @Inject constructor(
    private val learningStateRepository: LearningStateRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    suspend operator fun invoke(stage: ReviewStage): List<Concept> =
        learningStateRepository.getDueConcepts(stage, clock())
}
