package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewStage

interface LearningStateRepository {
    suspend fun getState(conceptId: Long): LearningState?
    suspend fun upsertState(state: LearningState)
    suspend fun getDueConcepts(stage: ReviewStage, now: Long): List<Concept>
    suspend fun countByStage(stage: ReviewStage): Int
}
