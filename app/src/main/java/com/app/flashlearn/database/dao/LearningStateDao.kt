package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.LearningStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningStateDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(state: LearningStateEntity)

    @Update
    suspend fun update(state: LearningStateEntity)

    @Query("SELECT * FROM learning_state WHERE conceptId = :conceptId LIMIT 1")
    suspend fun findByConcept(conceptId: Long): LearningStateEntity?

    @Query("SELECT * FROM learning_state WHERE conceptId = :conceptId LIMIT 1")
    fun observeByConcept(conceptId: Long): Flow<LearningStateEntity?>

    @Query(
        """
        SELECT * FROM learning_state
        WHERE stage = :stage
          AND nextReviewAt <= :now
        ORDER BY nextReviewAt ASC, conceptId ASC
        """
    )
    suspend fun findDueByStage(stage: String, now: Long): List<LearningStateEntity>

    @Query("""
        UPDATE learning_state
        SET stage = :newStage,
            difficulty = :newDifficulty,
            nextReviewAt = :newNextReviewAt,
            monthlyWrongCount = :newMonthlyWrongCount,
            totalCorrect = :newTotalCorrect,
            totalWrong = :newTotalWrong,
            lastReviewedAt = :newLastReviewedAt
        WHERE conceptId = :conceptId
          AND stage = :expectedStage
          AND difficulty = :expectedDifficulty
    """)
    suspend fun updateWithExpectedState(
        conceptId: Long,
        expectedStage: String,
        expectedDifficulty: String,
        newStage: String,
        newDifficulty: String,
        newNextReviewAt: Long?,
        newMonthlyWrongCount: Int,
        newTotalCorrect: Int,
        newTotalWrong: Int,
        newLastReviewedAt: Long?
    ): Int
}
