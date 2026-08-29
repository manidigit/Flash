package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.ReviewOutcome

interface ReviewHistoryRepository {
    suspend fun record(
        conceptId: Long,
        sessionId: String?,
        outcome: ReviewOutcome,
        isCorrect: Boolean,
        reviewDate: Long,
        responseTimeMs: Long?
    )

    suspend fun countCorrectBetween(from: Long, to: Long): Int
    suspend fun countTotalBetween(from: Long, to: Long): Int

    /**
     * صفحه دیباگ/جزئیات یک کلمه (بند «ADMIN / DEBUG INFORMATION»): کل تاریخچه مرور یک
     * Concept، جدیدترین اول، برای بررسی صحت عملکرد الگوریتم طبقه‌بندی سختی.
     */
    suspend fun getForConcept(conceptId: Long): List<ReviewHistory>
}
