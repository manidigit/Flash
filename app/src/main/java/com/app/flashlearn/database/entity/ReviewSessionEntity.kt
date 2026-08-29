package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یک جلسه مرور. id به‌صورت خوانا تولید می‌شود، مثلاً "2026-08-16-001".
 */
@Entity(tableName = "review_sessions")
data class ReviewSessionEntity(
    @PrimaryKey
    val id: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    // RANDOM, DAILY, WEEKLY, MONTHLY, EASY, MEDIUM, HARD, VERY_HARD, LEARNED
    val reviewType: String
)
