package com.app.flashlearn.domain.model

/**
 * وضعیت کامل یادگیری یک Concept. سیستم طبقه‌بندی سختی سازگار (Adaptive Difficulty):
 * تمام شمارنده‌های زیر تاریخچه‌ای‌اند و هرگز کاهش نمی‌یابند (نگاه کنید به
 * ProcessReviewAnswerUseCase و DifficultyScoreCalculator).
 */
data class LearningState(
    val conceptId: Long,
    val stage: LearningStage = LearningStage.DAILY,
    val difficulty: Difficulty = Difficulty.EASY,
    val nextReviewAt: Long? = null,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    val everFailed: Boolean = false,
    val dailyReviewCount: Int = 0,
    val dailyCorrectCount: Int = 0,
    val dailyIncorrectCount: Int = 0,
    val weeklyReviewCount: Int = 0,
    val weeklyCorrectCount: Int = 0,
    val weeklyIncorrectCount: Int = 0,
    val monthlyReviewCount: Int = 0,
    val monthlyCorrectCount: Int = 0,
    val monthlyIncorrectCount: Int = 0,
    val consecutiveCorrect: Int = 0,
    val consecutiveIncorrect: Int = 0,
    val highestStageReached: LearningStage = LearningStage.DAILY,
    val weeklyToDailyReturns: Int = 0,
    val monthlyToDailyReturns: Int = 0,
    val monthlyCompletions: Int = 0,
    val learnedCount: Int = 0,
    val lastReviewResult: Boolean? = null,
    val difficultyScore: Int = 0
)
