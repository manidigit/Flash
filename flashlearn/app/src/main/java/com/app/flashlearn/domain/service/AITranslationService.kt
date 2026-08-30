package com.app.flashlearn.domain.service

import com.app.flashlearn.domain.model.TranslationSuggestion

/**
 * سرویس ترجمه با AI (بند 41 و 76-78). Provider واقعی در لایه Data پیاده‌سازی و
 * از طریق Hilt تزریق می‌شود تا بتوان بدون تغییر در بقیه اپلیکیشن آن را عوض کرد.
 * طبق اصل Offline-First (بند 3)، اگر اینترنت یا تنظیمات AI در دسترس نباشد، این متد
 * با یک Result.failure واضح برمی‌گردد؛ بقیه اپلیکیشن باید بدون این قابلیت کار کند.
 */
interface AITranslationService {
    suspend fun translate(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslationSuggestion>

    /** آیا Endpoint/API Key تنظیم شده و اتصال اینترنت برقرار است. */
    suspend fun isAvailable(): Boolean
}
