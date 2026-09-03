package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.LearningStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: LearningStateEntity)

    @Query("SELECT * FROM learning_states WHERE conceptId = :conceptId")
    suspend fun get(conceptId: Long): LearningStateEntity?

    @Query(
        """
        SELECT ls.* FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = :stage AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        AND (ls.nextReviewAt IS NULL OR ls.nextReviewAt <= :now)
        ORDER BY ls.nextReviewAt ASC
        LIMIT :limit
        """
    )
    suspend fun getDue(stage: String, now: Long, limit: Int, categoryId: Long?): List<LearningStateEntity>

    @Query(
        """
        SELECT ls.* FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.difficulty = :difficulty AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getByDifficulty(difficulty: String, limit: Int, offset: Int, categoryId: Long?): List<LearningStateEntity>

    @Query(
        """
        SELECT ls.* FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = 'LEARNED' AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getLearned(limit: Int, offset: Int, categoryId: Long?): List<LearningStateEntity>

    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = :stage AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        """
    )
    suspend fun countTotal(stage: String, categoryId: Long?): Int

    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = :stage AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        AND (ls.nextReviewAt IS NULL OR ls.nextReviewAt <= :now)
        """
    )
    suspend fun countDue(stage: String, now: Long, categoryId: Long?): Int

    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.difficulty = :difficulty AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        """
    )
    suspend fun countByDifficulty(difficulty: String, categoryId: Long?): Int

    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = 'LEARNED' AND c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        """
    )
    suspend fun countLearned(categoryId: Long?): Int

    @Query(
        """
        SELECT ls.stage AS stage, COUNT(*) AS count FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE c.active = 1
        GROUP BY ls.stage
        """
    )
    suspend fun getStageSummaryRaw(): List<StageCount>

    @Query(
        """
        SELECT ls.difficulty AS difficulty, COUNT(*) AS count FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE c.active = 1
        GROUP BY ls.difficulty
        """
    )
    suspend fun getDifficultySummaryRaw(): List<DifficultyCount>

    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = 'LEARNED' AND c.active = 1
        """
    )
    fun getLearnedCount(): Flow<Int>

    @Query(
        """
        SELECT ls.* FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = :stage AND c.active = 1
        AND (ls.nextReviewAt IS NULL OR ls.nextReviewAt <= :now)
        """
    )
    fun getReadyForReview(stage: String, now: Long): Flow<List<LearningStateEntity>>

    /** نسخه Flow-محور برای HomeViewModel که مستقیم به DAO تزریق می‌شود. */
    @Query(
        """
        SELECT COUNT(*) FROM learning_states ls
        INNER JOIN concepts c ON c.id = ls.conceptId
        WHERE ls.stage = :stage AND c.active = 1
        """
    )
    fun getCountByStage(stage: String): Flow<Int>
}

data class StageCount(val stage: String, val count: Int)
data class DifficultyCount(val difficulty: String, val count: Int)
