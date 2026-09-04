package com.app.flashlearn.core.di

import com.app.flashlearn.domain.ReviewTransitionEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideReviewTransitionEngine(): ReviewTransitionEngine = ReviewTransitionEngine()
}
