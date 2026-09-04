package com.app.flashlearn.domain.model

data class ReviewSession(
    val id: String,
    val startedAt: Long,
    val endedAt: Long?,
    val reviewType: String
)
