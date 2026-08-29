package com.app.flashlearn.domain.repository

interface ReviewSessionRepository {
    /** یک Session id جدید به فرمت yyyy-MM-dd-NNN می‌سازد و رکورد آن را ذخیره می‌کند. */
    suspend fun startSession(reviewType: String, startedAt: Long): String
    suspend fun endSession(sessionId: String, endedAt: Long)
}
