package com.app.flashlearn.data.ai

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * پیاده‌سازی پیش‌فرض AIProvider برای هر سرویسی که Chat Completions API سازگار با
 * OpenAI را پیاده‌سازی کرده باشد (شامل بسیاری از سرویس‌های Self-hosted/Proxy).
 * از HttpURLConnection استفاده می‌شود تا هیچ وابستگی شبکه‌ای اضافه‌ای لازم نباشد.
 */
class OpenAICompatibleProvider @Inject constructor() : AIProvider {

    override suspend fun generate(prompt: String, endpoint: String, apiKey: String, model: String): String {
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            connection.connectTimeout = TimeUnit.SECONDS.toMillis(15).toInt()
            connection.readTimeout = TimeUnit.SECONDS.toMillis(30).toInt()

            val body = JSONObject()
                .put("model", model)
                .put(
                    "messages",
                    JSONArray().put(
                        JSONObject().put("role", "user").put("content", prompt)
                    )
                )
                .put("temperature", 0.2)

            OutputStreamWriter(connection.outputStream).use { it.write(body.toString()) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (responseCode !in 200..299) {
                throw IllegalStateException("AI provider returned HTTP $responseCode: $responseText")
            }

            val json = JSONObject(responseText)
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } finally {
            connection.disconnect()
        }
    }
}
