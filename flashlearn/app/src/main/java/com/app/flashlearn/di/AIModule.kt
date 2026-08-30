package com.app.flashlearn.di

import com.app.flashlearn.data.ai.AIProvider
import com.app.flashlearn.data.ai.AITranslationServiceImpl
import com.app.flashlearn.data.ai.OpenAICompatibleProvider
import com.app.flashlearn.domain.service.AITranslationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    @Binds
    @Singleton
    abstract fun bindAIProvider(impl: OpenAICompatibleProvider): AIProvider

    @Binds
    @Singleton
    abstract fun bindAITranslationService(impl: AITranslationServiceImpl): AITranslationService
}
