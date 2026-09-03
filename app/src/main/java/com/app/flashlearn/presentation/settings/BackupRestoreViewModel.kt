package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.R
import com.app.flashlearn.core.util.UiText
import com.app.flashlearn.data.importexport.AutoBackupWriter
import com.app.flashlearn.domain.model.BackupMode
import com.app.flashlearn.domain.model.ConflictResolution
import com.app.flashlearn.domain.model.ImportPreview
import com.app.flashlearn.domain.model.ImportResult
import com.app.flashlearn.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupRestoreUiState(
    val isBusy: Boolean = false,
    val errorMessage: UiText? = null,
    val lastResult: ImportResult? = null,
    val preRestoreBackupPath: String? = null,
    val lastPreview: ImportPreview? = null
)

/** Backup/Restore واقعی (بند 44-50). */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val autoBackupWriter: AutoBackupWriter
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    private var pendingImportJson: String? = null

    fun export(mode: BackupMode, write: suspend (String) -> Unit) {
        if (_uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            try {
                val json = backupRepository.exportToJson(mode)
                write(json)
                _uiState.value = _uiState.value.copy(isBusy = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = UiText.of(R.string.backup_export_error, e.message ?: "")
                )
            }
        }
    }

    fun previewImport(json: String) {
        if (_uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            try {
                pendingImportJson = json
                val preview = backupRepository.previewImport(json)
                _uiState.value = _uiState.value.copy(isBusy = false, lastPreview = preview)
            } catch (e: Exception) {
                pendingImportJson = null
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = UiText.of(R.string.backup_invalid_file_error, e.message ?: "")
                )
            }
        }
    }

    fun cancelImportPreview() {
        pendingImportJson = null
        _uiState.value = _uiState.value.copy(lastPreview = null)
    }

    fun confirmImport(resolution: ConflictResolution) {
        val json = pendingImportJson ?: return
        if (_uiState.value.isBusy) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, lastPreview = null, errorMessage = null)
            try {
                val snapshotPath = try {
                    val currentSnapshot = backupRepository.exportToJson()
                    autoBackupWriter.writePreRestoreSnapshot(currentSnapshot)
                } catch (e: Exception) {
                    null
                }

                val result = backupRepository.applyImport(json, resolution)
                pendingImportJson = null
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    lastResult = result,
                    preRestoreBackupPath = snapshotPath
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBusy = false,
                    errorMessage = UiText.of(R.string.backup_apply_import_error, e.message ?: "")
                )
            }
        }
    }
}
