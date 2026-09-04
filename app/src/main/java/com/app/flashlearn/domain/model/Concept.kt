package com.app.flashlearn.domain.model

data class Concept(
    val id: Long,
    val uuid: String,
    val contentType: ContentType,
    val categoryId: Long?,
    val favorite: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

enum class ContentType {
    WORD, PHRASE, SENTENCE, IDIOM, VERB, EXPRESSION, DIALOGUE
}
