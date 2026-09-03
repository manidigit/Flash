package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.VocabularySortOrder
import kotlinx.coroutines.flow.Flow

interface ConceptRepository {
    suspend fun insert(concept: Concept): Long
    suspend fun update(concept: Concept)
    suspend fun getById(id: Long): Concept?
    fun getAllActiveConcepts(): Flow<List<Concept>>
    fun observeActiveCount(): Flow<Int>

    suspend fun getPage(
        limit: Int,
        offset: Int,
        categoryId: Long?,
        sortOrder: VocabularySortOrder,
        sortLanguageCode: String
    ): List<Concept>

    suspend fun search(query: String, limit: Int, offset: Int, categoryId: Long?): List<Concept>

    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** حذف نرم (Archive)؛ active=false، تاریخچه مرور همیشه محفوظ می‌ماند. */
    suspend fun archive(id: Long)

    suspend fun findActiveConceptIdByText(languageCode: String, text: String): Long?
    suspend fun hasTranslation(conceptId: Long, languageCode: String, text: String): Boolean
    suspend fun addTranslation(conceptId: Long, content: ContentItem)

    /** گروه‌های کلمات تکراری بر اساس متن مبدا در یک زبان مشخص. */
    suspend fun findDuplicateGroups(sourceLanguage: String): List<List<Concept>>

    /** برای ساخت گزینه‌های غلط در تست چهارگزینه‌ای. */
    suspend fun getRandomTranslations(languageCode: String, excludeConceptId: Long, limit: Int): List<String>
}
