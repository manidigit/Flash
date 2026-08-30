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
    companion object {
        /**
         * رفع باگ («۶ کلمه پیش‌فرض هر بار Import بکاپ تکرار می‌شوند»): قبلاً این ۶ Concept
         * با UUID تصادفی (UUID.randomUUID()) ساخته می‌شدند. چون applyImport فقط بر اساس
         * uuid تشخیص Duplicate می‌دهد (نه متن)، و هر نصب تازه یک UUID تصادفی جدید برای
         * همین ۶ کلمه می‌سازد، وارد کردن بکاپ از هر دستگاه دیگری (که خودش هم این ۶ کلمه
         * پیش‌فرض را با UUID تصادفی خودش ساخته) همیشه به‌عنوان «۶ Concept کاملاً جدید»
         * تشخیص داده می‌شد. با UUID ثابت و از‌پیش‌مشخص برای هرکدام، این ۶ کلمه در همه
         * دستگاه‌ها همیشه همان شناسه یکسان را دارند و Import آن‌ها را درست شناسایی می‌کند.
         */
        private const val SEED_UUID_MANZANA = "seed-0001-manzana-flashlearn"
        private const val SEED_UUID_CASA = "seed-0002-casa-flashlearn"
        private const val SEED_UUID_COMER = "seed-0003-comer-flashlearn"
        private const val SEED_UUID_VIAJAR = "seed-0004-viajar-flashlearn"
        private const val SEED_UUID_HOTEL = "seed-0005-hotel-flashlearn"
        private const val SEED_UUID_GRACIAS = "seed-0006-gracias-flashlearn"
    }

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
            SEED_UUID_MANZANA to ("manzana" to "سیب"),
            SEED_UUID_CASA to ("casa" to "خانه"),
            SEED_UUID_COMER to ("comer" to "خوردن"),
            SEED_UUID_VIAJAR to ("viajar" to "سفر کردن"),
            SEED_UUID_HOTEL to ("hotel" to "هتل"),
            SEED_UUID_GRACIAS to ("gracias" to "ممنون")
        )

        val now = DateTimeUtils.now()
        for ((seedUuid, pair) in samples) {
            val (es, fa) = pair
            val conceptId = conceptRepository.insert(
                Concept(
                    id = 0,
                    uuid = seedUuid,
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
