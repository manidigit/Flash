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
class FlashLearnApplication : Application()
