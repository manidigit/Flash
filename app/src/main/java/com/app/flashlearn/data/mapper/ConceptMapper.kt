package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ContentEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType

fun ConceptEntity.toDomain(contents: List<ContentEntity>, tags: List<String>): Concept = Concept(
    id = id,
    uuid = uuid,
    contentType = ContentType.valueOf(contentType),
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
    notes = notes,
    contents = contents.map { it.toDomain() },
    tags = tags
)

fun Concept.toEntity(): ConceptEntity = ConceptEntity(
    id = id,
    uuid = uuid,
    contentType = contentType.name,
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt,
    notes = notes
)

fun ContentEntity.toDomain(): ContentItem = ContentItem(
    languageCode = languageCode,
    text = text,
    pronunciation = pronunciation,
    definition = definition,
    example = example,
    grammarNote = grammarNote,
    usageNote = usageNote
)

fun ContentItem.toEntity(conceptId: Long): ContentEntity = ContentEntity(
    conceptId = conceptId,
    languageCode = languageCode,
    text = text,
    pronunciation = pronunciation,
    definition = definition,
    example = example,
    grammarNote = grammarNote,
    usageNote = usageNote
)
