package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsUiState())
    val uiState: StateFlow<AISettingsUiState> = _uiState
}

data class AISettingsUiState(
    val aiEnabled: Boolean = false,
    val apiKeyConfigured: Boolean = false
)
