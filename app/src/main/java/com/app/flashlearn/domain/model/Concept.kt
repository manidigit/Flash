package com.app.flashlearn.domain.model

data class Concept(
    val id: Long,
    val uuid: String,
    val contentType: String,
    val categoryId: Long?,
    val favorite: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val contents: List<Content> = emptyList(),
    val tags: List<String> = emptyList(),
    val learningState: LearningState? = null
)
