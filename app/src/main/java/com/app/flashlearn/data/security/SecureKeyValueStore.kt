package com.app.flashlearn.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_FILE_NAME = "flashlearn_secure_prefs"

/**
 * ذخیره‌سازی رمزنگاری‌شده برای اطلاعات حساس (فعلاً فقط AI API Key — بند 76).
 * از Android Keystore برای رمزنگاری کلید استفاده می‌کند (EncryptedSharedPreferences)،
 * نه دیتابیس معمولی که به‌صورت متن ساده روی دیسک است.
 */
@Singleton
class SecureKeyValueStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    suspend fun set(key: String, value: String?) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(key) else putString(key, value)
        }.apply()
    }
}
