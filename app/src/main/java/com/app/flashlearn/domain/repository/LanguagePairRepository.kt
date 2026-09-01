package com.app.flashlearn.domain.repository

import kotlinx.coroutines.flow.Flow

interface LanguagePairRepository {
    suspend fun setActiveLanguagePair(source: String, target: String)
    fun getActiveLanguagePair(): Flow<Pair<String, String>?>
}
