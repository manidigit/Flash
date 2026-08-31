package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.app.flashlearn.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert
    suspend fun insert(settings: AppSettingsEntity)

    @Update
    suspend fun update(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 0")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 0")
    suspend fun getSettingsSync(): AppSettingsEntity?

    @Query("UPDATE app_settings SET streakDays = :days WHERE id = 0")
    suspend fun updateStreakDays(days: Int)

    @Query("UPDATE app_settings SET lastReviewDate = :date WHERE id = 0")
    suspend fun updateLastReviewDate(date: Long)

    @Query("UPDATE app_settings SET sourceLanguage = :source, targetLanguage = :target WHERE id = 0")
    suspend fun updateLanguagePair(source: String, target: String)
}
