package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConceptTag(conceptTag: ConceptTagEntity)

    @Query("DELETE FROM concept_tags WHERE conceptId = :conceptId AND tagId = :tagId")
    suspend fun removeConceptTag(conceptId: Long, tagId: Long)

    @Query("SELECT * FROM tags ORDER BY name")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN concept_tags ON tags.id = concept_tags.tagId
        WHERE concept_tags.conceptId = :conceptId
        """
    )
    fun observeTagsForConcept(conceptId: Long): Flow<List<TagEntity>>

    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN concept_tags ON tags.id = concept_tags.tagId
        WHERE concept_tags.conceptId = :conceptId
        """
    )
    suspend fun getTagsForConcept(conceptId: Long): List<TagEntity>
}
