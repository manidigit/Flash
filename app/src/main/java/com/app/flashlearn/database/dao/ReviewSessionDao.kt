package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ReviewSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewSessionDao {
    @Insert
    suspend fun insert(session: ReviewSessionEntity): Long

    @Update
    suspend fun update(session: ReviewSessionEntity)

    @Query("SELECT * FROM review_session WHERE id = :id")
    suspend fun getById(id: String): ReviewSessionEntity?

    @Query("SELECT * FROM review_session ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<ReviewSessionEntity>>

    @Query("SELECT COUNT(*) FROM review_session WHERE reviewType = :type")
    fun getCountByType(type: String): Flow<Int>
}
