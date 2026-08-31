package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_session")
data class ReviewSessionEntity(
    @PrimaryKey
    val id: String, // مثال: "2026-08-31-001"
    val startedAt: Long,
    val endedAt: Long? = null,
    val reviewType: String // DAILY / WEEKLY / MONTHLY / RANDOM
)
