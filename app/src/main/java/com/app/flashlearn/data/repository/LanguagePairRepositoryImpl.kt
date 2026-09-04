package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.LanguagePairDao
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.repository.LanguagePairRepository
import javax.inject.Inject

class LanguagePairRepositoryImpl @Inject constructor(
    private val dao: LanguagePairDao
) : LanguagePairRepository {
    override suspend fun getActivePair() = dao.getActive()?.toDomain()
    override suspend fun setActivePair(id: Long) {
        dao.deactivateAll()
        dao.activate(id)
    }
    override suspend fun addPair(pair: LanguagePair) = dao.insert(pair.toEntity())
}
