package com.app.flashlearn.domain.service

import com.app.flashlearn.domain.model.TranslationRequest
import com.app.flashlearn.domain.model.TranslationResult

interface AITranslationService {
    suspend fun translate(request: TranslationRequest): Result<TranslationResult>
    suspend fun isConfigured(): Boolean
}
