package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.ReviewHistory
import kotlinx.coroutines.flow.Flow

interface ReviewHistoryRepository {
    suspend fun insertReview(history: ReviewHistory): Long
    fun getByConceptId(conceptId: Long): Flow<List<ReviewHistory>>
    suspend fun getBySessionId(sessionId: String): List<ReviewHistory>
    fun getTotalCorrect(): Flow<Int>
    fun getTotalReviews(): Flow<Int>
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<ReviewHistory>>
}
