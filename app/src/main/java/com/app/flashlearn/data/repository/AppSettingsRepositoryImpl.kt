package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.domain.model.AppSettings
import com.app.flashlearn.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) : AppSettingsRepository {

    override fun getSettings(): Flow<AppSettings> {
        return appSettingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: AppSettings()
        }
    }

    override suspend fun getSettingsSync(): AppSettings {
        return appSettingsDao.getSettingsSync()?.toDomain() ?: AppSettings()
    }

    override suspend fun updateStreakDays(days: Int) {
        appSettingsDao.updateStreakDays(days)
    }

    override suspend fun updateLastReviewDate(date: Long) {
        appSettingsDao.updateLastReviewDate(date)
    }

    override suspend fun swapLanguagePair() {
        val current = getSettingsSync()
        appSettingsDao.updateLanguagePair(current.targetLanguage, current.sourceLanguage)
    }

    override suspend fun updateLanguagePair(source: String, target: String) {
        appSettingsDao.updateLanguagePair(source, target)
    }

    private fun AppSettingsEntity.toDomain() = AppSettings(
        streakDays = streakDays,
        lastReviewDate = lastReviewDate,
        appTheme = appTheme,
        appLanguage = appLanguage,
        sourceLanguage = sourceLanguage,
        targetLanguage = targetLanguage
    )
}
