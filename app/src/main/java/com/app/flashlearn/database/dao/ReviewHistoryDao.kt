package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.ReviewHistoryEntity

@Dao
interface ReviewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(history: ReviewHistoryEntity)

    @Query("SELECT * FROM review_history WHERE conceptId = :conceptId ORDER BY reviewDate DESC, id DESC")
    suspend fun findByConcept(conceptId: Long): List<ReviewHistoryEntity>

    @Query("SELECT * FROM review_history WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun findBySession(sessionId: String): List<ReviewHistoryEntity>

    @Query("SELECT * FROM review_history WHERE sessionId = :sessionId AND reviewAttemptId = :attemptId LIMIT 1")
    suspend fun findByAttempt(sessionId: String, attemptId: String): ReviewHistoryEntity?
}
