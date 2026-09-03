package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.repository.LanguagePairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * جفت زبان فعال روی همان رکورد تکی AppSettings ذخیره می‌شود (نه یک جدول جدا)؛
 * سازگار با HomeViewModel/SettingsViewModel که همان sourceLanguage/targetLanguage
 * را از AppSettings می‌خوانند و می‌نویسند.
 */
class LanguagePairRepositoryImpl @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) : LanguagePairRepository {

    override suspend fun setActiveLanguagePair(source: String, target: String) {
        appSettingsDao.updateLanguagePair(source, target)
    }

    override fun observeActivePair(): Flow<LanguagePair?> {
        return appSettingsDao.getSettings().map { entity ->
            entity?.let {
                LanguagePair(id = 0, sourceLanguage = it.sourceLanguage, targetLanguage = it.targetLanguage, isActive = true)
            }
        }
    }
}
