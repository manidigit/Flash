package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.TranslationRequest
import com.app.flashlearn.domain.model.TranslationResult
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.service.AITranslationService
import javax.inject.Inject

class TranslateWithAIUseCase @Inject constructor(
    private val aiTranslationService: AITranslationService,
    private val languagePairRepository: LanguagePairRepository
) {
    suspend operator fun invoke(sourceText: String, contentType: ContentType): Result<TranslationResult> {
        val pair = languagePairRepository.getActivePair()
            ?: return Result.failure(IllegalStateException("زبان مبدا/مقصد تنظیم نشده"))
        return aiTranslationService.translate(
            TranslationRequest(sourceText, pair.sourceLanguage, pair.targetLanguage, contentType)
        )
    }
}
