package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ConceptTagEntity

@Dao
interface ConceptTagDao {
    @Insert
    suspend fun insert(conceptTag: ConceptTagEntity)

    @Delete
    suspend fun delete(conceptTag: ConceptTagEntity)

    @Query("SELECT tagId FROM concept_tag WHERE conceptId = :conceptId")
    suspend fun getTagsByConceptId(conceptId: Long): List<Long>

    @Query("SELECT conceptId FROM concept_tag WHERE tagId = :tagId")
    suspend fun getConceptsByTagId(tagId: Long): List<Long>

    @Query("DELETE FROM concept_tag WHERE conceptId = :conceptId")
    suspend fun deleteByConceptId(conceptId: Long)
}
