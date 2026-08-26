package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ConceptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(concept: ConceptEntity): Long

    @Update
    suspend fun update(concept: ConceptEntity)

    @Query("UPDATE concepts SET active = 0, updatedAt = :updatedAt WHERE id = :conceptId")
    suspend fun archive(conceptId: Long, updatedAt: Long)

    @Query("UPDATE concepts SET favorite = :favorite, updatedAt = :updatedAt WHERE id = :conceptId")
    suspend fun setFavorite(conceptId: Long, favorite: Boolean, updatedAt: Long)

    @Query("SELECT * FROM concepts WHERE id = :id")
    suspend fun getById(id: Long): ConceptEntity?

    @Query("SELECT * FROM concepts WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): ConceptEntity?

    @Query("SELECT COUNT(*) FROM concepts WHERE uuid = :uuid")
    suspend fun countByUuid(uuid: String): Int

    @Query(
        """
        SELECT * FROM concepts
        WHERE active = 1
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPage(limit: Int, offset: Int): List<ConceptEntity>

    @Query("SELECT COUNT(*) FROM concepts WHERE active = 1")
    fun observeActiveCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM concepts
        WHERE active = 1 AND categoryId = :categoryId
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPageByCategory(categoryId: Long, limit: Int, offset: Int): List<ConceptEntity>

    @Query(
        """
        SELECT concepts.* FROM concepts
        INNER JOIN contents ON contents.conceptId = concepts.id AND contents.languageCode = :languageCode
        WHERE concepts.active = 1
        ORDER BY contents.text COLLATE NOCASE ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPageAlphabetical(languageCode: String, limit: Int, offset: Int): List<ConceptEntity>

    @Query(
        """
        SELECT concepts.* FROM concepts
        INNER JOIN contents ON contents.conceptId = concepts.id AND contents.languageCode = :languageCode
        WHERE concepts.active = 1 AND concepts.categoryId = :categoryId
        ORDER BY contents.text COLLATE NOCASE ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPageAlphabeticalInCategory(
        languageCode: String,
        categoryId: Long,
        limit: Int,
        offset: Int
    ): List<ConceptEntity>

    @Query(
        """
        SELECT DISTINCT concepts.* FROM concepts
        INNER JOIN contents ON contents.conceptId = concepts.id
        WHERE concepts.active = 1 AND (
            contents.text LIKE '%' || :query || '%' OR
            contents.definition LIKE '%' || :query || '%' OR
            contents.example LIKE '%' || :query || '%'
        )
        ORDER BY concepts.updatedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun search(query: String, limit: Int, offset: Int): List<ConceptEntity>

    @Query(
        """
        SELECT DISTINCT concepts.* FROM concepts
        INNER JOIN contents ON contents.conceptId = concepts.id
        WHERE concepts.active = 1 AND concepts.categoryId = :categoryId AND (
            contents.text LIKE '%' || :query || '%' OR
            contents.definition LIKE '%' || :query || '%' OR
            contents.example LIKE '%' || :query || '%'
        )
        ORDER BY concepts.updatedAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun searchInCategory(query: String, categoryId: Long, limit: Int, offset: Int): List<ConceptEntity>

    /**
     * پیدا کردن همه Concept هایی که در متن مبدأ (یک زبان مشخص) با حداقل یک Concept فعال
     * دیگر دقیقاً یکسان‌اند (بعد از Trim + lower-case). برای بخش «کلمات تکراری» در صفحه
     * واژگان (بند 64). نتیجه بر اساس متن نرمال‌شده مرتب می‌شود تا در لایه بالاتر راحت
     * گروه‌بندی شود، و در هر گروه قدیمی‌ترین (createdAt کمتر) اول می‌آید.
     */
    @Query(
        """
        SELECT concepts.* FROM concepts
        INNER JOIN contents ON contents.conceptId = concepts.id
        WHERE concepts.active = 1 AND contents.languageCode = :languageCode
          AND LOWER(TRIM(contents.text)) IN (
              SELECT LOWER(TRIM(c2.text)) FROM contents c2
              INNER JOIN concepts co2 ON co2.id = c2.conceptId
              WHERE co2.active = 1 AND c2.languageCode = :languageCode
              GROUP BY LOWER(TRIM(c2.text))
              HAVING COUNT(*) > 1
          )
        ORDER BY LOWER(TRIM(contents.text)) ASC, concepts.createdAt ASC
        """
    )
    suspend fun getDuplicateConceptsByText(languageCode: String): List<ConceptEntity>
}
