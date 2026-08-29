package com.app.flashlearn.domain.model

/**
 * یک رکورد استخراج‌شده از متن Paste شده یا فایل Import شده، قبل از تأیید نهایی کاربر
 * (بند 42 و 43). localId فقط برای شناسایی در UI هنگام ویرایش/حذف استفاده می‌شود، ربطی به
 * Database ندارد.
 *
 * scriptMismatch (رفع باگ): پارسر فقط ساختاری کار می‌کند، نه معنایی؛ نمی‌تواند تشخیص دهد
 * دو خط پشت‌سرهم واقعاً یک کلمه و ترجمه‌اش هستند یا دو متن بی‌ربط که تصادفاً پشت‌سرهم
 * افتاده‌اند. اما وقتی خط الفبای متن با زبان انتخاب‌شده (مثلاً حروف فارسی/عربی برای زبان
 * لاتین‌نویس یا برعکس) همخوانی نداشته باشد، این یک نشانه قوی از پارس اشتباه است؛ چنین
 * ردیفی هشدار می‌گیرد و پیش‌فرض از حالت انتخاب‌شده خارج می‌شود تا کاربر آگاهانه بررسی کند.
 */
data class ParsedVocabularyEntry(
    val localId: Int,
    val sourceText: String,
    val targetText: String,
    val extraLabel: String? = null,
    val included: Boolean = true,
    val scriptMismatch: Boolean = false
)
