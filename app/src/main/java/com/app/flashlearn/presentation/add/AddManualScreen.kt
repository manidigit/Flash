package com.app.flashlearn.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

/**
 * افزودن دستی کلمه (بند 40). جریان کوتاه طبق بند 79، به‌همراه انتخاب/ساخت Category و Tags.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (بند 83).
 */
@Composable
fun AddManualScreen(
    onSaved: () -> Unit,
    viewModel: AddManualViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var newCategoryText by remember { mutableStateOf("") }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = stringResource(R.string.add_manual_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.sourceText,
            onValueChange = viewModel::onSourceTextChanged,
            label = { Text(stringResource(R.string.add_manual_source_text_label, state.sourceLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.targetText,
            onValueChange = viewModel::onTargetTextChanged,
            label = { Text(stringResource(R.string.add_manual_target_text_label, state.targetLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.pronunciation,
            onValueChange = viewModel::onPronunciationChanged,
            label = { Text(stringResource(R.string.add_manual_pronunciation_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.example,
            onValueChange = viewModel::onExampleChanged,
            label = { Text(stringResource(R.string.add_manual_example_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChanged,
            label = { Text(stringResource(R.string.add_manual_notes_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.tagsText,
            onValueChange = viewModel::onTagsTextChanged,
            label = { Text(stringResource(R.string.add_manual_tags_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(stringResource(R.string.add_manual_category_section), style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            items(state.categories) { category ->
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
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
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

        Button(
            onClick = viewModel::save,
            enabled = state.canSave && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (state.isSaving) R.string.add_manual_saving else R.string.action_save))
        }
    }
}
