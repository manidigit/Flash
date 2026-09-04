package com.app.flashlearn.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.repository.LanguagePairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _selectedSource = MutableStateFlow<String?>(null)
    val selectedSource: StateFlow<String?> = _selectedSource.asStateFlow()

    private val _selectedTarget = MutableStateFlow<String?>(null)
    val selectedTarget: StateFlow<String?> = _selectedTarget.asStateFlow()

    fun selectSource(code: String) { _selectedSource.value = code }
    fun selectTarget(code: String) { _selectedTarget.value = code }

    suspend fun finish(): Boolean {
        val source = _selectedSource.value ?: return false
        val target = _selectedTarget.value ?: return false
        val id = languagePairRepository.addPair(LanguagePair(sourceLanguage = source, targetLanguage = target, isActive = true))
        languagePairRepository.setActivePair(id)
        return true
    }
}
