package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.LanguagePairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguagePairDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pair: LanguagePairEntity): Long

    @Query("UPDATE language_pairs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE language_pairs SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)

    @Query("SELECT * FROM language_pairs WHERE isActive = 1 LIMIT 1")
    fun observeActivePair(): Flow<LanguagePairEntity?>

    @Query(
        "SELECT * FROM language_pairs WHERE sourceLanguage = :source AND targetLanguage = :target LIMIT 1"
    )
    suspend fun find(source: String, target: String): LanguagePairEntity?

    @Query("SELECT * FROM language_pairs")
    fun observeAll(): Flow<List<LanguagePairEntity>>
}
