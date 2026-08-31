package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.LanguagePairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguagePairDao {
    @Insert
    suspend fun insert(languagePair: LanguagePairEntity)

    @Update
    suspend fun update(languagePair: LanguagePairEntity)

    @Query("SELECT * FROM language_pair WHERE isActive = 1 LIMIT 1")
    fun getActivePair(): Flow<LanguagePairEntity?>

    @Query("SELECT * FROM language_pair WHERE sourceLanguage = :source AND targetLanguage = :target LIMIT 1")
    suspend fun getPair(source: String, target: String): LanguagePairEntity?

    @Query("UPDATE language_pair SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE language_pair SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)
}
