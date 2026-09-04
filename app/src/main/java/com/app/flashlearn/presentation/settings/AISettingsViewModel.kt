package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.service.AITranslationService
import com.app.flashlearn.domain.service.SecureKeyValueStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AISettingsUiState(
    val hasApiKey: Boolean = false,
    val apiKeyInput: String = "",
    val isSaving: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val secureKeyValueStore: SecureKeyValueStore,
    private val aiTranslationService: AITranslationService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(hasApiKey = aiTranslationService.isConfigured())
        }
    }

    fun onApiKeyChanged(key: String) { _uiState.value = _uiState.value.copy(apiKeyInput = key) }

    fun saveApiKey() {
        val key = _uiState.value.apiKeyInput
        if (key.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            secureKeyValueStore.saveApiKey("openai_compatible", key)
            _uiState.value = _uiState.value.copy(isSaving = false, hasApiKey = true, apiKeyInput = "", message = "ذخیره شد")
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            secureKeyValueStore.clearApiKey("openai_compatible")
            _uiState.value = _uiState.value.copy(hasApiKey = false, message = "کلید حذف شد")
        }
    }
}
