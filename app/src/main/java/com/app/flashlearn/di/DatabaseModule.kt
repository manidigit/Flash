package com.app.flashlearn.di

import android.content.Context
import androidx.room.Room
import com.app.flashlearn.data.repository.*
import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.database.dao.*
import com.app.flashlearn.domain.repository.*
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
    fun provideDatabase(@ApplicationContext context: Context): FlashLearnDatabase {
        return Room.databaseBuilder(
            context,
            FlashLearnDatabase::class.java,
            FlashLearnDatabase.DATABASE_NAME
        ).build()
    }

    @Provides fun provideConceptDao(db: FlashLearnDatabase): ConceptDao = db.conceptDao()
    @Provides fun provideContentDao(db: FlashLearnDatabase): ContentDao = db.contentDao()
    @Provides fun provideLearningStateDao(db: FlashLearnDatabase): LearningStateDao = db.learningStateDao()
    @Provides fun provideReviewHistoryDao(db: FlashLearnDatabase): ReviewHistoryDao = db.reviewHistoryDao()
    @Provides fun provideReviewSessionDao(db: FlashLearnDatabase): ReviewSessionDao = db.reviewSessionDao()
    @Provides fun provideCategoryDao(db: FlashLearnDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideLanguageDao(db: FlashLearnDatabase): LanguageDao = db.languageDao()
    @Provides fun provideTagDao(db: FlashLearnDatabase): TagDao = db.tagDao()
    @Provides fun provideAppSettingsDao(db: FlashLearnDatabase): AppSettingsDao = db.appSettingsDao()

    @Singleton @Provides fun provideConceptRepository(impl: ConceptRepositoryImpl): ConceptRepository = impl
    @Singleton @Provides fun provideLearningStateRepository(impl: LearningStateRepositoryImpl): LearningStateRepository = impl
    @Singleton @Provides fun provideReviewHistoryRepository(impl: ReviewHistoryRepositoryImpl): ReviewHistoryRepository = impl
    @Singleton @Provides fun provideReviewSessionRepository(impl: ReviewSessionRepositoryImpl): ReviewSessionRepository = impl
    @Singleton @Provides fun provideCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository = impl
    @Singleton @Provides fun provideTagRepository(impl: TagRepositoryImpl): TagRepository = impl
    @Singleton @Provides fun provideLanguageRepository(impl: LanguageRepositoryImpl): LanguageRepository = impl
    @Singleton @Provides fun provideLanguagePairRepository(impl: LanguagePairRepositoryImpl): LanguagePairRepository = impl
    @Singleton @Provides fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Singleton
    @Provides
    fun provideBackupRepository(impl: com.app.flashlearn.data.importexport.JsonBackupServiceImpl): BackupRepository = impl
}
