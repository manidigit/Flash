package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.ContentDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.VocabularySortOrder
import com.app.flashlearn.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConceptRepositoryImpl @Inject constructor(
    private val conceptDao: ConceptDao,
    private val contentDao: ContentDao,
    private val tagDao: TagDao
) : ConceptRepository {

    override suspend fun insert(concept: Concept): Long {
        val conceptId = conceptDao.insert(concept.toEntity())
        contentDao.insertAll(concept.contents.map { it.toEntity(conceptId) })
        saveTags(conceptId, concept.tags)
        return conceptId
    }

    override suspend fun update(concept: Concept) {
        conceptDao.update(concept.toEntity())
        contentDao.deleteAllForConcept(concept.id)
        contentDao.insertAll(concept.contents.map { it.toEntity(concept.id) })
        saveTags(concept.id, concept.tags)
    }

    override suspend fun archive(conceptId: Long) {
        conceptDao.archive(conceptId, updatedAt = System.currentTimeMillis())
    }

    override suspend fun setFavorite(conceptId: Long, favorite: Boolean) {
        conceptDao.setFavorite(conceptId, favorite, updatedAt = System.currentTimeMillis())
    }

    override suspend fun getById(conceptId: Long): Concept? {
        val entity = conceptDao.getById(conceptId) ?: return null
        return buildDomain(entity.id, entity)
    }

    override suspend fun getByUuid(uuid: String): Concept? {
        val entity = conceptDao.getByUuid(uuid) ?: return null
        return buildDomain(entity.id, entity)
    }

    override suspend fun existsByUuid(uuid: String): Boolean =
        conceptDao.countByUuid(uuid) > 0

    override suspend fun existsByText(languageCode: String, text: String): Boolean =
        contentDao.countActiveByText(languageCode, text) > 0

    override suspend fun getPage(
        limit: Int,
        offset: Int,
        categoryId: Long?,
        sortOrder: VocabularySortOrder,
        sortLanguageCode: String?
    ): List<Concept> {
        val entities = if (sortOrder == VocabularySortOrder.ALPHABETICAL && sortLanguageCode != null) {
            if (categoryId != null) {
                conceptDao.getPageAlphabeticalInCategory(sortLanguageCode, categoryId, limit, offset)
            } else {
                conceptDao.getPageAlphabetical(sortLanguageCode, limit, offset)
            }
        } else if (categoryId != null) {
            conceptDao.getPageByCategory(categoryId, limit, offset)
        } else {
            conceptDao.getPage(limit, offset)
        }
        return entities.map { buildDomain(it.id, it) }
    }

    override suspend fun search(query: String, limit: Int, offset: Int, categoryId: Long?): List<Concept> {
        val entities = if (categoryId != null) {
            conceptDao.searchInCategory(query, categoryId, limit, offset)
        } else {
            conceptDao.search(query, limit, offset)
        }
        return entities.map { buildDomain(it.id, it) }
    }

    override fun observeActiveCount(): Flow<Int> = conceptDao.observeActiveCount()

    private suspend fun buildDomain(
        conceptId: Long,
        entity: com.app.flashlearn.database.entity.ConceptEntity
    ): Concept {
        val contents = contentDao.getForConcept(conceptId)
        val tags = tagDao.getTagsForConcept(conceptId).map { it.name }
        return entity.toDomain(contents, tags)
    }

    private suspend fun saveTags(conceptId: Long, tagNames: List<String>) {
        for (name in tagNames) {
            val existing = tagDao.findByName(name)
            val tagId = existing?.id ?: tagDao.insertTag(TagEntity(name = name))
            tagDao.insertConceptTag(ConceptTagEntity(conceptId = conceptId, tagId = tagId))
        }
    }
}
