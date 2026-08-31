package com.app.flashlearn.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.database.entity.*

@Database(
    entities = [
        ConceptEntity::class,
        ContentEntity::class,
        LearningStateEntity::class,
        LanguageEntity::class,
        ReviewHistoryEntity::class,
        ReviewSessionEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        ConceptTagEntity::class,
        LanguagePairEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FlashLearnDatabase : RoomDatabase() {
    abstract fun conceptDao(): ConceptDao
    abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun conceptTagDao(): ConceptTagDao
    abstract fun languageDao(): LanguageDao
    abstract fun languagePairDao(): LanguagePairDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "flashlearn.db"
    }
}
