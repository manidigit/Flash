package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.*
import com.app.flashlearn.domain.model.*

fun ConceptEntity.toDomain(): Concept = Concept(
    id = id,
    uuid = uuid,
    contentType = ContentType.valueOf(contentType),
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Concept.toEntity(): ConceptEntity = ConceptEntity(
    id = id,
    uuid = uuid,
    contentType = contentType.name,
    categoryId = categoryId,
    favorite = favorite,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ContentEntity.toDomain(): Content = Content(
    id = id,
    conceptId = conceptId,
    languageCode = languageCode,
    text = text,
    pronunciation = pronunciation,
    definition = definition,
    example = example,
    grammarNote = grammarNote,
    usageNote = usageNote
)

fun Content.toEntity(): ContentEntity = ContentEntity(
    id = id,
    conceptId = conceptId,
    languageCode = languageCode,
    text = text,
    pronunciation = pronunciation,
    definition = definition,
    example = example,
    grammarNote = grammarNote,
    usageNote = usageNote
)

fun LearningStateEntity.toDomain(): LearningState = LearningState(
    conceptId = conceptId,
    stage = ReviewStage.valueOf(stage),
    difficulty = Difficulty.valueOf(difficulty),
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt
)

fun LearningState.toEntity(): LearningStateEntity = LearningStateEntity(
    conceptId = conceptId,
    stage = stage.name,
    difficulty = difficulty.name,
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt
)

fun ReviewSessionEntity.toDomain(): ReviewSession = ReviewSession(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    reviewType = reviewType
)

fun ReviewSession.toEntity(): ReviewSessionEntity = ReviewSessionEntity(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    reviewType = reviewType
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    isCustom = isCustom
)

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name
)

fun LanguageEntity.toDomain(): Language = Language(
    code = code,
    displayName = displayName
)

fun LanguagePairEntity.toDomain(): LanguagePair = LanguagePair(
    id = id,
    sourceLanguage = sourceLanguage,
    targetLanguage = targetLanguage,
    isActive = isActive
)

fun AppSettingEntity.toDomain(): AppSetting = AppSetting(
    key = key,
    value = value,
    updatedAt = updatedAt
)
