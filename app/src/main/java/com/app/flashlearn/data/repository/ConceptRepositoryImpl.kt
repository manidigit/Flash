package com.app.flashlearn.data.repository

import androidx.room.withTransaction
import com.app.flashlearn.data.mapper.toDomain
import com.app.flashlearn.data.mapper.toEntity
import com.app.flashlearn.data.mapper.toDbString
import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.ContentDao
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.ConceptRepository
import javax.inject.Inject

// نسخه نهایی و رفع‌باگ‌شده (طبق سند ۰۹): بدون duplicate شدن contents، با TagDao.getByName واقعی
class ConceptRepositoryImpl @Inject constructor(
    private val database: FlashLearnDatabase,
    private val conceptDao: ConceptDao,
    private val contentDao: ContentDao,
    private val tagDao: TagDao,
    private val learningStateDao: LearningStateDao
) : ConceptRepository {

    override suspend fun addConcept(concept: Concept): Long = database.withTransaction {
        val id = conceptDao.insert(concept.toEntity())
        contentDao.insertAll(concept.contents.map { it.copy(conceptId = id).toEntity() })
        concept.tags.forEach { tag ->
            val existing = tagDao.getByName(tag.name)
            val tagId = existing?.id ?: tagDao.insert(tag.toEntity())
            tagDao.insertConceptTag(ConceptTagEntity(conceptId = id, tagId = tagId))
        }
        id
    }

    override suspend fun updateConcept(concept: Concept) = database.withTransaction {
        conceptDao.update(concept.toEntity().copy(updatedAt = System.currentTimeMillis()))
        contentDao.deleteByConceptId(concept.id)
        contentDao.insertAll(concept.contents.map { it.copy(conceptId = concept.id).toEntity() })
        Unit
    }

    override suspend fun getConceptById(id: Long): Concept? {
        val entity = conceptDao.getById(id) ?: return null
        return hydrate(entity)
    }

    override suspend fun getConceptsPaged(limit: Int, offset: Int): List<Concept> =
        conceptDao.getPaged(limit, offset).map { hydrate(it) }

    override suspend fun searchConcepts(query: String): List<Concept> =
        conceptDao.search(query).map { hydrate(it) }

    override suspend fun isDuplicate(uuid: String): Boolean = conceptDao.existsByUuid(uuid)

    override suspend fun deleteConcept(id: Long) {
        val existing = conceptDao.getById(id) ?: return
        conceptDao.update(existing.copy(active = false))
    }

    override suspend fun getConceptsByStage(stage: ReviewStage, limit: Int, offset: Int): List<Concept> =
        conceptDao.getPagedByStage(stage.toDbString(), limit, offset).map { hydrate(it) }

    override suspend fun getNewConcepts(limit: Int, offset: Int): List<Concept> =
        conceptDao.getNewConcepts(limit, offset).map { hydrate(it) }

    override suspend fun getLearningConcepts(limit: Int, offset: Int): List<Concept> =
        conceptDao.getLearningConcepts(limit, offset).map { hydrate(it) }

    private suspend fun hydrate(entity: ConceptEntity): Concept {
        val contents = contentDao.getByConceptId(entity.id).map { it.toDomain() }
        val tags = tagDao.getTagsForConcept(entity.id).map { it.toDomain() }
        val state = learningStateDao.getByConceptId(entity.id)?.toDomain()
        return entity.toDomain().copy(contents = contents, tags = tags, learningState = state)
    }
}
