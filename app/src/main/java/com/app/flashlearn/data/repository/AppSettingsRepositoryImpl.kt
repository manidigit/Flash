package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.domain.model.AppSettings
import com.app.flashlearn.domain.repository.AppSettingsRepository
import javax.inject.Inject

// نکته: بند ۳ سند اول AppSettings را Key-Value ساده فرض کرده بود؛
// این پیاده‌سازی چند کلید پرکاربرد را به یک AppSettings تجمیع می‌کند.
class AppSettingsRepositoryImpl @Inject constructor(
    private val dao: AppSettingsDao
) : AppSettingsRepository {
    override suspend fun getSettings(): AppSettings = AppSettings(
        theme = dao.get("theme")?.value ?: "system",
        activeLanguagePairId = dao.get("active_language_pair_id")?.value?.toLongOrNull(),
        aiProvider = dao.get("ai_provider")?.value,
        reviewSettingsJson = dao.get("review_settings_json")?.value
    )

    override suspend fun updateSettings(settings: AppSettings) {
        dao.set(AppSettingsEntity("theme", settings.theme))
        settings.activeLanguagePairId?.let { dao.set(AppSettingsEntity("active_language_pair_id", it.toString())) }
        settings.aiProvider?.let { dao.set(AppSettingsEntity("ai_provider", it)) }
        settings.reviewSettingsJson?.let { dao.set(AppSettingsEntity("review_settings_json", it)) }
    }
}
