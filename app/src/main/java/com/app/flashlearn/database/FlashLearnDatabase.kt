package com.app.flashlearn.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.database.entity.*

@Database(
    entities = [
        LanguageEntity::class,
        CategoryEntity::class,
        ConceptEntity::class,
        ContentEntity::class,
        LearningStateEntity::class,
        ReviewSessionEntity::class,
        ReviewHistoryEntity::class,
        AppSettingsEntity::class,
        TagEntity::class,
        ConceptTagEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlashLearnDatabase : RoomDatabase() {
    abstract fun languageDao(): LanguageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun conceptDao(): ConceptDao
    abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "flashlearn.db"
    }
}
