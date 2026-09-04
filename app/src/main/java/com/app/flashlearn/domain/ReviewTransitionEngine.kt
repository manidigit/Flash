package com.app.flashlearn.domain

import com.app.flashlearn.domain.model.*
import java.time.Instant

class ReviewTransitionEngine {

    fun transition(
        state: LearningState,
        answer: ReviewAnswer,
        now: Instant,
        isFirstTimeFullPathSuccess: Boolean = false
    ): ReviewTransition {
        val isCorrect = answer == ReviewAnswer.CORRECT
        val newState = when (state.stage) {
            ReviewStage.DAILY -> handleDaily(state, isCorrect, now)
            ReviewStage.WEEKLY -> handleWeekly(state, isCorrect, now)
            ReviewStage.MONTHLY -> handleMonthly(state, isCorrect, now, isFirstTimeFullPathSuccess)
            ReviewStage.LEARNED -> handleLearned(state, now)
        }
        return ReviewTransition(
            conceptId = state.conceptId,
            previousState = state,
            newState = newState,
            answer = answer,
            isCorrect = isCorrect,
            responseTimeMs = null, // بعداً توسط ViewModel پر می‌شود
            sessionId = "", // بعداً پر می‌شود
            reviewAttemptId = "" // بعداً پر می‌شود
        )
    }

    private fun handleDaily(state: LearningState, correct: Boolean, now: Instant): LearningState {
        return if (correct) {
            state.copy(
                stage = ReviewStage.WEEKLY,
                nextReviewAt = now.plusSeconds(7 * 24 * 60 * 60).toEpochMilli(),
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        } else {
            state.copy(
                stage = ReviewStage.DAILY,
                nextReviewAt = now.toEpochMilli(), // immediate
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        }
    }

    private fun handleWeekly(state: LearningState, correct: Boolean, now: Instant): LearningState {
        return if (correct) {
            state.copy(
                stage = ReviewStage.MONTHLY,
                nextReviewAt = now.plusSeconds(30 * 24 * 60 * 60).toEpochMilli(),
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        } else {
            val newDifficulty = if (state.difficulty.ordinal < Difficulty.MEDIUM.ordinal) {
                Difficulty.MEDIUM
            } else {
                state.difficulty
            }
            state.copy(
                stage = ReviewStage.DAILY,
                nextReviewAt = now.toEpochMilli(),
                difficulty = newDifficulty,
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        }
    }

    private fun handleMonthly(state: LearningState, correct: Boolean, now: Instant, firstTimeSuccess: Boolean): LearningState {
        return if (correct) {
            val newDifficulty = if (firstTimeSuccess) Difficulty.EASY else state.difficulty
            state.copy(
                stage = ReviewStage.LEARNED,
                nextReviewAt = null,
                difficulty = newDifficulty,
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        } else {
            val newMonthlyWrong = state.monthlyWrongCount + 1
            val newDifficulty = if (newMonthlyWrong > 1) Difficulty.VERY_HARD else Difficulty.HARD
            state.copy(
                stage = ReviewStage.DAILY,
                nextReviewAt = now.toEpochMilli(),
                difficulty = newDifficulty,
                monthlyWrongCount = newMonthlyWrong,
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now.toEpochMilli()
            )
        }
    }

    private fun handleLearned(state: LearningState, now: Instant): LearningState {
        // در اینجا فرض می‌کنیم فقط تاریخچه ثبت می‌شود، stage تغییر نمی‌کند.
        // اما برای نمونه اگر پاسخ اشتباه باشد، totalWrong افزایش می‌یابد.
        // در واقع ViewModel باید تصمیم بگیرد که آیا این مرور اختیاری است و تاریخچه ثبت شود.
        // این متد فقط برای تکمیل است؛ در عمل LEARNED توسط UseCase مدیریت می‌شود.
        return state.copy(
            // stage unchanged
            lastReviewedAt = now.toEpochMilli()
        )
    }
}
