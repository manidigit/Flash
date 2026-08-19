package com.app.flashlearn

import android.app.Application
import com.app.flashlearn.core.seed.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FlashLearnApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        // فقط یک‌بار اجرا می‌شود (seedIfNeeded چک می‌کند جدول زبان‌ها خالی است یا نه).
        GlobalScope.launch { databaseSeeder.seedIfNeeded() }
    }
}
