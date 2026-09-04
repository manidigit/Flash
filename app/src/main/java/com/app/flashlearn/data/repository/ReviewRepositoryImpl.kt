package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.database.dao.ReviewSessionDao
import com.app.flashlearn.domain.model.ReviewHistoryEntry
import com.app.flashlearn.domain.model.ReviewSession
import com.app.flashlearn.domain.repository.ReviewRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val sessionDao: ReviewSessionDao,
    private val historyDao: ReviewHistoryDao
) : ReviewRepository {
    override suspend fun startSession(reviewType: String): ReviewSession {
        val session = ReviewSession(
            id = generateSessionId(),
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            reviewType = reviewType
        )
        sessionDao.insert(session.toEntity())
        return session
    }

    override suspend fun endSession(sessionId: String, endedAt: Long) {
        val existing = sessionDao.getById(sessionId) ?: return
        sessionDao.update(existing.copy(endedAt = endedAt))
    }

    override suspend fun recordHistory(entry: ReviewHistoryEntry) =
        historyDao.insert(entry.toEntity())

    override suspend fun getHistoryForConcept(conceptId: Long) =
        historyDao.getByConceptId(conceptId).map { it.toDomain() }

    private fun generateSessionId(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "$date-${(1000..9999).random()}"
    }
}
