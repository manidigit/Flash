package com.app.flashlearn.domain.model

data class Concept(
    val id: Long = 0,
    val uuid: String,
    val contentType: ContentType,
    val categoryId: Long?,
    val favorite: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val contents: List<Content> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val learningState: LearningState? = null
)
