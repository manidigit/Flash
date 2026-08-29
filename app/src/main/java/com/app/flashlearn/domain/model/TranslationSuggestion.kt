package com.app.flashlearn.domain.model

/**
 * خروجی ساختاریافته AI (بند 77). فقط یک پیشنهاد است؛ تا کاربر Approve نکند
 * هیچ‌چیز در دیتابیس ذخیره نمی‌شود (بند 41 و 78).
 */
data class TranslationSuggestion(
    val sourceText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translation: String,
    val pronunciation: String?,
    val partOfSpeech: String?,
    val definition: String?,
    val example: String?,
    val notes: String?
)
