package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.ContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Insert
    suspend fun insert(content: ContentEntity): Long

    @Update
    suspend fun update(content: ContentEntity)

    @Delete
    suspend fun delete(content: ContentEntity)

    @Query("SELECT * FROM content WHERE conceptId = :conceptId AND languageCode = :lang LIMIT 1")
    suspend fun getByConceptAndLanguage(conceptId: Long, lang: String): ContentEntity?

    @Query("SELECT * FROM content WHERE conceptId = :conceptId")
    suspend fun getByConceptId(conceptId: Long): List<ContentEntity>
}
