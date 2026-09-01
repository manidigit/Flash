package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ConceptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Insert
    suspend fun insert(concept: ConceptEntity): Long

    @Update
    suspend fun update(concept: ConceptEntity)

    @Delete
    suspend fun delete(concept: ConceptEntity)

    @Query("SELECT * FROM concept WHERE id = :id")
    suspend fun getById(id: Long): ConceptEntity?

    @Query("SELECT * FROM concept WHERE active = 1")
    fun getAllActive(): Flow<List<ConceptEntity>>

    @Query("SELECT COUNT(*) FROM concept WHERE active = 1")
    fun getActiveCount(): Flow<Int>
}
