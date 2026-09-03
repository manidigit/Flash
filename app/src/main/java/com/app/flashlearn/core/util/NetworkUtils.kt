package com.app.flashlearn.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * بررسی اتصال اینترنت. طبق اصل Offline-First (بند 3)، فقط قابلیت‌های AI به این وابسته‌اند؛
 * هیچ بخش دیگری از اپلیکیشن نباید از این کلاس استفاده کند.
 */
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isOnline(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
