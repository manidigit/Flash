package com.app.flashlearn.database

import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.entity.LanguageEntity
import javax.inject.Inject

/**
 * Initial product seed: exactly three languages.
 * The schema itself remains extensible; this is only the initial dataset.
 */
class InitialLanguageSeeder @Inject constructor(
    private val languageDao: LanguageDao
) {
    suspend fun seed() {
        listOf(
            LanguageEntity(code = "fa", displayName = "فارسی", flagEmoji = "🇮🇷"),
            LanguageEntity(code = "en", displayName = "English", flagEmoji = "🇬🇧"),
            LanguageEntity(code = "es", displayName = "Español", flagEmoji = "🇪🇸")
        ).forEach { languageDao.insert(it) }
    }
}
