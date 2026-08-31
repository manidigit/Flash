package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {
    @Insert
    suspend fun insert(language: LanguageEntity)

    @Query("SELECT * FROM language WHERE code = :code")
    suspend fun getByCode(code: String): LanguageEntity?

    @Query("SELECT * FROM language ORDER BY displayName ASC")
    fun getAll(): Flow<List<LanguageEntity>>

    /** Initial product languages. The schema remains extensible for future languages. */
    @Query("SELECT * FROM language WHERE code IN ('fa', 'en', 'es') ORDER BY displayName ASC")
    fun getSupported(): Flow<List<LanguageEntity>>
}
