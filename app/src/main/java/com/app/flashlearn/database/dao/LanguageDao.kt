package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(language: LanguageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(languages: List<LanguageEntity>)

    @Query("SELECT * FROM languages WHERE isActive = 1 ORDER BY displayName")
    fun observeActiveLanguages(): Flow<List<LanguageEntity>>

    @Query("SELECT * FROM languages")
    suspend fun getAll(): List<LanguageEntity>

    @Query("SELECT * FROM languages WHERE code = :code")
    suspend fun getByCode(code: String): LanguageEntity?
}
