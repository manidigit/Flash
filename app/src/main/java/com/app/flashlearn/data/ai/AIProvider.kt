package com.app.flashlearn.data.ai

/**
 * قرارداد یک Provider ترجمه AI (بند 76). با پیاده‌سازی این Interface می‌توان
 * هر سرویس دیگری (OpenAI-compatible، Anthropic، یک مدل محلی و ...) را بدون تغییر
 * در AITranslationServiceImpl یا لایه UI جایگزین کرد.
 */
interface AIProvider {
    /**
     * prompt را به مدل می‌فرستد و متن خام پاسخ را برمی‌گرداند (انتظار می‌رود JSON باشد،
     * اما Parse و Validate آن بر عهده AITranslationServiceImpl است، نه Provider).
     */
    suspend fun generate(prompt: String, endpoint: String, apiKey: String, model: String): String
}
