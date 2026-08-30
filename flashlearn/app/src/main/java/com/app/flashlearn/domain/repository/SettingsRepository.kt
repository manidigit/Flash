package com.app.flashlearn.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun setValue(key: String, value: String)
    suspend fun getValue(key: String): String?
    fun observeValue(key: String): Flow<String?>

    companion object Keys {
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val THEME_MODE = "theme_mode" // "light" | "dark" | "system"

        // تنظیمات AI (بند 76): Endpoint/Model حساس نیستند و اینجا ذخیره می‌شوند؛
        // API Key حساس است و در SecureKeyValueStore (رمزنگاری‌شده با Android Keystore) ذخیره می‌شود.
        const val AI_ENDPOINT = "ai_endpoint"
        const val AI_MODEL = "ai_model"
    }
}
