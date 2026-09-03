package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguageEntity

@Dao
interface LanguageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(language: LanguageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(languages: List<LanguageEntity>)

    @Query("SELECT * FROM languages")
    suspend fun getAll(): List<LanguageEntity>

    @Query("SELECT * FROM languages WHERE code IN ('fa', 'en', 'es')")
    suspend fun getSupported(): List<LanguageEntity>

    @Query("SELECT COUNT(*) FROM languages")
    suspend fun count(): Int
}
