package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.CategoryDao
import com.app.flashlearn.database.entity.CategoryEntity
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { Category(it.id, it.name, it.isCustom) } }

    override suspend fun getOrCreate(name: String): Long {
        val existing = dao.findByName(name)
        return existing?.id ?: dao.insert(CategoryEntity(name = name, isCustom = true))
    }
}
