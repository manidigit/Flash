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

val ALL_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
