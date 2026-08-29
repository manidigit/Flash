package com.app.flashlearn.domain.model

/**
 * خروجی پردازش یک پاسخ. newState برای ذخیره در LearningState استفاده می‌شود
 * و بقیه فیلدها مستقیماً برای ساخت رکورد ReviewHistory (که هرگز حذف نمی‌شود) لازم‌اند.
 */
data class ReviewOutcome(
    val newState: LearningState,
    val previousStage: LearningStage,
    val newStage: LearningStage,
    val previousDifficulty: Difficulty,
    val newDifficulty: Difficulty
)
