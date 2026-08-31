package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.LearningStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStateDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(state: LearningStateEntity)

    @Update
    suspend fun update(state: LearningStateEntity)

    @Delete
    suspend fun delete(state: LearningStateEntity)

    @Query("SELECT * FROM learning_state WHERE conceptId = :conceptId")
    suspend fun getByConceptId(conceptId: Long): LearningStateEntity?

    @Query("SELECT * FROM learning_state WHERE stage = :stage AND stage != 'LEARNED' AND nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    fun getReadyForReview(stage: String, now: Long): Flow<List<LearningStateEntity>>

    @Query("SELECT * FROM learning_state WHERE stage = :stage")
    fun getByStage(stage: String): Flow<List<LearningStateEntity>>

    @Query("SELECT COUNT(*) FROM learning_state WHERE stage = :stage")
    fun getCountByStage(stage: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_state WHERE difficulty = :difficulty AND stage != 'LEARNED'")
    fun getCountByDifficulty(difficulty: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_state WHERE stage = 'WEEKLY' AND nextReviewAt <= :now")
    fun getDueWeekly(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_state WHERE stage = 'MONTHLY' AND nextReviewAt <= :now")
    fun getDueMonthly(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_state WHERE stage = 'DAILY' AND nextReviewAt <= :now")
    fun getDueDaily(now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_state WHERE stage = 'LEARNED'")
    fun getLearnedCount(): Flow<Int>
}
