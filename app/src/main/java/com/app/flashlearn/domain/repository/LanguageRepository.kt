package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Language
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    suspend fun insertLanguage(language: Language)
    fun getSupportedLanguages(): Flow<List<Language>>
}
