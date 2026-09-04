package com.app.flashlearn.core.di

import com.app.flashlearn.data.repository.ConceptRepositoryImpl
import com.app.flashlearn.data.repository.ReviewRepositoryImpl
import com.app.flashlearn.data.repository.ReviewSessionRepositoryImpl
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.ReviewRepository
import com.app.flashlearn.domain.repository.ReviewSessionRepository
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
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    @Binds
    @Singleton
    abstract fun bindReviewSessionRepository(impl: ReviewSessionRepositoryImpl): ReviewSessionRepository

    @Binds
    @Singleton
    abstract fun bindConceptRepository(impl: ConceptRepositoryImpl): ConceptRepository
}
