package com.app.flashlearn.data.ai

import com.app.flashlearn.core.util.NetworkUtils
import com.app.flashlearn.data.security.SecureKeyValueStore
import com.app.flashlearn.domain.model.TranslationSuggestion
import com.app.flashlearn.domain.repository.SettingsRepository
import com.app.flashlearn.domain.service.AITranslationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

private const val SECURE_KEY_AI_API_KEY = "ai_api_key"

/**
 * بند 77-78: از مدل خروجی Structured JSON خواسته می‌شود، خروجی Validate/Parse می‌شود
 * و در صورت خطا هرگز چیزی در دیتابیس ذخیره نمی‌شود. API Key از SecureKeyValueStore
 * (رمزنگاری‌شده) خوانده می‌شود، نه از دیتابیس معمولی.
 */
class AITranslationServiceImpl @Inject constructor(
    private val provider: AIProvider,
    private val settingsRepository: SettingsRepository,
    private val secureKeyValueStore: SecureKeyValueStore,
    private val networkUtils: NetworkUtils
) : AITranslationService {

    override suspend fun isAvailable(): Boolean {
        if (!networkUtils.isOnline()) return false
        val endpoint = settingsRepository.getValue(SettingsRepository.AI_ENDPOINT)
        val apiKey = secureKeyValueStore.get(SECURE_KEY_AI_API_KEY)
        return !endpoint.isNullOrBlank() && !apiKey.isNullOrBlank()
    }

    override suspend fun translate(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslationSuggestion> = withContext(Dispatchers.IO) {
        if (!networkUtils.isOnline()) {
            return@withContext Result.failure(IllegalStateException("اتصال اینترنت برقرار نیست"))
        }

        val endpoint = settingsRepository.getValue(SettingsRepository.AI_ENDPOINT)
        val apiKey = secureKeyValueStore.get(SECURE_KEY_AI_API_KEY)
        val model = settingsRepository.getValue(SettingsRepository.AI_MODEL)

        if (endpoint.isNullOrBlank() || apiKey.isNullOrBlank() || model.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("تنظیمات AI کامل نیست"))
        }

        try {
            val prompt = buildPrompt(sourceText, sourceLanguage, targetLanguage)
            val rawResponse = provider.generate(prompt, endpoint, apiKey, model)
            val suggestion = parseResponse(rawResponse, sourceText, sourceLanguage, targetLanguage)
            Result.success(suggestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(sourceText: String, sourceLanguage: String, targetLanguage: String): String =
        """
        Translate the following text from language code "$sourceLanguage" to language code "$targetLanguage".
        Text: "$sourceText"

        Respond with ONLY a JSON object (no markdown, no extra text) with exactly these keys:
        translation, pronunciation, partOfSpeech, definition, example, notes.
        Use null (not the string "null") for any field you cannot determine.
        """.trimIndent()

    /** طبق بند 78: هر خروجی AI باید Validate/Parse شود؛ فیلد ضروری translation باید غیرخالی باشد. */
    private fun parseResponse(
        raw: String,
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String
    ): TranslationSuggestion {
        val cleaned = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(cleaned)

        val translation = json.optString("translation").ifBlank {
            throw IllegalStateException("پاسخ AI فاقد فیلد translation معتبر است")
        }

        return TranslationSuggestion(
            sourceText = sourceText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            translation = translation,
            pronunciation = json.optStringOrNull("pronunciation"),
            partOfSpeech = json.optStringOrNull("partOfSpeech"),
            definition = json.optStringOrNull("definition"),
            example = json.optStringOrNull("example"),
            notes = json.optStringOrNull("notes")
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (isNull(key)) return null
        val value = optString(key, "")
        return value.ifBlank { null }
    }
}
