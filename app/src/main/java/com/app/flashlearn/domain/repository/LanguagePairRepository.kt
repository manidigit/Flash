package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.LanguagePair
import kotlinx.coroutines.flow.Flow

interface LanguagePairRepository {
    suspend fun setActiveLanguagePair(source: String, target: String)
    fun observeActivePair(): Flow<LanguagePair?>
}
