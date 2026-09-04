package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.ConceptTagEntity

@Dao
interface ConceptTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(relation: ConceptTagEntity)
    @Delete
    suspend fun delete(relation: ConceptTagEntity)
    @Query("SELECT tagId FROM concept_tag WHERE conceptId = :conceptId")
    suspend fun findTagIdsForConcept(conceptId: Long): List<Long>
    @Query("SELECT conceptId FROM concept_tag WHERE tagId = :tagId")
    suspend fun findConceptIdsForTag(tagId: Long): List<Long>
}
