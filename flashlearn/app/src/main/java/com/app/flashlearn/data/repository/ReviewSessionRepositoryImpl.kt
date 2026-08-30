package com.app.flashlearn.data.repository

import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.database.dao.ReviewSessionDao
import com.app.flashlearn.database.entity.ReviewSessionEntity
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import javax.inject.Inject

class ReviewSessionRepositoryImpl @Inject constructor(
    private val dao: ReviewSessionDao
) : ReviewSessionRepository {

    /**
     * رفع باگ (کرش/«هیچ کلمه‌ای وجود ندارد» بعد از Import بکاپ): قبلاً شناسه Session بر
     * اساس «تعداد Session های همین امروز» ساخته می‌شد (مثل 2026-08-16-003). وقتی بکاپی که
     * از قبل شامل Session های همان روز است Import می‌شود، این شمارش با ردیف‌های واردشده
     * به‌هم می‌ریزد و شناسه تازه‌ساخته‌شده گاهی دقیقاً با یک شناسه Import‌شده برخورد
     * می‌کند؛ چون Insert با OnConflictStrategy.ABORT است، این برخورد یک Exception پرتاب
     * می‌کند که کل بارگذاری صفحه مرور (برای هر نوع مروری) را از کار می‌انداخت. بعد از
     * نیمه‌شب چون تاریخ (و پیشوند Prefix) عوض می‌شود، شمارش از صفر شروع می‌شود و برخورد
     * موقتاً رخ نمی‌دهد - دقیقاً همان الگوی «بعد از ۱۲ شب درست می‌شود» که مشاهده شده بود.
     *
     * اصلاح: پسوند شناسه دیگر یک شمارنده ترتیبی شکننده نیست؛ از epoch میلی‌ثانیه لحظه شروع
     * استفاده می‌شود که عملاً تضمین می‌کند حتی بعد از هر تعداد Import/Merge هرگز با هیچ
     * شناسه دیگری برخورد نکند (فرمت خوانا مثل 2026-08-16 هم حفظ شده).
     */
    override suspend fun startSession(reviewType: String, startedAt: Long): String {
        val datePrefix = DateTimeUtils.todayDatePrefix(startedAt)
        val sessionId = "$datePrefix-$startedAt"

        dao.insert(
            ReviewSessionEntity(
                id = sessionId,
                startedAt = startedAt,
                reviewType = reviewType
            )
        )
        return sessionId
    }

    override suspend fun endSession(sessionId: String, endedAt: Long) {
        val session = dao.getById(sessionId) ?: return
        dao.update(session.copy(endedAt = endedAt))
    }
}
