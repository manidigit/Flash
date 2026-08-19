package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.LanguagePairDao
import com.app.flashlearn.database.entity.LanguagePairEntity
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.repository.LanguagePairRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LanguagePairRepositoryImpl @Inject constructor(
    private val dao: LanguagePairDao
) : LanguagePairRepository {

    override suspend fun setActivePair(sourceLanguage: String, targetLanguage: String) {
        val existing = dao.find(sourceLanguage, targetLanguage)
        val id = existing?.id ?: dao.insert(
            LanguagePairEntity(sourceLanguage = sourceLanguage, targetLanguage = targetLanguage)
        )
        dao.deactivateAll()
        dao.activate(id)
    }

    override fun observeActivePair(): Flow<LanguagePair?> =
        dao.observeActivePair().map { it?.toDomain() }

    override fun observeAllPairs(): Flow<List<LanguagePair>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    private fun LanguagePairEntity.toDomain() = LanguagePair(id, sourceLanguage, targetLanguage, isActive)
}
