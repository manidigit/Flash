package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ConceptEntity

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

    @Query("""
        SELECT * FROM concepts
        WHERE active = 1
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPaged(limit: Int, offset: Int): List<ConceptEntity>

    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        JOIN contents ct ON ct.conceptId = c.id
        WHERE c.active = 1 AND ct.text LIKE ('%' || :query || '%')
    """)
    suspend fun search(query: String): List<ConceptEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM concepts WHERE uuid = :uuid)")
    suspend fun existsByUuid(uuid: String): Boolean

    @Query("""
        SELECT c.* FROM concepts c
        JOIN learning_states ls ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.stage = :stage
        ORDER BY c.updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPagedByStage(stag
cat > ~/Flash/app/src/main/java/com/app/flashlearn/database/dao/ConceptDao.kt << 'KOTLINEOF'
package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ConceptEntity

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

    @Query("""
        SELECT * FROM concepts
        WHERE active = 1
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPaged(limit: Int, offset: Int): List<ConceptEntity>

    @Query("""
        SELECT DISTINCT c.* FROM concepts c
        JOIN contents ct ON ct.conceptId = c.id
        WHERE c.active = 1 AND ct.text LIKE ('%' || :query || '%')
    """)
    suspend fun search(query: String): List<ConceptEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM concepts WHERE uuid = :uuid)")
    suspend fun existsByUuid(uuid: String): Boolean

    @Query("""
        SELECT c.* FROM concepts c
        JOIN learning_states ls ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.stage = :stage
        ORDER BY c.updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getPagedByStage(stage: String, limit: Int, offset: Int): List<ConceptEntity>

    @Query("""
        SELECT c.* FROM concepts c
        JOIN learning_states ls ON ls.conceptId = c.id
        WHERE c.active = 1 AND ls.stage = 'DAILY' AND ls.totalCorrect = 0 AND ls.totalWrong = 0
        ORDER BY c.createdAt DESC LIMIT :limit OFFSET :offset
    """)
    suspend fun getNewConcepts(limit: Int, offset: Int): List<ConceptEntity>

    @Query("""
        SELECT c.* FROM concepts c
        JOIN learning_states ls ON ls.conceptId = c.id
        WHERE c.active = 1 AND (
            ls.stage IN ('WEEKLY', 'MONTHLY')
            OR (ls.stage = 'DAILY' AND (ls.totalCorrect > 0 OR ls.totalWrong > 0))
        )
        ORDER BY c.updatedAt DESC LIMIT :limit OFFSET :offset
    """)
    suspend fun getLearningConcepts(limit: Int, offset: Int): List<ConceptEntity>
}
