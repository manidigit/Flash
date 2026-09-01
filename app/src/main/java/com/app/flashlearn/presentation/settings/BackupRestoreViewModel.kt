package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState
}

data class BackupRestoreUiState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false
)
