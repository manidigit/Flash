package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.data.security.SecureKeyValueStore
import com.app.flashlearn.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SECURE_KEY_AI_API_KEY = "ai_api_key"

data class AISettingsUiState(
    val endpoint: String = "",
    val apiKey: String = "",
    val model: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

/**
 * تنظیمات AI (بند 76): Endpoint/Model در دیتابیس معمولی، API Key در SecureKeyValueStore
 * (رمزنگاری‌شده با Android Keystore، نه متن ساده) — هیچ‌کدام در Source Code Hard-code نشده‌اند.
 */
@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val secureKeyValueStore: SecureKeyValueStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AISettingsUiState(
                endpoint = settingsRepository.getValue(SettingsRepository.AI_ENDPOINT) ?: "",
                apiKey = secureKeyValueStore.get(SECURE_KEY_AI_API_KEY) ?: "",
                model = settingsRepository.getValue(SettingsRepository.AI_MODEL) ?: ""
            )
        }
    }

    fun onEndpointChanged(value: String) {
        _uiState.value = _uiState.value.copy(endpoint = value, saved = false)
    }

    fun onApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, saved = false)
    }

    fun onModelChanged(value: String) {
        _uiState.value = _uiState.value.copy(model = value, saved = false)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            settingsRepository.setValue(SettingsRepository.AI_ENDPOINT, state.endpoint.trim())
            settingsRepository.setValue(SettingsRepository.AI_MODEL, state.model.trim())
            secureKeyValueStore.set(SECURE_KEY_AI_API_KEY, state.apiKey.trim())
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
