package com.app.flashlearn.presentation.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.core.util.asString
import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import com.app.flashlearn.ui.theme.Spacing

/**
 * Import File - CSV/JSON (بند 43): انتخاب فایل با Storage Access Framework، Parse خودکار
 * بر اساس پسوند، و همان الگوی Preview Table قابل ویرایش قبل از Import نهایی.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (بند 83).
 *
 * درخواست کاربر: انتخاب یک دسته‌بندی (یا ساخت دسته‌بندی جدید) قبل از Import، دقیقاً مثل
 * «جای‌گذاری متن».
 */
@Composable
fun AddImportFileScreen(
    onImported: () -> Unit,
    viewModel: AddImportFileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val defaultFileLabel = stringResource(R.string.add_import_file_default_name)
    var newCategoryText by remember { mutableStateOf("") }

    LaunchedEffect(state.importedCount) {
        if (state.importedCount != null && state.duplicateCount == 0 && state.translationsAddedCount == 0) onImported()
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = queryFileName(uri, context) ?: defaultFileLabel
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (text != null) viewModel.onFilePicked(name, text)
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text(stringResource(R.string.add_import_file_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.add_import_file_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm)
        )

        Button(
            onClick = {
                pickFileLauncher.launch(arrayOf("text/csv", "application/json", "text/*", "*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_import_file_pick_button))
        }

        state.fileName?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = Spacing.xs))
        }

        Text(
            stringResource(R.string.add_paste_category_section),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            item {
                FilterChip(
                    selected = state.selectedCategoryId == null,
                    onClick = { viewModel.onCategorySelected(null) },
                    label = { Text(stringResource(R.string.add_paste_no_category)) }
                )
            }
            items(state.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = state.selectedCategoryId == category.id,
                    onClick = {
                        viewModel.onCategorySelected(
                            if (state.selectedCategoryId == category.id) null else category.id
                        )
                    },
                    label = { Text(category.name) }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.xs)
        ) {
            OutlinedTextField(
                value = newCategoryText,
                onValueChange = { newCategoryText = it },
                label = { Text(stringResource(R.string.add_manual_new_category_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = { viewModel.createCategory(newCategoryText); newCategoryText = "" },
                enabled = newCategoryText.isNotBlank()
            ) {
                Text(stringResource(R.string.action_add))
            }
        }

        state.errorMessage?.let {
            Text(it.asString(), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = Spacing.xs))
        }

        if (state.importedCount != null && (state.duplicateCount > 0 || state.translationsAddedCount > 0)) {
            Text(
                stringResource(
                    R.string.add_import_result_full,
                    state.importedCount ?: 0,
                    state.translationsAddedCount,
                    state.duplicateCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Button(onClick = onImported, modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
                Text(stringResource(R.string.action_confirm))
            }
        }

        if (state.entries.isNotEmpty()) {
            Text(
                stringResource(R.string.add_import_file_preview_count, state.includedCount, state.entries.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.sm)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(state.entries, key = { it.localId }) { entry ->
                    ImportPreviewRow(
                        entry = entry,
                        onToggle = { viewModel.toggleIncluded(entry.localId) },
                        onRemove = { viewModel.removeEntry(entry.localId) }
                    )
                }
            }

            Button(
                onClick = viewModel::importAll,
                enabled = state.includedCount > 0 && !state.isImporting,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
            ) {
                Text(
                    if (state.isImporting) stringResource(R.string.add_import_file_importing)
                    else stringResource(R.string.add_import_file_import_all_button, state.includedCount)
                )
            }
        }
    }
}

@Composable
private fun ImportPreviewRow(entry: ParsedVocabularyEntry, onToggle: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (entry.scriptMismatch) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            if (entry.scriptMismatch) {
                Text(
                    text = stringResource(R.string.add_paste_script_mismatch_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = entry.included, onCheckedChange = { onToggle() })
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.sourceText, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        entry.targetText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}

private fun queryFileName(uri: Uri, context: android.content.Context): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name ?: uri.lastPathSegment
}
