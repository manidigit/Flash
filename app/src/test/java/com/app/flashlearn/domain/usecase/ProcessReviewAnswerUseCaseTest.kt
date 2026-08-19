package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ProcessReviewAnswerUseCaseTest {

    private val useCase = ProcessReviewAnswerUseCase()
    private val now = 1_700_000_000_000L

    // ---------- بند 19: Daily ----------

    @Test
    fun daily_correct_movesToWeekly_withSevenDayDelay() {
        val state = LearningState(conceptId = 1, stage = LearningStage.DAILY)
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.WEEKLY, outcome.newStage)
        assertEquals(now + TimeUnit.DAYS.toMillis(7), outcome.newState.nextReviewAt)
    }

    @Test
    fun daily_incorrect_staysInDaily_andMarksEverFailed() {
        val state = LearningState(conceptId = 1, stage = LearningStage.DAILY)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertTrue(outcome.newState.everFailed)
        assertNull(outcome.newState.nextReviewAt)
    }

    // ---------- بند 20-21: Weekly ----------

    @Test
    fun weekly_correct_movesToMonthly_withThirtyDayDelay() {
        val state = LearningState(conceptId = 1, stage = LearningStage.WEEKLY)
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.MONTHLY, outcome.newStage)
        assertEquals(now + TimeUnit.DAYS.toMillis(30), outcome.newState.nextReviewAt)
    }

    @Test
    fun weekly_incorrect_returnsToDaily_withMediumDifficulty() {
        val state = LearningState(conceptId = 1, stage = LearningStage.WEEKLY, difficulty = Difficulty.EASY)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertEquals(Difficulty.MEDIUM, outcome.newDifficulty)
        assertTrue(outcome.newState.everFailed)
    }

    // ---------- بند 22-26: Monthly ----------

    @Test
    fun monthly_correct_neverFailedBefore_becomesLearnedAndEasy() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, everFailed = false)
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.EASY, outcome.newDifficulty)
    }

    @Test
    fun monthly_correct_butFailedBefore_becomesLearned_keepsCurrentDifficulty() {
        val state = LearningState(
            conceptId = 1,
            stage = LearningStage.MONTHLY,
            everFailed = true,
            difficulty = Difficulty.HARD
        )
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.HARD, outcome.newDifficulty)
    }

    @Test
    fun monthly_firstWrongAnswer_returnsToDaily_withHardDifficulty() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, monthlyWrongCount = 0)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertEquals(Difficulty.HARD, outcome.newDifficulty)
        assertEquals(1, outcome.newState.monthlyWrongCount)
    }

    @Test
    fun monthly_secondWrongAnswer_becomesVeryHard() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, monthlyWrongCount = 1)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertEquals(Difficulty.VERY_HARD, outcome.newDifficulty)
        assertEquals(2, outcome.newState.monthlyWrongCount)
    }

    @Test
    fun monthly_thirdWrongAnswer_staysVeryHard_andCountKeepsIncreasing() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, monthlyWrongCount = 2)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(Difficulty.VERY_HARD, outcome.newDifficulty)
        assertEquals(3, outcome.newState.monthlyWrongCount)
    }

    // ---------- بند 29: Learned (مرور اختیاری) ----------

    @Test
    fun learned_review_doesNotChangeStage_regardlessOfAnswer() {
        val state = LearningState(conceptId = 1, stage = LearningStage.LEARNED, difficulty = Difficulty.EASY)

        val correctOutcome = useCase(state, isCorrect = true, now = now)
        assertEquals(LearningStage.LEARNED, correctOutcome.newStage)

        val wrongOutcome = useCase(state, isCorrect = false, now = now)
        assertEquals(LearningStage.LEARNED, wrongOutcome.newStage)
    }

    // ---------- سناریوی کامل زنجیره‌ای ----------

    @Test
    fun fullHappyPath_dailyToWeeklyToMonthlyToLearned_endsAsEasy() {
        var state = LearningState(conceptId = 1)

        state = useCase(state, isCorrect = true, now = now).newState
        assertEquals(LearningStage.WEEKLY, state.stage)

        state = useCase(state, isCorrect = true, now = now + 1).newState
        assertEquals(LearningStage.MONTHLY, state.stage)

        state = useCase(state, isCorrect = true, now = now + 2).newState
        assertEquals(LearningStage.LEARNED, state.stage)
        assertEquals(Difficulty.EASY, state.difficulty)
    }

    @Test
    fun failureInMonthly_thenRecovery_endsLearnedButNotEasy() {
        var state = LearningState(conceptId = 1)

        state = useCase(state, isCorrect = true, now = now).newState      // Daily -> Weekly
        state = useCase(state, isCorrect = true, now = now + 1).newState  // Weekly -> Monthly
        state = useCase(state, isCorrect = false, now = now + 2).newState // Monthly fail -> Daily, HARD

        assertEquals(LearningStage.DAILY, state.stage)
        assertEquals(Difficulty.HARD, state.difficulty)
        assertEquals(1, state.monthlyWrongCount)
        assertTrue(state.everFailed)

        // چرخه دوباره از اول: Daily -> Weekly -> Monthly -> Learned (ولی EASY نمی‌شود چون قبلاً شکست خورده)
        state = useCase(state, isCorrect = true, now = now + 3).newState
        state = useCase(state, isCorrect = true, now = now + 4).newState
        state = useCase(state, isCorrect = true, now = now + 5).newState

        assertEquals(LearningStage.LEARNED, state.stage)
        assertEquals(Difficulty.HARD, state.difficulty) // نه EASY، چون everFailed=true بود
    }
}
