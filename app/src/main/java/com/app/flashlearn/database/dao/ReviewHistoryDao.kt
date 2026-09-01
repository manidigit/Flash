package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {
    @Insert
    suspend fun insert(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history WHERE conceptId = :conceptId")
    fun getByConceptId(conceptId: Long): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): List<ReviewHistoryEntity>

    @Query("SELECT COUNT(*) FROM review_history WHERE isCorrect = 1")
    fun getTotalCorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_history")
    fun getTotalReviews(): Flow<Int>
}
