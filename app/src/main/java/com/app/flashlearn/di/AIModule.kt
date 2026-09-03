package com.app.flashlearn.di

import com.app.flashlearn.data.ai.AIProvider
import com.app.flashlearn.data.ai.AITranslationServiceImpl
import com.app.flashlearn.data.ai.OpenAICompatibleProvider
import com.app.flashlearn.domain.service.AITranslationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Singleton
    @Provides
    fun provideAIProvider(impl: OpenAICompatibleProvider): AIProvider = impl

    @Singleton
    @Provides
    fun provideAITranslationService(impl: AITranslationServiceImpl): AITranslationService = impl
}
