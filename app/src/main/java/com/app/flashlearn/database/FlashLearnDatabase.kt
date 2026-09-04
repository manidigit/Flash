package com.app.flashlearn.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.database.entity.*

@Database(
    entities = [
        LanguageEntity::class,
        LanguagePairEntity::class,
        CategoryEntity::class,
        TagEntity::class,
        ConceptEntity::class,
        ContentEntity::class,
        LearningStateEntity::class,
        ReviewSessionEntity::class,
        ReviewHistoryEntity::class,
        ConceptTagEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FlashLearnDatabase : RoomDatabase() {

    abstract fun conceptDao(): ConceptDao
    abstract fun contentDao(): ContentDao
    abstract fun learningStateDao(): LearningStateDao
    abstract fun reviewSessionDao(): ReviewSessionDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun conceptTagDao(): ConceptTagDao
    abstract fun languageDao(): LanguageDao
    abstract fun languagePairDao(): LanguagePairDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: FlashLearnDatabase? = null

        fun getInstance(context: Context): FlashLearnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlashLearnDatabase::class.java,
                    "flashlearn.db"
                )
                    .fallbackToDestructiveMigration() // فقط برای نمونه؛ در تولید باید Migration بنویسید
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
