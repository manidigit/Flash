package com.app.flashlearn.di

import android.content.Context
import androidx.room.Room
import com.app.flashlearn.database.FlashLearnDatabase
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
import com.app.flashlearn.database.migration.ALL_MIGRATIONS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlashLearnDatabase =
        Room.databaseBuilder(context, FlashLearnDatabase::class.java, FlashLearnDatabase.DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            // fallbackToDestructiveMigration عمداً استفاده نشده تا داده کاربر در آپدیت‌ها حفظ شود.
            .build()

    @Provides
    fun provideConceptDao(db: FlashLearnDatabase): ConceptDao = db.conceptDao()

    @Provides
    fun provideContentDao(db: FlashLearnDatabase): ContentDao = db.contentDao()

    @Provides
    fun provideLearningStateDao(db: FlashLearnDatabase): LearningStateDao = db.learningStateDao()

    @Provides
    fun provideReviewHistoryDao(db: FlashLearnDatabase): ReviewHistoryDao = db.reviewHistoryDao()

    @Provides
    fun provideReviewSessionDao(db: FlashLearnDatabase): ReviewSessionDao = db.reviewSessionDao()

    @Provides
    fun provideLanguageDao(db: FlashLearnDatabase): LanguageDao = db.languageDao()

    @Provides
    fun provideLanguagePairDao(db: FlashLearnDatabase): LanguagePairDao = db.languagePairDao()

    @Provides
    fun provideCategoryDao(db: FlashLearnDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTagDao(db: FlashLearnDatabase): TagDao = db.tagDao()

    @Provides
    fun provideAppSettingsDao(db: FlashLearnDatabase): AppSettingsDao = db.appSettingsDao()
}
