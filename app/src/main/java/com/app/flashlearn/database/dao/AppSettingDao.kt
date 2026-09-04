package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_setting WHERE key = :key LIMIT 1")
    suspend fun get(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: AppSettingEntity)

    @Delete
    suspend fun delete(setting: AppSettingEntity)

    @Query("SELECT * FROM app_setting WHERE key = :key LIMIT 1")
    fun observe(key: String): Flow<AppSettingEntity?>
}
