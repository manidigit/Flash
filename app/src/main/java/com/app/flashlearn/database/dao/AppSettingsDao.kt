package com.app.flashlearn.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.flashlearn.database.entity.AppSettingsEntity

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun get(key: String): AppSettingsEntity?
}
