package com.app.flashlearn.core.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    fun now(): Long = System.currentTimeMillis()

    fun addDays(fromMillis: Long, days: Int): Long =
        fromMillis + TimeUnit.DAYS.toMillis(days.toLong())

    /** برای ساخت Session id مثل 2026-08-16 */
    fun todayDatePrefix(fromMillis: Long = now()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(fromMillis)
    }

    /** ابتدای روز جاری (00:00) برای بازه‌بندی آمار "Today". */
    fun startOfDay(fromMillis: Long = now()): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = fromMillis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
