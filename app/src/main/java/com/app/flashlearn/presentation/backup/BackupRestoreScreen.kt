package com.app.flashlearn.presentation.backup

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.flashlearn.ui.theme.SuccessLight
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(viewModel: BackupRestoreViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingExportType by remember { mutableStateOf<String?>(null) }
    var pendingImportType by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val jsonContent = when (pendingExportType) {
                "words" -> viewModel.getWordsBackupJson()
                "settings" -> viewModel.getSettingsBackupJson()
                else -> return@launch
            }
            context.contentResolver.openOutputStream(uri)?.use { it.write(jsonContent.toByteArray()) }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            ?: return@rememberLauncherForActivityResult
        when (pendingImportType) {
            "words" -> viewModel.importWords(content)
            "settings" -> viewModel.importSettings(content)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { pendingExportType = "words"; createDocumentLauncher.launch("flashlearn_words_backup.json") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("پشتیبان‌گیری از کلمات") }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { pendingExportType = "settings"; createDocumentLauncher.launch("flashlearn_settings_backup.json") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("پشتیبان‌گیری از تنظیمات و پیشرفت") }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { pendingImportType = "words"; openDocumentLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("بازیابی کلمات از فایل") }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { pendingImportType = "settings"; openDocumentLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("بازیابی تنظیمات/پیشرفت از فایل") }

        if (state.isProcessing) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        state.lastResultMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = if (state.isError) MaterialTheme.colorScheme.error else SuccessLight)
        }
    }
}
