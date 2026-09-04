package com.app.flashlearn.di

import com.app.flashlearn.data.backup.JsonBackupServiceImpl
import com.app.flashlearn.domain.service.BackupService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds @Singleton
    abstract fun bindBackupService(impl: JsonBackupServiceImpl): BackupService
}
