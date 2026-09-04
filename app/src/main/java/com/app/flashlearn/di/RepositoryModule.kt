package com.app.flashlearn.di

import com.app.flashlearn.data.repository.AppSettingsRepositoryImpl
import com.app.flashlearn.data.repository.CategoryRepositoryImpl
import com.app.flashlearn.data.repository.ConceptRepositoryImpl
import com.app.flashlearn.data.repository.LanguagePairRepositoryImpl
import com.app.flashlearn.data.repository.LearningStateRepositoryImpl
import com.app.flashlearn.data.repository.ReviewRepositoryImpl
import com.app.flashlearn.data.repository.StatisticsRepositoryImpl
import com.app.flashlearn.data.repository.TagRepositoryImpl
import com.app.flashlearn.domain.repository.AppSettingsRepository
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewRepository
import com.app.flashlearn.domain.repository.StatisticsRepository
import com.app.flashlearn.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindConceptRepository(impl: ConceptRepositoryImpl): ConceptRepository

    @Binds @Singleton
    abstract fun bindLearningStateRepository(impl: LearningStateRepositoryImpl): LearningStateRepository

    @Binds @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds @Singleton
    abstract fun bindLanguagePairRepository(impl: LanguagePairRepositoryImpl): LanguagePairRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds @Singleton
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds @Singleton
    abstract fun bindStatisticsRepository(impl: StatisticsRepositoryImpl): StatisticsRepository
}
