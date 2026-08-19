package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.LearningStateEntity

/**
 * تمام Query های این DAO مستقیماً منطق بند 20/22/31 نیازمندی‌ها را پیاده‌سازی می‌کنند:
 * یک کارت فقط زمانی "آماده مرور" است که nextReviewAt <= now باشد.
 */
@Dao
interface LearningStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: LearningStateEntity)

    @Update
    suspend fun update(state: LearningStateEntity)

    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId")
    suspend fun getForConcept(conceptId: Long): LearningStateEntity?

    @Query(
        """
        SELECT * FROM learning_states
        WHERE stage = :stage AND (nextReviewAt IS NULL OR nextReviewAt <= :now)
        ORDER BY nextReviewAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueForStage(stage: String, now: Long, limit: Int): List<LearningStateEntity>

    @Query(
        """
        SELECT learning_states.* FROM learning_states
        INNER JOIN concepts ON concepts.id = learning_states.conceptId
        WHERE learning_states.stage = :stage
          AND (learning_states.nextReviewAt IS NULL OR learning_states.nextReviewAt <= :now)
          AND concepts.categoryId = :categoryId
        ORDER BY learning_states.nextReviewAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDueForStageInCategory(stage: String, now: Long, categoryId: Long, limit: Int): List<LearningStateEntity>

    @Query(
        "SELECT COUNT(*) FROM learning_states WHERE stage = :stage AND (nextReviewAt IS NULL OR nextReviewAt <= :now)"
    )
    suspend fun countDueForStage(stage: String, now: Long): Int

    @Query("SELECT * FROM learning_states WHERE difficulty = :difficulty LIMIT :limit OFFSET :offset")
    suspend fun getByDifficulty(difficulty: String, limit: Int, offset: Int): List<LearningStateEntity>

    @Query(
        """
        SELECT learning_states.* FROM learning_states
        INNER JOIN concepts ON concepts.id = learning_states.conceptId
        WHERE learning_states.difficulty = :difficulty AND concepts.categoryId = :categoryId
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getByDifficultyInCategory(difficulty: String, categoryId: Long, limit: Int, offset: Int): List<LearningStateEntity>

    @Query("SELECT * FROM learning_states WHERE stage = 'LEARNED' LIMIT :limit OFFSET :offset")
    suspend fun getLearned(limit: Int, offset: Int): List<LearningStateEntity>

    @Query(
        """
        SELECT learning_states.* FROM learning_states
        INNER JOIN concepts ON concepts.id = learning_states.conceptId
        WHERE learning_states.stage = 'LEARNED' AND concepts.categoryId = :categoryId
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getLearnedInCategory(categoryId: Long, limit: Int, offset: Int): List<LearningStateEntity>

    @Query(
        """
        SELECT difficulty, COUNT(*) as count FROM learning_states
        GROUP BY difficulty
        """
    )
    suspend fun getDifficultySummary(): List<DifficultyCount>

    @Query(
        """
        SELECT stage, COUNT(*) as count FROM learning_states
        GROUP BY stage
        """
    )
    suspend fun getStageSummary(): List<StageCount>
}

data class DifficultyCount(
    val difficulty: String,
    val count: Int
)

data class StageCount(
    val stage: String,
    val count: Int
)
