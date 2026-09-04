package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.ReviewSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: ReviewSessionEntity)

    @Query("UPDATE review_session SET endedAt = :endedAt WHERE id = :sessionId AND endedAt IS NULL")
    suspend fun closeSession(sessionId: String, endedAt: Long)

    @Query("SELECT * FROM review_session WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ReviewSessionEntity?

    @Query("SELECT * FROM review_session WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ReviewSessionEntity?>
}
