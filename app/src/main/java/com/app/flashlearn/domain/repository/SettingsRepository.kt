package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun getSettingsSync(): AppSettings
    suspend fun updateStreakDays(days: Int)
    suspend fun swapLanguagePair()
}
