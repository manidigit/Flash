package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.LearningStateEntity

data class DifficultyCount(val difficulty: String, val count: Int)

@Dao
interface LearningStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: LearningStateEntity)

    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId")
    suspend fun getByConceptId(conceptId: Long): LearningStateEntity?

    @Query("""
        SELECT c.* FROM concepts c
        JOIN learning_states ls ON ls.conceptId = c.id
        WHERE ls.stage = :stage AND (ls.nextReviewAt IS NULL OR ls.nextReviewAt <= :now)
        AND c.active = 1
    """)
    suspend fun getDueConcepts(stage: String, now: Long): List<ConceptEntity>

    @Query("SELECT COUNT(*) FROM learning_states WHERE stage = :stage")
    suspend fun countByStage(stage: String): Int

    @Query("""
        SELECT difficulty, COUNT(*) as count
        FROM learning_states
        GROUP BY difficulty
    """)
    suspend fun getDifficultyDistribution(): List<DifficultyCount>
}
