package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import javax.inject.Inject

/**
 * لیست کارت‌های آماده مرور برای یک Stage مشخص را برمی‌گرداند (بند 20/22/31):
 * فقط آیتم‌هایی که nextReviewAt آن‌ها رسیده یا null است.
 */
class GetDueConceptsUseCase @Inject constructor(
    private val learningStateRepository: LearningStateRepository,
    private val conceptRepository: ConceptRepository
) {
    suspend operator fun invoke(
        stage: LearningStage,
        limit: Int = 50,
        now: Long = DateTimeUtils.now(),
        categoryId: Long? = null
    ): List<Concept> {
        val dueStates = learningStateRepository.getDue(stage, now, limit, categoryId)
        return dueStates.mapNotNull { state -> conceptRepository.getById(state.conceptId) }
    }
}
