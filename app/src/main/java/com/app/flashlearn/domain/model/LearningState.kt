package com.app.flashlearn.domain.model

/**
 * وضعیت مرور یک Concept.
 * everFailed مشخص می‌کند آیا این کلمه تا به حال حتی یک بار در هر مرحله‌ای غلط جواب داده شده.
 * این فیلد برای تعیین Difficulty=EASY در لحظه رسیدن به LEARNED لازم است (طبق بند 26):
 * فقط کلمه‌ای که در Daily، Weekly و Monthly همیشه از اول درست بوده EASY می‌شود.
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
    val everFailed: Boolean = false
)
