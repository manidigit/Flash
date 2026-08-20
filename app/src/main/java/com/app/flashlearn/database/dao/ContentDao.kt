package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ContentEntity

@Dao
interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: ContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contents: List<ContentEntity>)

    @Update
    suspend fun update(content: ContentEntity)

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId")
    suspend fun getForConcept(conceptId: Long): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId AND languageCode = :languageCode LIMIT 1")
    suspend fun getForConceptAndLanguage(conceptId: Long, languageCode: String): ContentEntity?

    /**
     * برای تشخیص تکراری بودن هنگام Import (بند 64، رفع باگ: کپی چندباره یک کلمه هنگام
     * Paste/Import هیچ‌وقت به‌عنوان Duplicate تشخیص داده نمی‌شد). فقط بین Concept های
     * فعال (active=1) و بر اساس متن نرمال‌شده (Trim + lower-case) در همان زبان مقایسه
     * می‌شود، تا اختلاف فاصله یا بزرگی/کوچکی حروف باعث درج تکراری نشود.
     */
    @Query(
        """
        SELECT COUNT(*) FROM contents c
        INNER JOIN concepts co ON co.id = c.conceptId
        WHERE co.active = 1
          AND c.languageCode = :languageCode
          AND LOWER(TRIM(c.text)) = LOWER(TRIM(:text))
        """
    )
    suspend fun countActiveByText(languageCode: String, text: String): Int

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteAllForConcept(conceptId: Long)
}
