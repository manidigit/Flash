package com.app.flashlearn.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.service.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupRestoreUiState(
    val isProcessing: Boolean = false,
    val lastResultMessage: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupService: BackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    suspend fun getWordsBackupJson(): String = backupService.exportWordsOnly()
    suspend fun getSettingsBackupJson(): String = backupService.exportSettingsAndProgress()

    fun importWords(jsonContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            backupService.importWordsOnly(jsonContent)
                .onSuccess { summary ->
                    _uiState.value = BackupRestoreUiState(
                        isProcessing = false,
                        lastResultMessage = "وارد شد: ${summary.importedCount} — رد شد (تکراری): ${summary.skippedCount} — خطا: ${summary.failedCount}",
                        isError = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = BackupRestoreUiState(isProcessing = false, lastResultMessage = "خطا: ${e.message}", isError = true)
                }
        }
    }

    fun importSettings(jsonContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            backupService.importSettingsAndProgress(jsonContent)
                .onSuccess { summary ->
                    _uiState.value = BackupRestoreUiState(
                        isProcessing = false,
                        lastResultMessage = "وارد شد: ${summary.importedCount} — رد شد: ${summary.skippedCount} — خطا: ${summary.failedCount}",
                        isError = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = BackupRestoreUiState(isProcessing = false, lastResultMessage = "خطا: ${e.message}", isError = true)
                }
        }
    }
}
