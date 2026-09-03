package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert
    suspend fun insertTag(tag: TagEntity): Long

    @Insert
    suspend fun insertConceptTag(link: ConceptTagEntity)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN concept_tags ct ON ct.tagId = t.id
        WHERE ct.conceptId = :conceptId
        """
    )
    suspend fun getTagsForConcept(conceptId: Long): List<TagEntity>

    @Query("DELETE FROM concept_tags WHERE conceptId = :conceptId")
    suspend fun deleteAllLinksForConcept(conceptId: Long)
}
