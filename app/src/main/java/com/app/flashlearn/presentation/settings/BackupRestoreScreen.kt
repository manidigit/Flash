package com.app.flashlearn.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.core.util.asString
import com.app.flashlearn.domain.model.ConflictResolution
import com.app.flashlearn.domain.model.BackupMode
import com.app.flashlearn.ui.theme.Spacing
import java.io.OutputStreamWriter

/**
 * Backup/Restore واقعی با انتخاب فایل از طریق Storage Access Framework (بند 44-50):
 * Export مستقیماً یک فایل JSON در محلی که کاربر انتخاب می‌کند می‌سازد؛ Import ابتدا
 * Preview نشان می‌دهد و فقط بعد از تأیید صریح کاربر اعمال می‌شود.
 */
@Composable
fun BackupRestoreScreen(
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var pendingBackupMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(BackupMode.FULL) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.export(pendingBackupMode) { json ->
            context.contentResolver.openOutputStream(uri)?.use { stream -> OutputStreamWriter(stream).use { it.write(json) } }
        }
    }
    fun launchBackup(mode: BackupMode, filename: String) {
        pendingBackupMode = mode
        exportLauncher.launch(filename)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) viewModel.previewImport(text)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(stringResource(R.string.backup_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text("بکاپ را می‌توانی جداگانه ذخیره کنی:")
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = Spacing.sm)) {
                    Button(onClick = { launchBackup(BackupMode.VOCABULARY, "flashlearn-vocabulary-backup.json") }, enabled = !state.isBusy) { Text("واژگان") }
                    Button(onClick = { launchBackup(BackupMode.LEARNING_PROGRESS, "flashlearn-progress-backup.json") }, enabled = !state.isBusy) { Text("پیشرفت و تنظیمات") }
                    Button(onClick = { launchBackup(BackupMode.FULL, "flashlearn-full-backup.json") }, enabled = !state.isBusy) { Text("کامل") }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(stringResource(R.string.backup_import_description))
                Button(
                    onClick = { importLauncher.launch("application/json") },
                    modifier = Modifier.padding(top = Spacing.sm),
                    enabled = !state.isBusy
                ) {
                    Text(stringResource(R.string.backup_import_pick_file_button))
                }
            }
        }

        if (state.isBusy) {
            CircularProgressIndicator()
        }

        state.errorMessage?.let {
            Text(it.asString(), color = MaterialTheme.colorScheme.error)
        }

        state.lastResult?.let { result ->
            Text(
                stringResource(R.string.backup_result_summary, result.inserted, result.updated, result.skipped),
                style = MaterialTheme.typography.bodyMedium
            )
            state.preRestoreBackupPath?.let {
                Text(
                    stringResource(R.string.backup_auto_snapshot_saved),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    val preview = state.lastPreview
    if (preview != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelImportPreview,
            title = { Text(stringResource(R.string.backup_preview_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.backup_preview_new_count, preview.newCount))
                    Text(stringResource(R.string.backup_preview_identical_count, preview.identicalExistingCount))
                    Text(stringResource(R.string.backup_preview_conflict_count, preview.conflictCount))
                    if (preview.conflictCount > 0) {
                        Text(
                            stringResource(R.string.backup_preview_conflict_prompt),
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                    Text(
                        stringResource(R.string.backup_preview_auto_snapshot_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    if (preview.conflictCount > 0) {
                        OutlinedButton(onClick = { viewModel.confirmImport(ConflictResolution.KEEP_EXISTING) }) {
                            Text(stringResource(R.string.backup_resolution_keep_existing))
                        }
                        OutlinedButton(onClick = { viewModel.confirmImport(ConflictResolution.MERGE) }) {
                            Text(stringResource(R.string.backup_resolution_merge))
                        }
                        Button(onClick = { viewModel.confirmImport(ConflictResolution.USE_IMPORTED) }) {
                            Text(stringResource(R.string.backup_resolution_use_imported))
                        }
                    } else {
                        Button(onClick = { viewModel.confirmImport(ConflictResolution.SKIP) }) {
                            Text(stringResource(R.string.action_confirm))
                        }
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::cancelImportPreview) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }
}
