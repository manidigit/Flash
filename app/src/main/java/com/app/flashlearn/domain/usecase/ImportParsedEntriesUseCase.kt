package com.app.flashlearn.domain.usecase

import com.app.flashlearn.core.util.DateTimeUtils
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
 * نتیجه واقعی Import: چند مورد درج شد و چند مورد به‌خاطر تکراری بودن رد شد.
 * قبلاً فقط یک Int برمی‌گشت و کاربر هیچ بازخوردی درباره موارد تکراری نمی‌گرفت.
 */
data class ImportOutcome(val insertedCount: Int, val duplicateCount: Int)

/**
 * ذخیره‌سازی مشترک برای هر جریانی که یک لیست ParsedVocabularyEntry تولید می‌کند
 * (Paste Text و Import File — بند 42 و 43). هر رکورد یک Concept جدید با LearningState
 * اولیه DAILY می‌شود.
 *
 * بند 64 (Edge Case «Import ناقص»): هر ردیف کاملاً مستقل از بقیه است. اگر یک ردیف به هر
 * دلیلی (مثلاً محدودیت طول متن) درج نشود، بقیه ردیف‌های موفق نباید از دست بروند و کل
 * عملیات نباید Exception پرتاب کند و برنامه را Crash کند؛ فقط همان ردیف Skip می‌شود.
 *
 * تشخیص تکراری (رفع باگ): قبلاً هیچ بررسی Duplicate‌ای انجام نمی‌شد، پس اگر کاربر یک
 * گروه کلمه را چندبار Paste/Import می‌کرد (مثلاً یک متن یکسان را دوبار Copy می‌کرد)،
 * همان کلمه چندبار به‌عنوان Concept جدا اضافه می‌شد. حالا هر متن مبدأ (نرمال‌شده با
 * Trim + lower-case) هم در برابر دیتابیس فعلی (existsByText) و هم درون همین دسته Import
 * فعلی (با یک Set محلی، چون خود دیتابیس تا پایان تراکنش این دسته از قبلی‌های همین دسته
 * خبر ندارد) بررسی می‌شود.
 */
class ImportParsedEntriesUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(
        entries: List<ParsedVocabularyEntry>,
        sourceLanguage: String,
        targetLanguage: String
    ): ImportOutcome {
        val now = DateTimeUtils.now()
        var insertedCount = 0
        var duplicateCount = 0
        val seenInThisBatch = HashSet<String>()

        for (entry in entries) {
            if (entry.sourceText.isBlank() || entry.targetText.isBlank()) continue

            val normalizedSource = entry.sourceText.trim().lowercase()

            try {
                val isDuplicateInBatch = !seenInThisBatch.add(normalizedSource)
                val isDuplicateInDb = !isDuplicateInBatch &&
                    conceptRepository.existsByText(sourceLanguage, normalizedSource)

                if (isDuplicateInBatch || isDuplicateInDb) {
                    duplicateCount++
                    continue
                }

                val concept = Concept(
                    id = 0,
                    uuid = UUID.randomUUID().toString(),
                    contentType = ContentType.WORD,
                    categoryId = null,
                    favorite = false,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    notes = entry.extraLabel,
                    contents = listOf(
                        ContentItem(languageCode = sourceLanguage, text = entry.sourceText.trim()),
                        ContentItem(languageCode = targetLanguage, text = entry.targetText.trim())
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

        return ImportOutcome(insertedCount = insertedCount, duplicateCount = duplicateCount)
    }
}
