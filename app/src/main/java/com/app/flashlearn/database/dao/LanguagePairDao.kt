package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguagePairEntity

@Dao
interface LanguagePairDao {
    @Insert
    suspend fun insert(pair: LanguagePairEntity): Long

    @Query("UPDATE language_pairs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE language_pairs SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)

    @Query("SELECT * FROM language_pairs WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): LanguagePairEntity?
}
