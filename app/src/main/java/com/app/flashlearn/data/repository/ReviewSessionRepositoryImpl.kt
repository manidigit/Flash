package com.app.flashlearn.data.repository

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.database.dao.ReviewSessionDao
import com.app.flashlearn.database.entity.ReviewSessionEntity
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import javax.inject.Inject

class ReviewSessionRepositoryImpl @Inject constructor(
    private val dao: ReviewSessionDao
) : ReviewSessionRepository {

    override suspend fun startSession(reviewType: String, startedAt: Long): String {
        val datePrefix = DateTimeUtils.todayDatePrefix(startedAt)
        val countToday = dao.countSessionsForDatePrefix(datePrefix)
        val sequence = (countToday + 1).toString().padStart(3, '0')
        val sessionId = "$datePrefix-$sequence"

        dao.insert(
            ReviewSessionEntity(
                id = sessionId,
                startedAt = startedAt,
                reviewType = reviewType
            )
        )
        return sessionId
    }

    override suspend fun endSession(sessionId: String, endedAt: Long) {
        val session = dao.getById(sessionId) ?: return
        dao.update(session.copy(endedAt = endedAt))
    }
}
