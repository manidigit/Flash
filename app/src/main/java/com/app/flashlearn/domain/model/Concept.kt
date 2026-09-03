package com.app.flashlearn.domain.model

data class Concept(
    val id: Long,
    val uuid: String,
    val contentType: ContentType,
    val categoryId: Long?,
    val favorite: Boolean,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String? = null,
    val contents: List<ContentItem> = emptyList(),
    val tags: List<String> = emptyList()
)

/** اولین معنی موجود در یک زبان مشخص (بند 9). */
fun Concept.contentFor(languageCode: String): ContentItem? =
    contents.firstOrNull { it.languageCode == languageCode }

/** همه معنی‌های موجود در یک زبان مشخص (بند 64 - کلمه با چند معنی). */
fun Concept.contentsFor(languageCode: String): List<ContentItem> =
    contents.filter { it.languageCode == languageCode }
