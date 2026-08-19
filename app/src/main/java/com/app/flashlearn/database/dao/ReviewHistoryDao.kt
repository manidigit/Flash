package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import com.app.flashlearn.database.entity.ReviewHistoryEntity

@Dao
interface ReviewHistoryDao {

    @Insert
    suspend fun insert(history: ReviewHistoryEntity): Long

    @androidx.room.Query("SELECT * FROM review_history WHERE conceptId = :conceptId ORDER BY reviewDate DESC")
    suspend fun getForConcept(conceptId: Long): List<ReviewHistoryEntity>

    @androidx.room.Query("SELECT * FROM review_history WHERE sessionId = :sessionId ORDER BY reviewDate ASC")
    suspend fun getForSession(sessionId: String): List<ReviewHistoryEntity>

    @androidx.room.Query(
        "SELECT COUNT(*) FROM review_history WHERE reviewDate >= :from AND reviewDate < :to AND isCorrect = 1"
    )
    suspend fun countCorrectBetween(from: Long, to: Long): Int

    @androidx.room.Query(
        "SELECT COUNT(*) FROM review_history WHERE reviewDate >= :from AND reviewDate < :to"
    )
    suspend fun countTotalBetween(from: Long, to: Long): Int
}
