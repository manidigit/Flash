package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {
    @Insert
    suspend fun insert(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history WHERE conceptId = :conceptId ORDER BY reviewDate DESC")
    suspend fun getForConcept(conceptId: Long): List<ReviewHistoryEntity>

    @Query("SELECT COUNT(*) FROM review_history WHERE isCorrect = 1 AND reviewDate >= :from AND reviewDate <= :to")
    suspend fun countCorrectBetween(from: Long, to: Long): Int

    @Query("SELECT COUNT(*) FROM review_history WHERE reviewDate >= :from AND reviewDate <= :to")
    suspend fun countTotalBetween(from: Long, to: Long): Int

    /** تعداد کلماتی که حداقل یک‌بار مرور شده‌اند (بند: کلمات تمرین‌شده). */
    @Query("SELECT COUNT(DISTINCT conceptId) FROM review_history")
    fun getPracticedConceptCount(): Flow<Int>

    /** برای محاسبه Streak: تاریخ تمام مرورهای انجام‌شده. */
    @Query("SELECT DISTINCT reviewDate FROM review_history ORDER BY reviewDate DESC")
    fun getReviewDates(): Flow<List<Long>>
}
