package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.data.security.SecureKeyValueStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_ENDPOINT = "ai_endpoint"
private const val KEY_MODEL = "ai_model"
private const val KEY_API_KEY = "ai_api_key"

data class AISettingsUiState(
    val endpoint: String = "",
    val model: String = "",
    val apiKey: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

/** تنظیمات AI (بند 76): تمام مقادیر در SecureKeyValueStore رمزنگاری‌شده ذخیره می‌شوند. */
@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val secureKeyValueStore: SecureKeyValueStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = AISettingsUiState(
                endpoint = secureKeyValueStore.get(KEY_ENDPOINT) ?: "",
                model = secureKeyValueStore.get(KEY_MODEL) ?: "",
                apiKey = secureKeyValueStore.get(KEY_API_KEY) ?: ""
            )
        }
    }

    fun onEndpointChanged(value: String) {
        _uiState.value = _uiState.value.copy(endpoint = value, saved = false)
    }

    fun onModelChanged(value: String) {
        _uiState.value = _uiState.value.copy(model = value, saved = false)
    }

    fun onApiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value, saved = false)
    }

    fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            secureKeyValueStore.set(KEY_ENDPOINT, state.endpoint.trim())
            secureKeyValueStore.set(KEY_MODEL, state.model.trim())
            secureKeyValueStore.set(KEY_API_KEY, state.apiKey.trim())
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
