package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.LanguagePair
import kotlinx.coroutines.flow.Flow

interface LanguagePairRepository {
    /** جفت‌زبان را پیدا یا در صورت نبود می‌سازد، و آن را به‌عنوان جهت فعال تنظیم می‌کند (بند 71). */
    suspend fun setActivePair(sourceLanguage: String, targetLanguage: String)
    fun observeActivePair(): Flow<LanguagePair?>
    fun observeAllPairs(): Flow<List<LanguagePair>>
}
