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
 * ذخیره‌سازی مشترک برای هر جریانی که یک لیست ParsedVocabularyEntry تولید می‌کند
 * (Paste Text و Import File — بند 42 و 43). هر رکورد یک Concept جدید با LearningState
 * اولیه DAILY می‌شود؛ تشخیص Duplicate در این UseCase انجام نمی‌شود (این ورودی‌ها همیشه
 * موارد کاملاً جدید کاربر هستند، نه یک Backup که ممکن است تکراری باشد — آن مورد در
 * BackupRepository/JsonBackupServiceImpl پوشش داده شده).
 *
 * بند 64 (Edge Case «Import ناقص»): برخلاف applyImport یک Backup (که باید یک تراکنش
 * همه‌یا‌هیچ باشد چون رکوردها به هم وابسته‌اند)، اینجا هر ردیف کاملاً مستقل است. اگر یک
 * ردیف به هر دلیلی (مثلاً محدودیت طول متن) درج نشود، بقیه ردیف‌های موفق نباید از دست
 * بروند و کل عملیات نباید Exception پرتاب کند و برنامه را Crash کند؛ فقط همان ردیف Skip
 * می‌شود و شمارش نهایی واقعی (نه خوش‌بینانه) به UI برمی‌گردد.
 */
class ImportParsedEntriesUseCase @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend operator fun invoke(
        entries: List<ParsedVocabularyEntry>,
        sourceLanguage: String,
        targetLanguage: String
    ): Int {
        val now = DateTimeUtils.now()
        var count = 0

        for (entry in entries) {
            if (entry.sourceText.isBlank() || entry.targetText.isBlank()) continue

            try {
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
                count++
            } catch (e: Exception) {
                // این ردیف Skip می‌شود؛ بقیه ردیف‌ها (که هیچ وابستگی به این یکی ندارند) ادامه پیدا می‌کنند.
            }
        }

        return count
    }
}
