package com.app.flashlearn.di

import android.content.Context
import androidx.room.Room
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
    fun provideDatabase(@ApplicationContext context: Context): FlashLearnDatabase =
        Room.databaseBuilder(context, FlashLearnDatabase::class.java, FlashLearnDatabase.DATABASE_NAME)
            // .addMigrations(...) — Migration های واقعی وقتی ورژن دیتابیس عوض شد اینجا اضافه می‌شن
            .build()

    @Provides fun provideConceptDao(db: FlashLearnDatabase) = db.conceptDao()
    @Provides fun provideContentDao(db: FlashLearnDatabase) = db.contentDao()
    @Provides fun provideLearningStateDao(db: FlashLearnDatabase) = db.learningStateDao()
    @Provides fun provideReviewHistoryDao(db: FlashLearnDatabase) = db.reviewHistoryDao()
    @Provides fun provideReviewSessionDao(db: FlashLearnDatabase) = db.reviewSessionDao()
    @Provides fun provideCategoryDao(db: FlashLearnDatabase) = db.categoryDao()
    @Provides fun provideTagDao(db: FlashLearnDatabase) = db.tagDao()
    @Provides fun provideLanguagePairDao(db: FlashLearnDatabase) = db.languagePairDao()
    @Provides fun provideAppSettingsDao(db: FlashLearnDatabase) = db.appSettingsDao()
}
