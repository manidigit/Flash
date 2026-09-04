package com.app.flashlearn.domain.service

import com.app.flashlearn.domain.model.ImportSummary

interface BackupService {
    suspend fun exportWordsOnly(): String
    suspend fun exportSettingsAndProgress(): String
    suspend fun importWordsOnly(json: String): Result<ImportSummary>
    suspend fun importSettingsAndProgress(json: String): Result<ImportSummary>
}
