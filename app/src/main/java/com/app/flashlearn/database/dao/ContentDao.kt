package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ContentEntity

@Dao
interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: ContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contents: List<ContentEntity>)

    @Update
    suspend fun update(content: ContentEntity)

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId")
    suspend fun getForConcept(conceptId: Long): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId AND languageCode = :languageCode LIMIT 1")
    suspend fun getForConceptAndLanguage(conceptId: Long, languageCode: String): ContentEntity?

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteAllForConcept(conceptId: Long)
}
