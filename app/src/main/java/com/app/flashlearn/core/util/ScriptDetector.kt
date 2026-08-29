package com.app.flashlearn.core.util

/**
 * تشخیص سبک/تقریبی خط الفبا (Script) یک متن، فقط برای هشدار Paste/Import (بند 64).
 * این یک تشخیص زبان کامل نیست؛ فقط بین «الفبای فارسی/عربی» و «سایر الفباها» فرق می‌گذارد،
 * چون رایج‌ترین حالت اشتباه پارس (مثلاً پشت‌سرهم افتادن دو خط فارسی به‌جای یک جفت
 * مبدأ/مقصد واقعی) دقیقاً همین‌جا خودش را نشان می‌دهد.
 */
object ScriptDetector {

    private val persianArabicRange = 0x0600..0x06FF

    /** زبان‌هایی که انتظار می‌رود متن‌شان با الفبای فارسی/عربی نوشته شده باشد. */
    private val persianArabicLanguages = setOf("fa", "ar", "ur", "ps")

    private fun hasPersianArabicScript(text: String): Boolean =
        // فقط حروف واقعی شمرده می‌شوند، نه علامت‌های نگارشی؛ رفع باگ: علامت سؤال فارسی
        // «؟» (U+061F) و ویرگول فارسی «،» هم داخل همین بازه یونیکد هستند ولی حرف نیستند،
        // پس یک جمله اسپانیایی که فقط علامت سؤالش «؟» باشد نباید به‌اشتباه «فارسی/عربی»
        // تشخیص داده شود.
        text.any { it.isLetter() && it.code in persianArabicRange }

    private fun hasLatinLetters(text: String): Boolean =
        text.any { it.isLetter() && it.code < 0x0250 }

    /**
     * آیا الفبای این متن با زبان انتخاب‌شده همخوانی ندارد؟ فقط برای زبان‌های فارسی/عربی در
     * برابر زبان‌های لاتین‌نویس بررسی می‌شود (چون تفاوت خط‌شان زیاد و قابل اتکاست)؛ برای بقیه
     * جفت‌زبان‌ها (که تشخیص مطمئن سخت‌تر است) هیچ هشداری داده نمی‌شود تا False Positive
     * نداشته باشیم.
     */
    fun likelyScriptMismatch(languageCode: String, text: String): Boolean {
        if (text.isBlank()) return false
        val expectsPersianArabic = languageCode.lowercase() in persianArabicLanguages
        val actuallyPersianArabic = hasPersianArabicScript(text)
        val actuallyLatin = hasLatinLetters(text)

        return when {
            expectsPersianArabic && !actuallyPersianArabic && actuallyLatin -> true
            !expectsPersianArabic && actuallyPersianArabic -> true
            else -> false
        }
    }
}
