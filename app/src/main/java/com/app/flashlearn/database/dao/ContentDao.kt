package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ContentEntity

@Dao
interface ContentDao {
    @Insert
    suspend fun insert(content: ContentEntity): Long

    @Insert
    suspend fun insertAll(contents: List<ContentEntity>)

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId")
    suspend fun getForConcept(conceptId: Long): List<ContentEntity>

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteAllForConcept(conceptId: Long)

    @Query("SELECT COUNT(*) FROM contents WHERE conceptId = :conceptId AND languageCode = :languageCode AND text = :text")
    suspend fun countMatching(conceptId: Long, languageCode: String, text: String): Int

    @Query(
        """
        SELECT text FROM contents
        WHERE languageCode = :languageCode AND conceptId != :excludeConceptId
        ORDER BY RANDOM() LIMIT :limit
        """
    )
    suspend fun getRandomTexts(languageCode: String, excludeConceptId: Long, limit: Int): List<String>
}
