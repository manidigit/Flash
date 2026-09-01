package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguageEntity

@Dao
interface LanguageDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(language: LanguageEntity)

    @Query("SELECT * FROM language WHERE code IN ('fa', 'en', 'es')")
    suspend fun getSupported(): List<LanguageEntity>
}
