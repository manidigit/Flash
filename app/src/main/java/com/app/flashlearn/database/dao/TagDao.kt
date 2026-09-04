package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Insert
    suspend fun insertConceptTag(crossRef: ConceptTagEntity)

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Query("""
        SELECT t.* FROM tags t
        JOIN concept_tags ct ON ct.tagId = t.id
        WHERE ct.conceptId = :conceptId
    """)
    suspend fun getTagsForConcept(conceptId: Long): List<TagEntity>
}
