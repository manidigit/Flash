package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * تست‌های سیستم طبقه‌بندی سختی سازگار (Adaptive Difficulty Classification). ۱۰ سناریوی
 * دقیقاً مشخص‌شده در نیازمندی، به‌همراه تست‌های پایه انتقال مرحله و پیشگیری از تکراری.
 */
class ProcessReviewAnswerUseCaseTest {

    private val useCase = ProcessReviewAnswerUseCase()
    private val now = 1_700_000_000_000L

    // ---------- انتقال مراحل (پایه) ----------

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
        assertEquals(1, outcome.newState.dailyIncorrectCount)
    }

    @Test
    fun weekly_correct_movesToMonthly_withThirtyDayDelay() {
        val state = LearningState(conceptId = 1, stage = LearningStage.WEEKLY)
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.MONTHLY, outcome.newStage)
        assertEquals(now + TimeUnit.DAYS.toMillis(30), outcome.newState.nextReviewAt)
    }

    @Test
    fun weekly_incorrect_returnsToDaily_andRecordsReturn() {
        val state = LearningState(conceptId = 1, stage = LearningStage.WEEKLY, difficulty = Difficulty.EASY)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertTrue(outcome.newState.everFailed)
        assertEquals(1, outcome.newState.weeklyIncorrectCount)
        assertEquals(1, outcome.newState.weeklyToDailyReturns)
    }

    @Test
    fun monthly_correct_neverFailedBefore_becomesLearnedAndEasy() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, everFailed = false)
        val outcome = useCase(state, isCorrect = true, now = now)

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.EASY, outcome.newDifficulty)
        assertEquals(1, outcome.newState.monthlyCompletions)
        assertEquals(1, outcome.newState.learnedCount)
    }

    @Test
    fun monthly_incorrect_returnsToDaily_andRecordsReturn() {
        val state = LearningState(conceptId = 1, stage = LearningStage.MONTHLY, monthlyWrongCount = 0)
        val outcome = useCase(state, isCorrect = false, now = now)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertEquals(1, outcome.newState.monthlyWrongCount)
        assertEquals(1, outcome.newState.monthlyIncorrectCount)
        assertEquals(1, outcome.newState.monthlyToDailyReturns)
    }

    @Test
    fun learned_review_doesNotChangeStage_regardlessOfAnswer() {
        val state = LearningState(conceptId = 1, stage = LearningStage.LEARNED, difficulty = Difficulty.EASY)

        val correctOutcome = useCase(state, isCorrect = true, now = now)
        assertEquals(LearningStage.LEARNED, correctOutcome.newStage)

        val wrongOutcome = useCase(state, isCorrect = false, now = now)
        assertEquals(LearningStage.LEARNED, wrongOutcome.newStage)
    }

    // ---------- ۱۰ سناریوی دقیق نیازمندی ----------

    // TEST 1: DAILY correct, WEEKLY correct, MONTHLY correct => LEARNED, EASY
    @Test
    fun test1_perfectPath_endsLearnedAndEasy() {
        var state = LearningState(conceptId = 1)
        state = useCase(state, isCorrect = true, now = now).newState       // Daily correct
        state = useCase(state, isCorrect = true, now = now + 1).newState   // Weekly correct
        val outcome = useCase(state, isCorrect = true, now = now + 2)      // Monthly correct

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.EASY, outcome.newDifficulty)
    }

    // TEST 2: DAILY wrong, DAILY correct, WEEKLY correct, MONTHLY correct => LEARNED, MEDIUM
    @Test
    fun test2_oneDailyMistake_endsLearnedAndMedium() {
        var state = LearningState(conceptId = 1)
        state = useCase(state, isCorrect = false, now = now).newState      // Daily wrong
        state = useCase(state, isCorrect = true, now = now + 1).newState   // Daily correct
        state = useCase(state, isCorrect = true, now = now + 2).newState   // Weekly correct
        val outcome = useCase(state, isCorrect = true, now = now + 3)      // Monthly correct

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.MEDIUM, outcome.newDifficulty)
    }

    // TEST 3: DAILY correct, WEEKLY wrong, DAILY correct, WEEKLY correct, MONTHLY correct => LEARNED, MEDIUM
    @Test
    fun test3_oneWeeklyMistake_endsLearnedAndMedium() {
        var state = LearningState(conceptId = 1)
        state = useCase(state, isCorrect = true, now = now).newState       // Daily correct
        state = useCase(state, isCorrect = false, now = now + 1).newState  // Weekly wrong -> Daily
        state = useCase(state, isCorrect = true, now = now + 2).newState   // Daily correct
        state = useCase(state, isCorrect = true, now = now + 3).newState   // Weekly correct
        val outcome = useCase(state, isCorrect = true, now = now + 4)      // Monthly correct

        assertEquals(LearningStage.LEARNED, outcome.newStage)
        assertEquals(Difficulty.MEDIUM, outcome.newDifficulty)
    }

    // TEST 4: DAILY correct, WEEKLY wrong, DAILY correct, WEEKLY wrong, DAILY correct, WEEKLY correct
    // Expected: not EASY, at least HARD
    @Test
    fun test4_twoWeeklyMistakes_atLeastHard() {
        var state = LearningState(conceptId = 1)
        state = useCase(state, isCorrect = true, now = now).newState
        state = useCase(state, isCorrect = false, now = now + 1).newState
        state = useCase(state, isCorrect = true, now = now + 2).newState
        state = useCase(state, isCorrect = false, now = now + 3).newState
        state = useCase(state, isCorrect = true, now = now + 4).newState
        val outcome = useCase(state, isCorrect = true, now = now + 5)

        assertNotEquals(Difficulty.EASY, outcome.newDifficulty)
        assertTrue(
            outcome.newDifficulty == Difficulty.HARD || outcome.newDifficulty == Difficulty.VERY_HARD
        )
    }

    // TEST 5: DAILY correct, WEEKLY correct, MONTHLY wrong
    // Expected: return to DAILY, difficulty increases, monthlyIncorrect = 1
    @Test
    fun test5_singleMonthlyFailure_returnsToDailyAndIncreasesDifficulty() {
        var state = LearningState(conceptId = 1)
        state = useCase(state, isCorrect = true, now = now).newState
        state = useCase(state, isCorrect = true, now = now + 1).newState
        val outcome = useCase(state, isCorrect = false, now = now + 2)

        assertEquals(LearningStage.DAILY, outcome.newStage)
        assertNotEquals(Difficulty.EASY, outcome.newDifficulty)
        assertEquals(1, outcome.newState.monthlyIncorrectCount)
    }

    // TEST 6: (DAILY correct, WEEKLY correct, MONTHLY wrong) x 3
    // Expected: VERY_HARD or escalating strongly toward it
    @Test
    fun test6_threeRepeatedMonthlyFailures_becomesVeryHard() {
        var state = LearningState(conceptId = 1)
        var t = now
        repeat(3) {
            state = useCase(state, isCorrect = true, now = t++).newState   // Daily correct
            state = useCase(state, isCorrect = true, now = t++).newState   // Weekly correct
            state = useCase(state, isCorrect = false, now = t++).newState  // Monthly wrong -> Daily
        }

        assertEquals(Difficulty.VERY_HARD, state.difficulty)
        assertEquals(3, state.monthlyIncorrectCount)
        assertEquals(3, state.monthlyToDailyReturns)
    }

    // TEST 7: Repeated DAILY mistakes => difficulty increases progressively
    @Test
    fun test7_repeatedDailyMistakes_difficultyIncreasesProgressively() {
        var state = LearningState(conceptId = 1)
        val scores = mutableListOf<Int>()
        repeat(4) {
            state = useCase(state, isCorrect = false, now = now + it).newState
            scores.add(state.difficultyScore)
        }
        // امتیاز خام باید هر بار افزایش یابد (هرگز کاهش نمی‌یابد)
        for (i in 1 until scores.size) {
            assertTrue(scores[i] > scores[i - 1])
        }
    }

    // TEST 8: Several successful reviews after previous failures => difficulty can improve,
    // BUT historical review records (raw counters) remain unchanged.
    @Test
    fun test8_recoveryAfterFailures_improvesDifficultyButKeepsHistory() {
        var state = LearningState(conceptId = 1)
        var t = now
        // یک شکست MONTHLY برای رسیدن به HARD/VERY_HARD
        state = useCase(state, isCorrect = true, now = t++).newState
        state = useCase(state, isCorrect = true, now = t++).newState
        state = useCase(state, isCorrect = false, now = t++).newState // Monthly fail -> Daily

        val difficultyAfterFailure = state.difficulty
        val monthlyIncorrectAfterFailure = state.monthlyIncorrectCount
        val rawScoreAfterFailure = state.difficultyScore

        // چند موفقیت پشت‌سرهم
        repeat(6) {
            state = useCase(state, isCorrect = true, now = t++).newState
        }

        // تاریخچه واقعی هرگز پاک نمی‌شود
        assertEquals(monthlyIncorrectAfterFailure, state.monthlyIncorrectCount)
        assertTrue(state.difficultyScore >= rawScoreAfterFailure)
        // ولی سختی نمایشی می‌تواند بهتر شده باشد یا حداقل بدتر نشده باشد
        assertTrue(state.difficulty.ordinal <= difficultyAfterFailure.ordinal)
    }

    // TEST 9 و TEST 10 (چند ترجمه برای یک کلمه، و جلوگیری از ترجمه تکراری) در
    // ImportParsedEntriesUseCase و ConceptRepository پیاده‌سازی و تست می‌شوند، نه اینجا؛
    // چون به دیتابیس واقعی (Room) نیاز دارند که در این تست خالص pure منطقی جایی ندارد.

    // ---------- سناریوی کامل زنجیره‌ای (رگرسیون) ----------

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
}
