package com.app.flashlearn.core.di

import android.content.Context
import com.app.flashlearn.database.FlashLearnDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): FlashLearnDatabase {
        return FlashLearnDatabase.getInstance(context)
    }

    @Provides
    fun provideConceptDao(database: FlashLearnDatabase) = database.conceptDao()
    @Provides
    fun provideContentDao(database: FlashLearnDatabase) = database.contentDao()
    @Provides
    fun provideLearningStateDao(database: FlashLearnDatabase) = database.learningStateDao()
    @Provides
    fun provideReviewSessionDao(database: FlashLearnDatabase) = database.reviewSessionDao()
    @Provides
    fun provideReviewHistoryDao(database: FlashLearnDatabase) = database.reviewHistoryDao()
    @Provides
    fun provideCategoryDao(database: FlashLearnDatabase) = database.categoryDao()
    @Provides
    fun provideTagDao(database: FlashLearnDatabase) = database.tagDao()
    @Provides
    fun provideConceptTagDao(database: FlashLearnDatabase) = database.conceptTagDao()
    @Provides
    fun provideLanguageDao(database: FlashLearnDatabase) = database.languageDao()
    @Provides
    fun provideLanguagePairDao(database: FlashLearnDatabase) = database.languagePairDao()
    @Provides
    fun provideAppSettingDao(database: FlashLearnDatabase) = database.appSettingDao()
}
