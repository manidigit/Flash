package com.app.flashlearn.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.database.entity.*

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
    abstract fun languagePairDao(): LanguagePairDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "flashlearn.db"
    }
}
