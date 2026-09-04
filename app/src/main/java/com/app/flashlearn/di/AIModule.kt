package com.app.flashlearn.di

import com.app.flashlearn.data.ai.AITranslationServiceImpl
import com.app.flashlearn.data.ai.SecureKeyValueStoreImpl
import com.app.flashlearn.domain.service.AITranslationService
import com.app.flashlearn.domain.service.SecureKeyValueStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {
    @Binds @Singleton
    abstract fun bindSecureKeyValueStore(impl: SecureKeyValueStoreImpl): SecureKeyValueStore

    @Binds @Singleton
    abstract fun bindAITranslationService(impl: AITranslationServiceImpl): AITranslationService
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
