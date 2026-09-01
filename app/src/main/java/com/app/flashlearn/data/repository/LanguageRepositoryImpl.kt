package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.entity.LanguageEntity
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val languageDao: LanguageDao
) : LanguageRepository {

    override suspend fun insertLanguage(language: Language) {
        languageDao.insert(language.toEntity())
    }

    override fun getSupportedLanguages(): Flow<List<Language>> = flow {
        val languages = languageDao.getSupported()
        emit(languages.map { it.toDomain() })
    }

    private fun Language.toEntity() = LanguageEntity(
        code = code,
        displayName = displayName,
        flagEmoji = flagEmoji
    )

    private fun LanguageEntity.toDomain() = Language(
        code = code,
        displayName = displayName,
        flagEmoji = flagEmoji
    )
}
