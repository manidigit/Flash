package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Difficulty

interface StatisticsRepository {
    suspend fun getDailyReviewCounts(sinceEpochMillis: Long): List<Pair<String, Int>>
    suspend fun getDifficultyDistribution(): Map<Difficulty, Int>
    suspend fun getAccuracySummary(sinceEpochMillis: Long): Pair<Int, Int>
}
