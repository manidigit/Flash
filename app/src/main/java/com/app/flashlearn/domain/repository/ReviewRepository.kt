package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.ReviewHistoryEntry
import com.app.flashlearn.domain.model.ReviewSession

interface ReviewRepository {
    suspend fun startSession(reviewType: String): ReviewSession
    suspend fun endSession(sessionId: String, endedAt: Long)
    suspend fun recordHistory(entry: ReviewHistoryEntry)
    suspend fun getHistoryForConcept(conceptId: Long): List<ReviewHistoryEntry>
}
