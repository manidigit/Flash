package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.core.util.ParenthesesUtils
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import java.util.UUID
import javax.inject.Inject

/**
 * نتیجه واقعی Import:
 * - insertedCount: چند Concept کاملاً جدید ساخته شد
 * - translationsAddedCount: چند معنی/ترجمه جدید به یک کلمه از قبل موجود اضافه شد (بند 64)
 * - duplicateCount: چند مورد کاملاً تکراری (همان کلمه + همان معنی) رد شد
 */
data class ImportOutcome(
    val insertedCount: Int,
    val translationsAddedCount: Int,
    val duplicateCount: Int
)

/**
 * ذخیره‌سازی مشترک برای هر جریانی که یک لیست ParsedVocabularyEntry تولید می‌کند
 * (Paste Text و Import File — بند 42 و 43).
 *
 * بند 64 (Edge Case «Import ناقص»): هر ردیف کاملاً مستقل از بقیه است؛ خطای یک ردیف بقیه
 * را متوقف نمی‌کند و کل عملیات Crash نمی‌کند.
 *
 * بند 64 (رفع باگ «کلمه با چند معنی»): یک کلمه می‌تواند چند معنی/ترجمه متفاوت داشته باشد
 * (مثلاً «banco» هم «نیمکت» هم «بانک»). وقتی متن مبدأ یک ردیف با یک Concept فعال موجود
 * یکی باشد:
 *   - اگر همان معنی (متن مقصد) از قبل روی همان Concept ثبت شده → تکراری واقعی، Skip.
 *   - اگر معنی متفاوتی باشد → به‌عنوان یک معنی جدید به همان Concept اضافه می‌شود، نه
 *     یک Concept جدید و جدا.
 * چون هر ردیف قبل از رفتن به ردیف بعدی کامل commit می‌شود، خود دیتابیس مرجع تشخیص
 * تکراری/چندمعنایی درون همین دسته Import هم هست؛ نیازی به نگه‌داشتن جدا یک Set در حافظه نیست.
 */
class ImportParsedEntriesUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(
        entries: List<ParsedVocabularyEntry>,
        sourceLanguage: String,
        targetLanguage: String,
        // درخواست کاربر: امکان تخصیص یک دسته‌بندی به همه رکوردهای این دسته Import (بند 64).
        // فقط روی Concept های کاملاً جدید اعمال می‌شود؛ اگر ردیفی به یک کلمه از قبل موجود
        // ادغام شود، دسته‌بندی آن کلمه (که ممکن است قبلاً دستی تنظیم شده) دست‌نخورده می‌ماند.
        categoryId: Long? = null
    ): ImportOutcome {
        val now = DateTimeUtils.now()
        var insertedCount = 0
        var translationsAddedCount = 0
        var duplicateCount = 0

        for (entry in entries) {
            if (entry.sourceText.isBlank() || entry.targetText.isBlank()) continue

            val sourceExtract = ParenthesesUtils.extract(entry.sourceText.trim())
            val targetExtract = ParenthesesUtils.extract(entry.targetText.trim())
            val sourceText = sourceExtract.cleanText
            val targetText = targetExtract.cleanText
            val extractedNotes = sourceExtract.notes + targetExtract.notes
            val mergedEntryNotes = ParenthesesUtils.mergeNotes(entry.extraLabel, extractedNotes)

            try {
                val existingConceptId = conceptRepository.findActiveConceptIdByText(sourceLanguage, sourceText)

                if (existingConceptId != null) {
                    val existingConcept = conceptRepository.getById(existingConceptId)
                    if (existingConcept != null && !mergedEntryNotes.isNullOrBlank()) {
                        val mergedNotes = ParenthesesUtils.mergeNotes(existingConcept.notes, listOf(mergedEntryNotes))
                        if (mergedNotes != existingConcept.notes) conceptRepository.update(existingConcept.copy(notes = mergedNotes, updatedAt = now))
                    }
                    val alreadyHasThisMeaning = conceptRepository.hasTranslation(existingConceptId, targetLanguage, targetText)
                    if (alreadyHasThisMeaning) {
                        duplicateCount++
                    } else {
                        conceptRepository.addTranslation(
                            existingConceptId,
                            ContentItem(languageCode = targetLanguage, text = targetText)
                        )
                        translationsAddedCount++
                    }
                    continue
                }

                val concept = Concept(
                    id = 0,
                    uuid = UUID.randomUUID().toString(),
                    contentType = ContentType.WORD,
                    categoryId = categoryId,
                    favorite = false,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    notes = mergedEntryNotes,
                    contents = listOf(
                        ContentItem(languageCode = sourceLanguage, text = sourceText),
                        ContentItem(languageCode = targetLanguage, text = targetText)
                    ),
                    tags = emptyList()
                )
                val conceptId = conceptRepository.insert(concept)
                learningStateRepository.save(LearningState(conceptId = conceptId))
                insertedCount++
            } catch (e: Exception) {
                // این ردیف Skip می‌شود؛ بقیه ردیف‌ها (که هیچ وابستگی به این یکی ندارند) ادامه پیدا می‌کنند.
            }
        }

        return ImportOutcome(
            insertedCount = insertedCount,
            translationsAddedCount = translationsAddedCount,
            duplicateCount = duplicateCount
        )
    }
}
