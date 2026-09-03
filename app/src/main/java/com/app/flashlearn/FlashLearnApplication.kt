package com.app.flashlearn

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * رفع باگ (کلمات پیش‌فرض تکراری بعد از Import بکاپ): قبلاً اینجا Seed کردن دیتابیس
 * (زبان‌ها + ۶ کلمه نمونه) با GlobalScope.launch به‌صورت ناهمگام و بدون انتظار اجرا
 * می‌شد. روی یک نصب تازه، اگر کاربر سریع Onboarding را رد می‌کرد و بلافاصله می‌رفت
 * Backup/Restore → Import، این Coroutine ناهمگام (که هنوز کامل نشده بود) با فرآیند
 * Import هم‌زمان روی دیتابیس می‌نوشت؛ نتیجه گاهی هم ۶ کلمه نمونه و هم کلمات واقعی بکاپ
 * با هم داخل دیتابیس می‌ماندند. Seed کردن حالا به‌طور Synchronous (منتظرمانده) داخل
 * MainActivity، قبل از تعیین صفحه شروع، انجام می‌شود (نگاه کنید به AppRoot در
 * MainActivity.kt) تا تضمین شود همیشه قبل از هر تعامل کاربر (از جمله Import) کامل شده.
 */
@HiltAndroidApp
class FlashLearnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
    }

    /**
     * چون دسترسی به adb logcat روی گوشی بدون کامپیوتر سخت است، هر Crash خام قبل از
     * اینکه سیستم اپ را ببندد، در یک فایل متنی ساده در حافظه داخلی اپ نوشته می‌شود.
     * این فایل با Termux قابل خواندن است، بدون نیاز به هیچ‌گونه دسترسی خاص:
     *   cat /data/data/com.app.flashlearn/files/crash_log.txt   (نیاز به run-as دارد)
     * یا ساده‌تر، مسیر بیرونی:
     *   ls /sdcard/Android/data/com.app.flashlearn/files/
     */
    private fun installCrashLogger() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val logText = buildString {
                    appendLine("=== FlashLearn Crash Log ===")
                    appendLine("Time: ${java.util.Date()}")
                    appendLine("Thread: ${thread.name}")
                    appendLine()
                    appendLine(android.util.Log.getStackTraceString(throwable))
                }
                getExternalFilesDir(null)?.let { dir ->
                    java.io.File(dir, "crash_log.txt").writeText(logText)
                }
                java.io.File(filesDir, "crash_log.txt").writeText(logText)
            } catch (loggingError: Exception) {
                // اگر نوشتن لاگ خودش شکست خورد، حداقل جلوی هندلر اصلی را نمی‌گیریم.
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
