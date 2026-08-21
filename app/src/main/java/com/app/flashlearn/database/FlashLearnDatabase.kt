package com.app.flashlearn.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.dao.CategoryDao
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.ContentDao
import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.dao.LanguagePairDao
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.database.dao.ReviewSessionDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.database.entity.CategoryEntity
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.ContentEntity
import com.app.flashlearn.database.entity.LanguageEntity
import com.app.flashlearn.database.entity.LanguagePairEntity
import com.app.flashlearn.database.entity.LearningStateEntity
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.database.entity.ReviewSessionEntity
import com.app.flashlearn.database.entity.TagEntity

/**
 * نسخه 1 دیتابیس. هر تغییر Schema در آینده باید Migration مربوطه را در
 * database/migration/Migrations.kt اضافه کند و exportSchema فعال بماند
 * تا فایل‌های JSON schema برای تست Migration در app/schemas ذخیره شوند.
 */
@Database(
    entities = [
        LanguageEntity::class,
        ConceptEntity::class,
        ContentEntity::class,
        LearningStateEntity::class,
        ReviewHistoryEntity::class,
        ReviewSessionEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        ConceptTagEntity::class,
        LanguagePairEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class FlashLearnDatabase : RoomDatabase() {

    abstract fun languageDao(): LanguageDao
    abstract fun conceptDao(): ConceptDao
    abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun languagePairDao(): LanguagePairDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "flashlearn.db"
    }
}
