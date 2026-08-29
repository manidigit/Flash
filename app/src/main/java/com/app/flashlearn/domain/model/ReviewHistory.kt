package com.app.flashlearn.domain.model

/**
 * یک رکورد تاریخچه مرور. هرگز ویرایش/حذف نمی‌شود؛ هر مرور یک رکورد جدید می‌سازد
 * (طبق نیازمندی «The review history must never be overwritten»).
 */
data class ReviewHistory(
    val id: Long,
    val conceptId: Long,
    val sessionId: String?,
    val reviewStage: String,
    val reviewDate: Long,
    val isCorrect: Boolean,
    val previousStatus: String,
    val newStatus: String,
    val previousDifficulty: String,
    val newDifficulty: String,
    val responseTimeMs: Long?
)
