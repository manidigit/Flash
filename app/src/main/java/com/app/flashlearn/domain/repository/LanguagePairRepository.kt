package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.LanguagePair

interface LanguagePairRepository {
    suspend fun getActivePair(): LanguagePair?
    suspend fun setActivePair(id: Long)
    suspend fun addPair(pair: LanguagePair): Long
}
