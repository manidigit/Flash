package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dao: AppSettingsDao
) : SettingsRepository {

    override suspend fun setValue(key: String, value: String) {
        dao.set(AppSettingsEntity(key = key, value = value))
    }

    override suspend fun getValue(key: String): String? =
        dao.get(key)?.value

    override fun observeValue(key: String): Flow<String?> =
        dao.observe(key).map { it?.value }
}
