package com.app.flashlearn.data.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.app.flashlearn.domain.service.SecureKeyValueStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SecureKeyValueStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecureKeyValueStore {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "flashlearn_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveApiKey(provider: String, key: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString("api_key_$provider", key).apply()
    }

    override suspend fun getApiKey(provider: String): String? = withContext(Dispatchers.IO) {
        prefs.getString("api_key_$provider", null)
    }

    override suspend fun clearApiKey(provider: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove("api_key_$provider").apply()
    }
}
