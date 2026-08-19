package com.app.flashlearn.core.seed

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguageRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import java.util.UUID
import javax.inject.Inject

/**
 * دیتای اولیه لازم برای استفاده اپلیکیشن: لیست زبان‌ها (بند 13) و چند Concept نمونه
 * اسپانیایی -> فارسی (بند 73) تا کاربر بلافاصله بعد از نصب چیزی برای مرور داشته باشد.
 * فقط یک‌بار اجرا می‌شود (اگر جدول Language خالی باشد).
 */
class DatabaseSeeder @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository
) {
    suspend fun seedIfNeeded() {
        if (languageRepository.getAll().isNotEmpty()) return

        languageRepository.insertAll(
            listOf(
                Language("fa", "فارسی"),
                Language("en", "English"),
                Language("es", "Español"),
                Language("de", "Deutsch"),
                Language("fr", "Français"),
                Language("ar", "العربية"),
                Language("tr", "Türkçe"),
                Language("ru", "Русский")
            )
        )

        val samples = listOf(
            "manzana" to "سیب",
            "casa" to "خانه",
            "comer" to "خوردن",
            "viajar" to "سفر کردن",
            "hotel" to "هتل",
            "gracias" to "ممنون"
        )

        val now = DateTimeUtils.now()
        for ((es, fa) in samples) {
            val conceptId = conceptRepository.insert(
                Concept(
                    id = 0,
                    uuid = UUID.randomUUID().toString(),
                    contentType = ContentType.WORD,
                    categoryId = null,
                    favorite = false,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    notes = null,
                    contents = listOf(
                        ContentItem(languageCode = "es", text = es),
                        ContentItem(languageCode = "fa", text = fa)
                    ),
                    tags = emptyList()
                )
            )
            learningStateRepository.save(LearningState(conceptId = conceptId))
        }
    }
}
