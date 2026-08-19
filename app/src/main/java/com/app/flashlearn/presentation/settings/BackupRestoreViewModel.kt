package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.R
import com.app.flashlearn.core.util.UiText
import com.app.flashlearn.data.importexport.AutoBackupWriter
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

sealed interface BackupRestoreEvent {
    data class ExportReady(val json: String) : BackupRestoreEvent
    data class ImportPreviewReady(val preview: ImportPreview, val json: String) : BackupRestoreEvent
    data class ImportApplied(val result: ImportResult) : BackupRestoreEvent
    data class Error(val message: String) : BackupRestoreEvent
}

data class BackupRestoreUiState(
    val isBusy: Boolean = false,
    val lastPreview: ImportPreview? = null,
    val pendingImportJson: String? = null,
    val lastResult: ImportResult? = null,
    val errorMessage: UiText? = null,
    val preRestoreBackupPath: String? = null
)

/**
 * Export/Import (بند 44-50). قبل از هر Import واقعی، ابتدا previewImport (فقط-خواندنی) نشان داده
 * می‌شود؛ اعمال واقعی فقط بعد از تأیید کاربر روی نتیجه Preview انجام می‌شود (بند 46).
 * قبل از اعمال، یک Snapshot خودکار از وضعیت فعلی گرفته می‌شود (بند 50 — Backup Before Restore)؛
 * اگر گرفتن این Snapshot با خطا مواجه شود، Import اصلاً اعمال نمی‌شود (اولویت با ایمنی داده).
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val autoBackupWriter: AutoBackupWriter
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun export(onReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            try {
                val json = backupRepository.exportToJson()
                onReady(json)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = UiText.of(R.string.backup_export_error, e.message ?: ""))
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
            }
        }
    }

    fun previewImport(json: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            try {
                val preview = backupRepository.previewImport(json)
                _uiState.value = _uiState.value.copy(lastPreview = preview, pendingImportJson = json)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = UiText.of(R.string.backup_invalid_file_error, e.message ?: ""))
            } finally {
                _uiState.value = _uiState.value.copy(isBusy = false)
            }
        }
    }

    fun confirmImport(resolution: ConflictResolution) {
        val json = _uiState.value.pendingImportJson ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, errorMessage = null)
            try {
                // بند 50: قبل از هر Restore، یک نسخه پشتیبان خودکار از وضعیت فعلی گرفته می‌شود.
                val currentSnapshot = backupRepository.exportToJson()
                val backupPath = autoBackupWriter.writePreRestoreSnapshot(currentSnapshot)

                val result = backupRepository.applyImport(json, resolution)
                _uiState.value = BackupRestoreUiState(lastResult = result, preRestoreBackupPath = backupPath)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = UiText.of(R.string.backup_apply_import_error, e.message ?: ""), isBusy = false)
            }
        }
    }

    fun cancelImportPreview() {
        _uiState.value = _uiState.value.copy(lastPreview = null, pendingImportJson = null)
    }
}
