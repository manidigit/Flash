package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.AppSettings

interface AppSettingsRepository {
    suspend fun getSettings(): AppSettings
    suspend fun updateSettings(settings: AppSettings)
}
