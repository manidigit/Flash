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
    everFailed = everFailed,
    dailyReviewCount = dailyReviewCount,
    dailyCorrectCount = dailyCorrectCount,
    dailyIncorrectCount = dailyIncorrectCount,
    weeklyReviewCount = weeklyReviewCount,
    weeklyCorrectCount = weeklyCorrectCount,
    weeklyIncorrectCount = weeklyIncorrectCount,
    monthlyReviewCount = monthlyReviewCount,
    monthlyCorrectCount = monthlyCorrectCount,
    monthlyIncorrectCount = monthlyIncorrectCount,
    consecutiveCorrect = consecutiveCorrect,
    consecutiveIncorrect = consecutiveIncorrect,
    highestStageReached = LearningStage.valueOf(highestStageReached),
    weeklyToDailyReturns = weeklyToDailyReturns,
    monthlyToDailyReturns = monthlyToDailyReturns,
    monthlyCompletions = monthlyCompletions,
    learnedCount = learnedCount,
    lastReviewResult = lastReviewResult,
    difficultyScore = difficultyScore
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
    everFailed = everFailed,
    dailyReviewCount = dailyReviewCount,
    dailyCorrectCount = dailyCorrectCount,
    dailyIncorrectCount = dailyIncorrectCount,
    weeklyReviewCount = weeklyReviewCount,
    weeklyCorrectCount = weeklyCorrectCount,
    weeklyIncorrectCount = weeklyIncorrectCount,
    monthlyReviewCount = monthlyReviewCount,
    monthlyCorrectCount = monthlyCorrectCount,
    monthlyIncorrectCount = monthlyIncorrectCount,
    consecutiveCorrect = consecutiveCorrect,
    consecutiveIncorrect = consecutiveIncorrect,
    highestStageReached = highestStageReached.name,
    weeklyToDailyReturns = weeklyToDailyReturns,
    monthlyToDailyReturns = monthlyToDailyReturns,
    monthlyCompletions = monthlyCompletions,
    learnedCount = learnedCount,
    lastReviewResult = lastReviewResult,
    difficultyScore = difficultyScore
)
