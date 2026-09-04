package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.ConceptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(concept: ConceptEntity): Long

    @Update
    suspend fun update(concept: ConceptEntity)

    @Query("UPDATE concept SET active = 0 WHERE id = :conceptId")
    suspend fun deactivate(conceptId: Long)

    @Query("SELECT * FROM concept WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ConceptEntity?

    @Query("SELECT * FROM concept WHERE uuid = :uuid LIMIT 1")
    suspend fun findByUuid(uuid: String): ConceptEntity?

    @Query("SELECT * FROM concept WHERE active = 1 ORDER BY id")
    fun observeAllActive(): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM concept WHERE active = 1 AND favorite = 1 ORDER BY id")
    fun observeFavorites(): Flow<List<ConceptEntity>>

    @Query("SELECT * FROM concept WHERE active = 1 AND id IN (:ids)")
    suspend fun findByIds(ids: List<Long>): List<ConceptEntity>
}
