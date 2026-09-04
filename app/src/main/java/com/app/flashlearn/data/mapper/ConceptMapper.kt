package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.domain.model.Concept

fun ConceptEntity.toDomain(): Concept = Concept(
    id = id,
    uuid = uuid,
    contentType = contentType.toContentType(),
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Concept.toEntity(): ConceptEntity = ConceptEntity(
    id = id,
    uuid = uuid,
    contentType = contentType.toDbString(),
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)
