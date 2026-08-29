package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import javax.inject.Inject

/**
 * تولید Session id خوانا مثل 2026-08-16-001 (بند 65).
 * شماره ترتیبی از تعداد Session های همان روز گرفته می‌شود.
 */
class GenerateSessionIdUseCase @Inject constructor(
    private val reviewSessionRepository: ReviewSessionRepository
) {
    suspend operator fun invoke(reviewType: String, now: Long = DateTimeUtils.now()): String {
        return reviewSessionRepository.startSession(reviewType, now)
    }
}
