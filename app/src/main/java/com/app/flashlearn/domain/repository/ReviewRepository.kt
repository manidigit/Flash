package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewHistory
import com.app.flashlearn.domain.model.ReviewTransition

interface ReviewRepository {
    suspend fun getLearningState(conceptId: Long): LearningState?
    suspend fun saveTransition(transition: ReviewTransition)
    suspend fun getReviewHistoryForConcept(conceptId: Long): List<ReviewHistory>
}
