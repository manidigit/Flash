package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.entity.LanguageEntity
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val dao: LanguageDao
) : LanguageRepository {

    override suspend fun insertAll(languages: List<Language>) {
        dao.insertAll(languages.map { LanguageEntity(code = it.code, displayName = it.displayName) })
    }

    override suspend fun getAll(): List<Language> =
        dao.getAll().map { Language(it.code, it.displayName) }

    override fun observeActive(): Flow<List<Language>> =
        dao.observeActiveLanguages().map { list -> list.map { Language(it.code, it.displayName) } }
}
