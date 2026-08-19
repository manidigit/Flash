package com.app.flashlearn.domain.model

/**
 * یک رکورد استخراج‌شده از متن Paste شده، قبل از تأیید نهایی کاربر (بند 42).
 * localId فقط برای شناسایی در UI هنگام ویرایش/حذف استفاده می‌شود، ربطی به Database ندارد.
 */
data class ParsedVocabularyEntry(
    val localId: Int,
    val sourceText: String,
    val targetText: String,
    val extraLabel: String? = null,
    val included: Boolean = true
)
