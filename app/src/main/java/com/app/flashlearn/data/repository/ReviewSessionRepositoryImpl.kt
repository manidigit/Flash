package com.app.flashlearn.data.repository

import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.domain.model.ReviewSession
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewSessionRepositoryImpl @Inject constructor(
    private val database: FlashLearnDatabase
) : ReviewSessionRepository {

    override suspend fun createSession(session: ReviewSession) {
        database.reviewSessionDao().insert(session.toEntity())
    }

    override suspend fun closeSession(sessionId: String, endedAt: Long) {
        database.reviewSessionDao().closeSession(sessionId, endedAt)
    }

    override suspend fun getSession(sessionId: String): ReviewSession? {
        return database.reviewSessionDao().findById(sessionId)?.toDomain()
    }
}
