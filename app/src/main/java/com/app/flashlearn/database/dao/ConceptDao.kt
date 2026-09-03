package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ConceptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Insert
    suspend fun insert(concept: ConceptEntity): Long

    @Update
    suspend fun update(concept: ConceptEntity)

    @Query("SELECT * FROM concepts WHERE id = :id")
    suspend fun getById(id: Long): ConceptEntity?

    @Query("SELECT * FROM concepts WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): ConceptEntity?

    @Query("SELECT * FROM concepts WHERE active = 1 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<ConceptEntity>>

    @Query("SELECT COUNT(*) FROM concepts WHERE active = 1")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT * FROM concepts ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<ConceptEntity>

    @Query(
        """
        SELECT c.* FROM concepts c
        WHERE c.active = 1 AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        ORDER BY c.createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPageRecent(limit: Int, offset: Int, categoryId: Long?): List<ConceptEntity>

    @Query(
        """
        SELECT c.* FROM concepts c
        INNER JOIN contents ct ON ct.conceptId = c.id AND ct.languageCode = :languageCode
        WHERE c.active = 1 AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        GROUP BY c.id
        ORDER BY MIN(ct.text) COLLATE NOCASE ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPageAlphabetical(limit: Int, offset: Int, categoryId: Long?, languageCode: String): List<ConceptEntity>

    @Query(
        """
        SELECT DISTINCT c.* FROM concepts c
        INNER JOIN contents ct ON ct.conceptId = c.id
        WHERE c.active = 1
        AND (:categoryId IS NULL OR c.categoryId = :categoryId)
        AND (ct.text LIKE '%' || :query || '%' OR c.notes LIKE '%' || :query || '%')
        ORDER BY c.createdAt DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun search(query: String, limit: Int, offset: Int, categoryId: Long?): List<ConceptEntity>

    @Query("UPDATE concepts SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE concepts SET active = 0, updatedAt = :now WHERE id = :id")
    suspend fun archive(id: Long, now: Long)

    @Query(
        """
        SELECT c.id FROM concepts c
        INNER JOIN contents ct ON ct.conceptId = c.id
        WHERE c.active = 1 AND ct.languageCode = :languageCode AND ct.text = :text
        LIMIT 1
        """
    )
    suspend fun findActiveConceptIdByText(languageCode: String, text: String): Long?

    @Query(
        """
        SELECT c.id AS id, ct.text AS text FROM concepts c
        INNER JOIN contents ct ON ct.conceptId = c.id
        WHERE c.active = 1 AND ct.languageCode = :sourceLanguage
        """
    )
    suspend fun getIdsAndTextForLanguage(sourceLanguage: String): List<ConceptIdText>
}

data class ConceptIdText(val id: Long, val text: String)
