package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.CategoryDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.TagRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {
    override suspend fun getAllCategories() = dao.getAll().map { it.toDomain() }
    override suspend fun addCategory(category: Category) = dao.insert(category.toEntity())
}

class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao
) : TagRepository {
    override suspend fun getTagsForConcept(conceptId: Long) = dao.getTagsForConcept(conceptId).map { it.toDomain() }
    override suspend fun addTagToConcept(conceptId: Long, tagName: String) {
        val existing = dao.getByName(tagName)
        val tagId = existing?.id ?: dao.insert(TagEntity(name = tagName))
        dao.insertConceptTag(ConceptTagEntity(conceptId, tagId))
    }
}
