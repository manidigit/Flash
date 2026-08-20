package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.VocabularySortOrder
import kotlinx.coroutines.flow.Flow

interface ConceptRepository {
    suspend fun insert(concept: Concept): Long
    suspend fun update(concept: Concept)
    suspend fun archive(conceptId: Long)
    suspend fun setFavorite(conceptId: Long, favorite: Boolean)
    suspend fun getById(conceptId: Long): Concept?
    suspend fun getByUuid(uuid: String): Concept?
    suspend fun existsByUuid(uuid: String): Boolean

    /**
     * آیا Concept فعالی با این متن (نرمال‌شده) در این زبان از قبل وجود دارد؟
     * برای جلوگیری از درج تکراری هنگام Manual/Paste/Import (بند 64).
     */
    suspend fun existsByText(languageCode: String, text: String): Boolean
    suspend fun getPage(
        limit: Int,
        offset: Int,
        categoryId: Long? = null,
        sortOrder: VocabularySortOrder = VocabularySortOrder.RECENT,
        sortLanguageCode: String? = null
    ): List<Concept>
    suspend fun search(query: String, limit: Int, offset: Int, categoryId: Long? = null): List<Concept>
    fun observeActiveCount(): Flow<Int>
}
