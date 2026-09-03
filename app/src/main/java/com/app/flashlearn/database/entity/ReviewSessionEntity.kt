package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_sessions")
data class ReviewSessionEntity(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val reviewType: String
)
