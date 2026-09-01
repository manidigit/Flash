package com.app.flashlearn.di

import android.content.Context
import androidx.room.Room
import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.data.repository.*
import com.app.flashlearn.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FlashLearnDatabase {
        return Room.databaseBuilder(
            context,
            FlashLearnDatabase::class.java,
            FlashLearnDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideConceptDao(database: FlashLearnDatabase): ConceptDao = database.conceptDao()
    @Provides
    fun provideContentDao(database: FlashLearnDatabase): ContentDao = database.contentDao()
    @Provides
    fun provideLearningStateDao(database: FlashLearnDatabase): LearningStateDao = database.learningStateDao()
    @Provides
    fun provideReviewHistoryDao(database: FlashLearnDatabase): ReviewHistoryDao = database.reviewHistoryDao()
    @Provides
    fun provideReviewSessionDao(database: FlashLearnDatabase): ReviewSessionDao = database.reviewSessionDao()
    @Provides
    fun provideCategoryDao(database: FlashLearnDatabase): CategoryDao = database.categoryDao()
    @Provides
    fun provideLanguageDao(database: FlashLearnDatabase): LanguageDao = database.languageDao()
    @Provides
    fun provideAppSettingsDao(database: FlashLearnDatabase): AppSettingsDao = database.appSettingsDao()

    @Singleton
    @Provides
    fun provideConceptRepository(impl: ConceptRepositoryImpl): ConceptRepository = impl

    @Singleton
    @Provides
    fun provideAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository = impl

    @Singleton
    @Provides
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Singleton
    @Provides
    fun provideLanguageRepository(impl: LanguageRepositoryImpl): LanguageRepository = impl

    @Singleton
    @Provides
    fun provideLanguagePairRepository(impl: LanguagePairRepositoryImpl): LanguagePairRepository = impl
}
