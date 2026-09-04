package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.ContentEntity

@Dao
interface ContentDao {
    @Insert
    suspend fun insertAll(contents: List<ContentEntity>)

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId")
    suspend fun getByConceptId(conceptId: Long): List<ContentEntity>

    @Query("SELECT * FROM contents WHERE conceptId = :conceptId AND languageCode = :lang")
    suspend fun getByConceptAndLanguage(conceptId: Long, lang: String): ContentEntity?

    @Query("DELETE FROM contents WHERE conceptId = :conceptId")
    suspend fun deleteByConceptId(conceptId: Long)
}
