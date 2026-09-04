package com.app.flashlearn.data.ai

import com.app.flashlearn.domain.model.TranslationRequest
import com.app.flashlearn.domain.model.TranslationResult
import com.app.flashlearn.domain.service.AITranslationService
import com.app.flashlearn.domain.service.SecureKeyValueStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

// هشدار: این پیاده‌سازی حدسی و تست‌نشده است (بدون شبکه/کلید واقعی نوشته شده).
// قبل از استفاده واقعی: provider را مشخص کن و با کلید واقعی تست کن.
class AITranslationServiceImpl @Inject constructor(
    private val secureKeyValueStore: SecureKeyValueStore,
    private val httpClient: OkHttpClient
) : AITranslationService {

    companion object {
        private const val PROVIDER_KEY = "openai_compatible"
        private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"
    }

    override suspend fun isConfigured(): Boolean =
        secureKeyValueStore.getApiKey(PROVIDER_KEY) != null

    override suspend fun translate(request: TranslationRequest): Result<TranslationResult> {
        val apiKey = secureKeyValueStore.getApiKey(PROVIDER_KEY)
            ?: return Result.failure(IllegalStateException("کلید API تنظیم نشده"))

        return try {
            val prompt = buildPrompt(request)
            val responseJson = withContext(Dispatchers.IO) { callApi(apiKey, prompt) }
            Result.success(parseResponse(responseJson))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(request: TranslationRequest): String = """
        ترجمه کن از ${request.sourceLanguageCode} به ${request.targetLanguageCode}: "${request.sourceText}"
        فقط JSON برگردون با کلیدهای: translatedText, pronunciation, definition, example, grammarNote
        هیچ توضیح اضافه‌ای نده.
    """.trimIndent()

    private fun callApi(apiKey: String, prompt: String): String {
        val body = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }
        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("AI API error: ${response.code}")
            return response.body?.string() ?: throw IOException("پاسخ خالی از AI")
        }
    }

    private fun parseResponse(raw: String): TranslationResult {
        val content = JSONObject(raw)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        val json = JSONObject(content)
        return TranslationResult(
            translatedText = json.optString("translatedText"),
            pronunciation = json.optString("pronunciation").ifBlank { null },
            definition = json.optString("definition").ifBlank { null },
            example = json.optString("example").ifBlank { null },
            grammarNote = json.optString("grammarNote").ifBlank { null }
        )
    }
}
