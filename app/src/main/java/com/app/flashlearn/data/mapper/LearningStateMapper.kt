package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.LearningStateEntity
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState

fun LearningStateEntity.toDomain(): LearningState = LearningState(
    conceptId = conceptId,
    stage = LearningStage.valueOf(stage),
    difficulty = Difficulty.valueOf(difficulty),
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt,
    everFailed = everFailed
)

fun LearningState.toEntity(): LearningStateEntity = LearningStateEntity(
    conceptId = conceptId,
    stage = stage.name,
    difficulty = difficulty.name,
    nextReviewAt = nextReviewAt,
    monthlyWrongCount = monthlyWrongCount,
    totalCorrect = totalCorrect,
    totalWrong = totalWrong,
    lastReviewedAt = lastReviewedAt,
    everFailed = everFailed
)
