package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.ReviewIntervals
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewOutcome
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * پیاده‌سازی خالص (بدون وابستگی به Android/Room) الگوریتم مرور و طبقه‌بندی خودکار سختی.
 * این کلاس فقط یک تابع محاسباتی است؛ ذخیره‌سازی نتیجه بر عهده Repository/UseCase بالادستی
 * است. مستقل و کاملاً قابل تست با JUnit است (نیازی به هیچ Mock/Android Context ندارد).
 *
 * سیستم طبقه‌بندی سختی سازگار (Adaptive Difficulty Classification):
 * - هیچ‌کدام از شمارنده‌های تاریخچه‌ای (dailyIncorrectCount, weeklyIncorrectCount,
 *   monthlyIncorrectCount, monthlyToDailyReturns و ...) هرگز کاهش داده نمی‌شوند؛ فقط جمع
 *   می‌شوند. کاربر می‌تواند سختی نمایشی بهتر شود (Recovery) اما تاریخچه واقعی همیشه محفوظ
 *   و قابل بازسازی می‌ماند (طبق نیازمندی صریح «Do not destroy historical data»).
 * - سختی بعد از *هر* مرور (چه درست چه غلط) دوباره محاسبه می‌شود.
 * - اشتباه در MONTHLY بیشترین وزن را دارد، بعد WEEKLY، بعد DAILY (طبق نیازمندی صریح
 *   «A mistake at a later learning stage is more significant»).
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

    // DAILY -> WEEKLY در صورت صحیح؛ در غیر این صورت در DAILY می‌ماند و امتیاز سختی طبق
    // چندمین اشتباه DAILY (از ابتدای عمر این کلمه) افزایش می‌یابد.
    private fun handleDaily(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        val withReviewCounted = state.copy(
            dailyReviewCount = state.dailyReviewCount + 1,
            lastReviewedAt = now,
            lastReviewResult = isCorrect
        )

        val afterOutcome = if (isCorrect) {
            withReviewCounted.copy(
                stage = LearningStage.WEEKLY,
                nextReviewAt = addDays(now, ReviewIntervals.WEEKLY_DELAY_DAYS),
                totalCorrect = withReviewCounted.totalCorrect + 1,
                dailyCorrectCount = withReviewCounted.dailyCorrectCount + 1,
                consecutiveCorrect = withReviewCounted.consecutiveCorrect + 1,
                consecutiveIncorrect = 0,
                highestStageReached = maxStage(withReviewCounted.highestStageReached, LearningStage.WEEKLY)
            )
        } else {
            val newDailyIncorrectCount = withReviewCounted.dailyIncorrectCount + 1
            val delta = DifficultyScoreCalculator.dailyIncorrectDelta(newDailyIncorrectCount)
            withReviewCounted.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                everFailed = true,
                totalWrong = withReviewCounted.totalWrong + 1,
                dailyIncorrectCount = newDailyIncorrectCount,
                consecutiveIncorrect = withReviewCounted.consecutiveIncorrect + 1,
                consecutiveCorrect = 0,
                difficultyScore = withReviewCounted.difficultyScore + delta
            )
        }

        return afterOutcome.copy(difficulty = classify(afterOutcome))
    }

    // WEEKLY فقط وقتی نمایش داده می‌شود که nextReviewAt رسیده باشد (در Repository چک می‌شود).
    // نتیجه: صحیح -> MONTHLY، غلط -> بازگشت به DAILY (با ثبت این بازگشت) و افزایش امتیاز سختی.
    private fun handleWeekly(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        val withReviewCounted = state.copy(
            weeklyReviewCount = state.weeklyReviewCount + 1,
            lastReviewedAt = now,
            lastReviewResult = isCorrect
        )

        val afterOutcome = if (isCorrect) {
            withReviewCounted.copy(
                stage = LearningStage.MONTHLY,
                nextReviewAt = addDays(now, ReviewIntervals.MONTHLY_DELAY_DAYS),
                totalCorrect = withReviewCounted.totalCorrect + 1,
                weeklyCorrectCount = withReviewCounted.weeklyCorrectCount + 1,
                consecutiveCorrect = withReviewCounted.consecutiveCorrect + 1,
                consecutiveIncorrect = 0,
                highestStageReached = maxStage(withReviewCounted.highestStageReached, LearningStage.MONTHLY)
            )
        } else {
            val newWeeklyIncorrectCount = withReviewCounted.weeklyIncorrectCount + 1
            val delta = DifficultyScoreCalculator.weeklyIncorrectDelta(newWeeklyIncorrectCount)
            withReviewCounted.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                everFailed = true,
                totalWrong = withReviewCounted.totalWrong + 1,
                weeklyIncorrectCount = newWeeklyIncorrectCount,
                weeklyToDailyReturns = withReviewCounted.weeklyToDailyReturns + 1,
                consecutiveIncorrect = withReviewCounted.consecutiveIncorrect + 1,
                consecutiveCorrect = 0,
                difficultyScore = withReviewCounted.difficultyScore + delta
            )
        }

        return afterOutcome.copy(difficulty = classify(afterOutcome))
    }

    // MONTHLY صحیح -> LEARNED (EASY فقط اگر هیچ‌وقت اشتباه نکرده باشد).
    // MONTHLY غلط -> بازگشت به DAILY؛ این مهم‌ترین سیگنال سختی است (بند «MONTHLY FAILURE
    // HAS HIGH PRIORITY») و بیشترین امتیاز را می‌گیرد، با تشدید تهاجمی در شکست‌های تکراری.
    private fun handleMonthly(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        val withReviewCounted = state.copy(
            monthlyReviewCount = state.monthlyReviewCount + 1,
            lastReviewedAt = now,
            lastReviewResult = isCorrect
        )

        val afterOutcome = if (isCorrect) {
            withReviewCounted.copy(
                stage = LearningStage.LEARNED,
                nextReviewAt = null,
                totalCorrect = withReviewCounted.totalCorrect + 1,
                monthlyCorrectCount = withReviewCounted.monthlyCorrectCount + 1,
                consecutiveCorrect = withReviewCounted.consecutiveCorrect + 1,
                consecutiveIncorrect = 0,
                highestStageReached = maxStage(withReviewCounted.highestStageReached, LearningStage.LEARNED),
                monthlyCompletions = withReviewCounted.monthlyCompletions + 1,
                learnedCount = withReviewCounted.learnedCount + 1
            )
        } else {
            val newMonthlyIncorrectCount = withReviewCounted.monthlyIncorrectCount + 1
            val delta = DifficultyScoreCalculator.monthlyIncorrectDelta(newMonthlyIncorrectCount)
            withReviewCounted.copy(
                stage = LearningStage.DAILY,
                nextReviewAt = null,
                everFailed = true,
                totalWrong = withReviewCounted.totalWrong + 1,
                monthlyIncorrectCount = newMonthlyIncorrectCount,
                monthlyWrongCount = withReviewCounted.monthlyWrongCount + 1,
                monthlyToDailyReturns = withReviewCounted.monthlyToDailyReturns + 1,
                consecutiveIncorrect = withReviewCounted.consecutiveIncorrect + 1,
                consecutiveCorrect = 0,
                difficultyScore = withReviewCounted.difficultyScore + delta
            )
        }

        val classified = afterOutcome.copy(difficulty = classify(afterOutcome))
        // این یک مورد حیاتی است (طبق نیازمندی): شکست تکراری در MONTHLY هرگز نباید در
        // EASY/MEDIUM باقی بماند، حتی اگر فرمول امتیازدهی به‌هر‌دلیلی نتیجه پایین‌تری بدهد.
        return if (!isCorrect && classified.monthlyIncorrectCount >= 3 &&
            (classified.difficulty == Difficulty.EASY || classified.difficulty == Difficulty.MEDIUM)
        ) {
            classified.copy(difficulty = Difficulty.VERY_HARD)
        } else {
            classified
        }
    }

    // مرور کلمات یادگرفته‌شده اختیاری است؛ تاریخچه ثبت می‌شود ولی stage تغییر نمی‌کند. طبق
    // طراحی، پاسخ غلط در این مرحله اختیاری وارد فرمول امتیاز سختی نمی‌شود (چون بخشی از چرخه
    // اصلی DAILY/WEEKLY/MONTHLY نیست)، اما سختی نمایشی همچنان دوباره محاسبه می‌شود چون
    // رشته موفقیت اخیر (که در Recovery اثر دارد) تغییر کرده است.
    private fun handleLearned(state: LearningState, isCorrect: Boolean, now: Long): LearningState {
        val afterOutcome = state.copy(
            totalCorrect = if (isCorrect) state.totalCorrect + 1 else state.totalCorrect,
            totalWrong = if (!isCorrect) state.totalWrong + 1 else state.totalWrong,
            everFailed = state.everFailed || !isCorrect,
            consecutiveCorrect = if (isCorrect) state.consecutiveCorrect + 1 else 0,
            consecutiveIncorrect = if (!isCorrect) state.consecutiveIncorrect + 1 else 0,
            lastReviewedAt = now,
            lastReviewResult = isCorrect
        )
        return afterOutcome.copy(difficulty = classify(afterOutcome))
    }

    private fun classify(state: LearningState): Difficulty = DifficultyScoreCalculator.classify(
        rawScore = state.difficultyScore,
        consecutiveCorrect = state.consecutiveCorrect,
        monthlyIncorrectCount = state.monthlyIncorrectCount
    )

    private fun maxStage(a: LearningStage, b: LearningStage): LearningStage =
        if (a.ordinal >= b.ordinal) a else b

    private fun addDays(fromMillis: Long, days: Int): Long =
        fromMillis + TimeUnit.DAYS.toMillis(days.toLong())
}
