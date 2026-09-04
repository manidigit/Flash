package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ReviewHistoryEntity

data class DailyCount(val day: String, val count: Int)
data class AccuracySummary(val correctCount: Int, val totalCount: Int)

@Dao
interface ReviewHistoryDao {
    @Insert
    suspend fun insert(history: ReviewHistoryEntity)

    @Query("SELECT * FROM review_histories WHERE conceptId = :conceptId ORDER BY reviewDate DESC")
    suspend fun getByConceptId(conceptId: Long): List<ReviewHistoryEntity>

    @Query("""
        SELECT date(reviewDate / 1000, \x27unixepoch\x27, \x27localtime\x27) as day, COUNT(*) as count
        FROM review_histories
        WHERE reviewDate >= :sinceEpochMillis
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getDailyReviewCounts(sinceEpochMillis: Long): List<DailyCount>

    @Query("""
        SELECT DISTINCT date(reviewDate / 1000, \x27unixepoch\x27, \x27localtime\x27) as day
        FROM review_histories
        ORDER BY day DESC
    """)
    suspend fun getDistinctReviewDays(): List<String>

    @Query("""
        SELECT
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount,
            COUNT(*) as totalCount
        FROM review_histories
        WHERE reviewDate >= :sinceEpochMillis
    """)
    suspend fun getAccuracySummary(sinceEpochMillis: Long): AccuracySummary
}
