package com.app.flashlearn.domain.model

/**
 * وضعیت مرور یک Concept.
 * everFailed مشخص می‌کند آیا این کلمه تا به حال حتی یک بار در هر مرحله‌ای غلط جواب داده شده.
 *
 * فیلدهای آماری دقیق (به‌ازای هر مرحله) برای محاسبه خودکار Difficulty از روی کل تاریخچه
 * واقعی مرور استفاده می‌شوند (نه فقط یک شمارنده ساده). این فیلدها هرگز کاهش داده نمی‌شوند؛
 * فقط جمع می‌شوند تا تاریخچه واقعی همیشه دست‌نخورده و قابل بازسازی بماند. Difficulty نمایشی
 * از روی این آمار (به‌همراه یک مکانیزم بهبود/Recovery برای موفقیت‌های اخیر) محاسبه می‌شود
 * و در فیلد جداگانه [difficultyScore] هم امتیاز خام نگه داشته می‌شود تا همیشه قابل توضیح
 * باشد که چرا یک کلمه EASY/MEDIUM/HARD/VERY_HARD شده (بند «Implementation Quality»).
 */
data class LearningState(
    val conceptId: Long,
    val stage: LearningStage = LearningStage.DAILY,
    val difficulty: Difficulty = Difficulty.MEDIUM,
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
