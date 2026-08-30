package com.app.flashlearn.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * لیست Migration های دیتابیس. fallbackToDestructiveMigration هرگز نباید در Release
 * استفاده شود چون داده کاربر را پاک می‌کند.
 */

/**
 * نسخه 1 -> 2 (بند 64، رفع باگ «کلمه با چند معنی»): Index ترکیبی (conceptId, languageCode)
 * جدول contents از Unique به غیر-Unique تغییر کرد تا یک Concept بتواند چند ترجمه/معنی در
 * همان زبان مقصد داشته باشد. چون SQLite امکان تغییر مستقیم Unique-ness یک Index موجود را
 * نمی‌دهد، باید Index قدیمی حذف و همان Index بدون Unique دوباره ساخته شود. نام Index دقیقاً
 * همان نامی است که Room به‌طور خودکار از روی ستون‌ها می‌سازد (index_contents_conceptId_languageCode)،
 * پس هیچ داده‌ای پاک یا جابه‌جا نمی‌شود؛ فقط محدودیت Unique برداشته می‌شود.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS index_contents_conceptId_languageCode")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_contents_conceptId_languageCode " +
                "ON contents(conceptId, languageCode)"
        )
    }
}

/**
 * نسخه 2 -> 3 (سیستم طبقه‌بندی سختی سازگار / Adaptive Difficulty): افزودن ستون‌های آماری
 * دقیق به‌ازای هر مرحله به جدول learning_states، برای محاسبه خودکار سختی از روی کل
 * تاریخچه واقعی مرور به‌جای یک شمارنده ساده. همه ستون‌های جدید مقدار پیش‌فرض دارند، پس
 * هیچ داده موجودی پاک یا تغییر نمی‌کند؛ کلماتی که از قبل در دیتابیس هستند فقط از این پس
 * آمار جدید را جمع می‌کنند (تاریخچه گذشته‌شان که قبلاً ثبت نشده، قابل بازسازی نیست، اما
 * چیزی هم پاک نمی‌شود).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE learning_states ADD COLUMN dailyReviewCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN dailyCorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN dailyIncorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN weeklyReviewCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN weeklyCorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN weeklyIncorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN monthlyReviewCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN monthlyCorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN monthlyIncorrectCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN consecutiveCorrect INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN consecutiveIncorrect INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN highestStageReached TEXT NOT NULL DEFAULT 'DAILY'")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN weeklyToDailyReturns INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN monthlyToDailyReturns INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN monthlyCompletions INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN learnedCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN lastReviewResult INTEGER")
        db.execSQL("ALTER TABLE learning_states ADD COLUMN difficultyScore INTEGER NOT NULL DEFAULT 0")

        // بازسازی حداقلی برای کلماتی که از قبل در دیتابیس بودند: کلماتی که همین الان در
        // WEEKLY/MONTHLY/LEARNED هستند حتماً حداقل یک‌بار به آن مرحله رسیده‌اند، پس
        // highestStageReached آن‌ها را همان مقدار فعلی stage قرار می‌دهیم (به‌جای مقدار
        // پیش‌فرض نادرست DAILY).
        db.execSQL("UPDATE learning_states SET highestStageReached = stage WHERE stage != 'DAILY'")
    }
}

val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
