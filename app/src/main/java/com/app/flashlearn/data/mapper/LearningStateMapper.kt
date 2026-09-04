package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.LearningStateEntity
import com.app.flashlearn.domain.model.LearningState

fun LearningStateEntity.toDomain(): LearningState = LearningState(
    conceptId = conceptId,
    stage = stage.toReviewStage(),
    difficulty = difficulty.toDifficulty(),
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt
)

fun LearningState.toEntity(): LearningStateEntity = LearningStateEntity(
    conceptId = conceptId,
    stage = stage.toDbString(),
    difficulty = difficulty.toDbString(),
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt
)
