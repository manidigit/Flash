package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.ReviewSession

interface ReviewSessionRepository {
    suspend fun createSession(session: ReviewSession)
    suspend fun closeSession(sessionId: String, endedAt: Long)
    suspend fun getSession(sessionId: String): ReviewSession?
}
