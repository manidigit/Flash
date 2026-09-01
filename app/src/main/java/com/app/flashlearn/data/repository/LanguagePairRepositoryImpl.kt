package com.app.flashlearn.data.repository

import com.app.flashlearn.domain.repository.LanguagePairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LanguagePairRepositoryImpl @Inject constructor() : LanguagePairRepository {

    override suspend fun setActiveLanguagePair(source: String, target: String) {
        // Implementation will be added later with actual database
    }

    override fun getActiveLanguagePair(): Flow<Pair<String, String>?> = flow {
        emit(Pair("fa", "en"))
    }
}
