package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Language
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    suspend fun insertAll(languages: List<Language>)
    suspend fun getAll(): List<Language>
    fun observeActive(): Flow<List<Language>>
}
