package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * وضعیت مرور یک Concept. رابطه یک‌به‌یک با ConceptEntity.
 * stage و difficulty عمداً از هم جدا نگه داشته شده‌اند (طبق نیازمندی).
 *
 * فیلدهای آماری دقیق (نسخه 3 دیتابیس) برای محاسبه خودکار Difficulty از روی کل تاریخچه
 * مرور، نه فقط یک شمارنده ساده. هیچ‌کدام از این فیلدها هرگز کاهش داده نمی‌شوند (فقط جمع
 * می‌شوند)، تا تاریخچه واقعی همیشه قابل بازسازی باشد؛ Difficulty نمایشی از روی این آمار
 * محاسبه می‌شود، نه این‌که مستقیم دستکاری شود.
 */
@Entity(
    tableName = "learning_states",
    foreignKeys = [
        ForeignKey(
            entity = ConceptEntity::class,
            parentColumns = ["id"],
            childColumns = ["conceptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["stage"]),
        Index(value = ["difficulty"]),
        Index(value = ["nextReviewAt"])
    ]
)
data class LearningStateEntity(
    @PrimaryKey
    val conceptId: Long,
    // DAILY, WEEKLY, MONTHLY, LEARNED
    val stage: String = "DAILY",
    // EASY, MEDIUM, HARD, VERY_HARD
    val difficulty: String = "MEDIUM",
    val nextReviewAt: Long? = null,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null,
    // مشخص می‌کند این Concept تا به حال حتی یک‌بار در هر مرحله‌ای اشتباه جواب داده شده یا نه؛
    // برای تعیین Difficulty=EASY در لحظه رسیدن به LEARNED لازم است (بند 26).
    val everFailed: Boolean = false,

    // --- آمار دقیق به‌ازای هر مرحله (نسخه 3) ---
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
    // بالاترین مرحله‌ای که این کلمه تا به حال به آن رسیده (برای این‌که مشخص شود آیا کلمه‌ای
    // که الان دوباره در DAILY است قبلاً به WEEKLY/MONTHLY/LEARNED هم رسیده بوده یا نه).
    val highestStageReached: String = "DAILY",
    val weeklyToDailyReturns: Int = 0,
    val monthlyToDailyReturns: Int = 0,
    val monthlyCompletions: Int = 0,
    val learnedCount: Int = 0,
    val lastReviewResult: Boolean? = null,
    // امتیاز خام و انباشتی سختی (هرگز کاهش نمی‌یابد؛ Difficulty نمایشی از روی این امتیاز +
    // مکانیزم بهبود/Recovery محاسبه می‌شود، نه این‌که خودش مستقیم Difficulty باشد).
    val difficultyScore: Int = 0
)
