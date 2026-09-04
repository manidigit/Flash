package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.ReviewTransitionEngine
import com.app.flashlearn.domain.model.*
import com.app.flashlearn.domain.repository.ReviewRepository
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import com.app.flashlearn.core.util.Result
import java.time.Instant
import javax.inject.Inject

class SubmitReviewAnswerUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val sessionRepository: ReviewSessionRepository,
    private val engine: ReviewTransitionEngine
) {

    suspend operator fun invoke(
        conceptId: Long,
        answer: ReviewAnswer,
        sessionId: String,
        attemptId: String,
        responseTimeMs: Long?,
        now: Instant
    ): Result<ReviewTransition> {
        // 1. بارگذاری وضعیت فعلی
        val currentState = reviewRepository.getLearningState(conceptId)
            ?: return Result.Error("Learning state not found")

        // 2. اعتبارسنجی: آیا کارت واجد شرایط است؟
        if (currentState.stage != ReviewStage.LEARNED) {
            if (currentState.nextReviewAt == null || currentState.nextReviewAt > now.toEpochMilli()) {
                return Result.Error("Card not due yet")
            }
        }

        // 3. محاسبه انتقال با استفاده از TransitionEngine
        // برای تعیین firstTimeSuccess، باید تاریخچه بررسی شود. ساده‌سازی: فرض می‌کنیم false
        val transition = engine.transition(
            state = currentState,
            answer = answer,
            now = now,
            isFirstTimeFullPathSuccess = false // در عمل باید از Repository بررسی شود
        )

        // 4. ذخیره در یک تراکنش
        return try {
            // اینجا باید یک تراکنش Room انجام دهیم:
            // - بروزرسانی learning_state
            // - درج review_history
            // برای سادگی، در Repository پیاده‌سازی می‌شود
            reviewRepository.saveTransition(transition.copy(
                sessionId = sessionId,
                reviewAttemptId = attemptId,
                responseTimeMs = responseTimeMs
            ))
            Result.Success(transition)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to save review")
        }
    }
}
