package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
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

    /**
     * پیدا کردن Concept فعالی که همین متن مبدأ را دارد، برای ادغام معنی/ترجمه جدید در آن
     * به‌جای ساخت یک Concept کاملاً جدید و تکراری (بند 64).
     */
    suspend fun findActiveConceptIdByText(languageCode: String, text: String): Long?

    /**
     * افزودن یک معنی/ترجمه جدید به یک Concept موجود، بدون دست‌زدن به بقیه محتوای آن
     * (بند 64: یک کلمه می‌تواند چند معنی داشته باشد).
     */
    suspend fun addTranslation(conceptId: Long, content: ContentItem)

    /**
     * آیا Concept مشخص‌شده از قبل دقیقاً همین معنی/ترجمه را در این زبان دارد؟ (بند 64)
     */
    suspend fun hasTranslation(conceptId: Long, languageCode: String, text: String): Boolean

    /**
     * چند متن تصادفی (متعلق به Concept های دیگر) در همین زبان، برای ساخت گزینه‌های غلط
     * تست چهارگزینه‌ای مرور (ویژگی جدید).
     */
    suspend fun getRandomTranslations(languageCode: String, excludeConceptId: Long, limit: Int): List<String>
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
