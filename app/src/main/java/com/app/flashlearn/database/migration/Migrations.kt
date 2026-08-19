package com.app.flashlearn.database.migration

import androidx.room.migration.Migration

/**
 * لیست Migration های دیتابیس. در حال حاضر فقط نسخه 1 وجود دارد،
 * پس این لیست خالی است. هر Migration بعدی اینجا اضافه می‌شود، مثلاً:
 *
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE concepts ADD COLUMN newField TEXT")
 *     }
 * }
 *
 * سپس در Database.databaseBuilder(...).addMigrations(MIGRATION_1_2, ...) اضافه شود.
 * fallbackToDestructiveMigration هرگز نباید در Release استفاده شود چون داده کاربر را پاک می‌کند.
 */
val ALL_MIGRATIONS: Array<Migration> = arrayOf()
