package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.repository.LanguagePairRepository
import javax.inject.Inject

class SwitchLanguagePairUseCase @Inject constructor(
    private val languagePairRepository: LanguagePairRepository
) {
    suspend operator fun invoke(pairId: Long) {
        languagePairRepository.setActivePair(pairId)
    }
}
