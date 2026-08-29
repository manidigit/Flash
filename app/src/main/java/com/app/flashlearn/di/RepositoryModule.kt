package com.app.flashlearn.di

import com.app.flashlearn.data.importexport.JsonBackupServiceImpl
import com.app.flashlearn.data.repository.CategoryRepositoryImpl
import com.app.flashlearn.data.repository.ConceptRepositoryImpl
import com.app.flashlearn.data.repository.LanguagePairRepositoryImpl
import com.app.flashlearn.data.repository.LanguageRepositoryImpl
import com.app.flashlearn.data.repository.LearningStateRepositoryImpl
import com.app.flashlearn.data.repository.ReviewHistoryRepositoryImpl
import com.app.flashlearn.data.repository.ReviewSessionRepositoryImpl
import com.app.flashlearn.data.repository.SettingsRepositoryImpl
import com.app.flashlearn.data.repository.TagRepositoryImpl
import com.app.flashlearn.domain.repository.BackupRepository
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LanguageRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewHistoryRepository
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import com.app.flashlearn.domain.repository.SettingsRepository
import com.app.flashlearn.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConceptRepository(impl: ConceptRepositoryImpl): ConceptRepository

    @Binds
    @Singleton
    abstract fun bindLearningStateRepository(impl: LearningStateRepositoryImpl): LearningStateRepository

    @Binds
    @Singleton
    abstract fun bindReviewHistoryRepository(impl: ReviewHistoryRepositoryImpl): ReviewHistoryRepository

    @Binds
    @Singleton
    abstract fun bindReviewSessionRepository(impl: ReviewSessionRepositoryImpl): ReviewSessionRepository

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(impl: LanguageRepositoryImpl): LanguageRepository

    @Binds
    @Singleton
    abstract fun bindLanguagePairRepository(impl: LanguagePairRepositoryImpl): LanguagePairRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: JsonBackupServiceImpl): BackupRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
}
