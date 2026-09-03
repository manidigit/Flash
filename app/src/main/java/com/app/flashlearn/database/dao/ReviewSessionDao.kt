package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ReviewSessionEntity

@Dao
interface ReviewSessionDao {
    @Insert
    suspend fun insert(session: ReviewSessionEntity): Long

    @Update
    suspend fun update(session: ReviewSessionEntity)

    @Query("SELECT * FROM review_sessions WHERE id = :id")
    suspend fun getById(id: String): ReviewSessionEntity?
}
