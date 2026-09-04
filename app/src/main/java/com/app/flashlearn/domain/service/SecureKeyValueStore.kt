package com.app.flashlearn.domain.service

interface SecureKeyValueStore {
    suspend fun saveApiKey(provider: String, key: String)
    suspend fun getApiKey(provider: String): String?
    suspend fun clearApiKey(provider: String)
}
