package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.ReviewIntervals
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewOutcome
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * پیاده‌سازی خالص (بدون وابستگی به Android/Room) الگوریتم مرور طبق بندهای 19 تا 26.
 * این کلاس فقط یک تابع محاسباتی است؛ ذخیره‌سازی نتیجه بر عهده Repository/UseCase بالادستی است.
 */
class ProcessReviewAnswerUseCase @Inject constructor() {

    operator fun invoke(currentState: LearningState, isCorrect: Boolean, now: Long): ReviewOutcome {
        val previousStage = currentState.stage
        val previousDifficulty = currentState.difficulty

        val newState = when (previousStage) {
            LearningStage.DAILY -> handleDaily(currentState, isCorrect, now)
            LearningStage.WEEKLY -> handleWeekly(currentState, isCorrect, now)
            LearningStage.MONTHLY -> handleMonthly(currentState, isCorrect, now)
            LearningStage.LEARNED -> handleLearned(currentState, isCorrect, now)
        }

        return ReviewOutcome(
            newState = newState,
            previousStage = previousStage,
            newStage = newState.stage,
            previousDifficulty = previousDifficulty,
            newDifficulty = newState.difficulty
        )
    }

    // بند 19: Daily -> Weekly در صورت صحیح، در غیر این صورت در Daily می‌ماند.
    private fun handleDaily(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        return if (isCorrect) {
            state.copy(
                stage = LearningStage.WEEKLY,
                nextReviewAt = addDays(now, ReviewIntervals.WEEKLY_DELAY_DAYS),
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now
            )
        } else {
            state.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                everFailed = true,
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now
            )
        }
    }

    // بند 20-21: Weekly فقط وقتی نمایش داده می‌شود که nextReviewAt رسیده باشد (در Repository چک می‌شود).
    // نتیجه: صحیح -> Monthly، غلط -> Daily با Difficulty=MEDIUM (اولین اشتباه).
    private fun handleWeekly(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        return if (isCorrect) {
            state.copy(
                stage = LearningStage.MONTHLY,
                nextReviewAt = addDays(now, ReviewIntervals.MONTHLY_DELAY_DAYS),
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now
            )
        } else {
            state.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                difficulty = Difficulty.MEDIUM,
                everFailed = true,
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now
            )
        }
    }

    // بند 22-26: Monthly صحیح -> LEARNED (EASY فقط اگر هرگز اشتباه نکرده باشد).
    // Monthly غلط -> Daily، Difficulty=HARD یا VERY_HARD بسته به تعداد خطاهای Monthly.
    private fun handleMonthly(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        return if (isCorrect) {
            val finalDifficulty = if (!state.everFailed) Difficulty.EASY else state.difficulty
            state.copy(
                stage = LearningStage.LEARNED,
                difficulty = finalDifficulty,
                nextReviewAt = null,
                totalCorrect = state.totalCorrect + 1,
                lastReviewedAt = now
            )
        } else {
            val newWrongCount = state.monthlyWrongCount + 1
            val newDifficulty = if (newWrongCount > 1) Difficulty.VERY_HARD else Difficulty.HARD
            state.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                difficulty = newDifficulty,
                monthlyWrongCount = newWrongCount,
                everFailed = true,
                totalWrong = state.totalWrong + 1,
                lastReviewedAt = now
            )
        }
    }

    // بند 29: مرور کلمات یادگرفته‌شده اختیاری است؛ تاریخچه ثبت می‌شود ولی stage تغییر نمی‌کند.
    private fun handleLearned(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        return state.copy(
            totalCorrect = if (isCorrect) state.totalCorrect + 1 else state.totalCorrect,
            totalWrong = if (!isCorrect) state.totalWrong + 1 else state.totalWrong,
            lastReviewedAt = now
        )
    }

    private fun addDays(fromMillis: Long, days: Int): Long =
        fromMillis + TimeUnit.DAYS.toMillis(days.toLong())
}
