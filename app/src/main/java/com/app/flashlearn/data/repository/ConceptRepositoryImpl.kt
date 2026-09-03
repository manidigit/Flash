package com.app.flashlearn.data.repository

import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.ContentDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.TagEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.VocabularySortOrder
import com.app.flashlearn.domain.repository.ConceptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConceptRepositoryImpl @Inject constructor(
    private val conceptDao: ConceptDao,
    private val contentDao: ContentDao,
    private val tagDao: TagDao
) : ConceptRepository {

    private suspend fun hydrate(entity: ConceptEntity): Concept {
        val contents = contentDao.getForConcept(entity.id)
        val tags = tagDao.getTagsForConcept(entity.id).map { it.name }
        return entity.toDomain(contents, tags)
    }

    private suspend fun syncTags(conceptId: Long, tags: List<String>) {
        tagDao.deleteAllLinksForConcept(conceptId)
        for (name in tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct()) {
            val tagId = tagDao.findByName(name)?.id ?: tagDao.insertTag(TagEntity(name = name))
            tagDao.insertConceptTag(ConceptTagEntity(conceptId = conceptId, tagId = tagId))
        }
    }

    override suspend fun insert(concept: Concept): Long {
        val conceptId = conceptDao.insert(concept.toEntity())
        contentDao.insertAll(concept.contents.map { it.toEntity(conceptId) })
        syncTags(conceptId, concept.tags)
        return conceptId
    }

    override suspend fun update(concept: Concept) {
        conceptDao.update(concept.toEntity())
        contentDao.deleteAllForConcept(concept.id)
        contentDao.insertAll(concept.contents.map { it.toEntity(concept.id) })
        syncTags(concept.id, concept.tags)
    }

    override suspend fun getById(id: Long): Concept? {
        val entity = conceptDao.getById(id) ?: return null
        return hydrate(entity)
    }

    override fun getAllActiveConcepts(): Flow<List<Concept>> {
        return conceptDao.getAllActive().map { entities -> entities.map { hydrate(it) } }
    }

    override fun observeActiveCount(): Flow<Int> = conceptDao.getActiveCount()

    override suspend fun getPage(
        limit: Int,
        offset: Int,
        categoryId: Long?,
        sortOrder: VocabularySortOrder,
        sortLanguageCode: String
    ): List<Concept> {
        val entities = if (sortOrder == VocabularySortOrder.ALPHABETICAL) {
            conceptDao.getPageAlphabetical(limit, offset, categoryId, sortLanguageCode)
        } else {
            conceptDao.getPageRecent(limit, offset, categoryId)
        }
        return entities.map { hydrate(it) }
    }

    override suspend fun search(query: String, limit: Int, offset: Int, categoryId: Long?): List<Concept> {
        return conceptDao.search(query, limit, offset, categoryId).map { hydrate(it) }
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        conceptDao.setFavorite(id, favorite)
    }

    override suspend fun archive(id: Long) {
        conceptDao.archive(id, System.currentTimeMillis())
    }

    override suspend fun findActiveConceptIdByText(languageCode: String, text: String): Long? {
        return conceptDao.findActiveConceptIdByText(languageCode, text)
    }

    override suspend fun hasTranslation(conceptId: Long, languageCode: String, text: String): Boolean {
        return contentDao.countMatching(conceptId, languageCode, text) > 0
    }

    override suspend fun addTranslation(conceptId: Long, content: ContentItem) {
        contentDao.insert(content.toEntity(conceptId))
    }

    override suspend fun findDuplicateGroups(sourceLanguage: String): List<List<Concept>> {
        val rows = conceptDao.getIdsAndTextForLanguage(sourceLanguage)
        val grouped = rows.groupBy { it.text.trim().lowercase() }.values.filter { it.size > 1 }
        return grouped.map { rowsInGroup ->
            rowsInGroup.mapNotNull { row -> conceptDao.getById(row.id)?.let { hydrate(it) } }
        }
    }

    override suspend fun getRandomTranslations(languageCode: String, excludeConceptId: Long, limit: Int): List<String> {
        return contentDao.getRandomTexts(languageCode, excludeConceptId, limit)
    }
}
