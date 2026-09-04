package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.ContentEntity

@Dao
interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(content: ContentEntity): Long

    @Update
    suspend fun update(content: ContentEntity)

    @Delete
    suspend fun delete(content: ContentEntity)

    @Query("SELECT * FROM content WHERE conceptId = :conceptId ORDER BY languageCode")
    suspend fun findByConcept(conceptId: Long): List<ContentEntity>

    @Query("SELECT * FROM content WHERE conceptId = :conceptId AND languageCode = :languageCode LIMIT 1")
    suspend fun findByConceptAndLanguage(conceptId: Long, languageCode: String): ContentEntity?

    @Query("SELECT * FROM content WHERE languageCode = :languageCode AND text LIKE '%' || :query || '%'")
    suspend fun searchByText(languageCode: String, query: String): List<ContentEntity>
}
