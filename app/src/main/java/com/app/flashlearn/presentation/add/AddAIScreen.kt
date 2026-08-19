package com.app.flashlearn.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.core.util.asString
import com.app.flashlearn.ui.theme.Spacing

/**
 * افزودن با AI (بند 41): متن مبدا -> ترجمه با AI -> Preview قابل ویرایش -> Approve/Cancel.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (بند 83).
 */
@Composable
fun AddAIScreen(
    onSaved: () -> Unit,
    viewModel: AddAIViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(stringResource(R.string.add_ai_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = state.sourceText,
            onValueChange = viewModel::onSourceTextChanged,
            label = { Text(stringResource(R.string.add_ai_source_text_label, state.sourceLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = viewModel::translate,
            enabled = state.sourceText.isNotBlank() && !state.isTranslating,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (state.isTranslating) R.string.add_ai_translating else R.string.add_ai_translate_button))
        }

        state.errorMessage?.let {
            Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        if (state.suggestion != null) {
            Text(stringResource(R.string.add_ai_preview_title), style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.editedTranslation,
                onValueChange = viewModel::onEditedTranslationChanged,
                label = { Text(stringResource(R.string.add_ai_translation_label, state.targetLanguage)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.editedPronunciation,
                onValueChange = viewModel::onEditedPronunciationChanged,
                label = { Text(stringResource(R.string.add_ai_pronunciation_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.editedExample,
                onValueChange = viewModel::onEditedExampleChanged,
                label = { Text(stringResource(R.string.add_ai_example_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            state.suggestion?.definition?.let {
                Text(stringResource(R.string.add_ai_definition_prefix, it), style = MaterialTheme.typography.bodyMedium)
            }
            state.suggestion?.partOfSpeech?.let {
                Text(stringResource(R.string.add_ai_part_of_speech_prefix, it), style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = viewModel::cancelSuggestion) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = viewModel::approve,
                    enabled = state.editedTranslation.isNotBlank() && !state.isSaving
                ) {
                    Text(stringResource(if (state.isSaving) R.string.add_ai_saving else R.string.action_confirm_and_save))
                }
            }
        }
    }
}
