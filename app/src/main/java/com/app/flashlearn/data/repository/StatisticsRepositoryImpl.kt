package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDifficulty
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.repository.StatisticsRepository
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val reviewHistoryDao: ReviewHistoryDao,
    private val learningStateDao: LearningStateDao
) : StatisticsRepository {
    override suspend fun getDailyReviewCounts(sinceEpochMillis: Long) =
        reviewHistoryDao.getDailyReviewCounts(sinceEpochMillis).map { it.day to it.count }

    override suspend fun getDifficultyDistribution(): Map<Difficulty, Int> =
        learningStateDao.getDifficultyDistribution().associate { it.difficulty.toDifficulty() to it.count }

    override suspend fun getAccuracySummary(sinceEpochMillis: Long): Pair<Int, Int> {
        val summary = reviewHistoryDao.getAccuracySummary(sinceEpochMillis)
        return summary.correctCount to summary.totalCount
    }
}
