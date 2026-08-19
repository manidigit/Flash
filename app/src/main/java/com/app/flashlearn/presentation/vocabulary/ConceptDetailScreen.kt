package com.app.flashlearn.presentation.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

/** ویرایش/حذف/Favorite یک کلمه موجود (بند 38). */
@Composable
fun ConceptDetailScreen(
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ConceptDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }
    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.concept == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.concept_detail_not_found), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.concept_detail_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = viewModel::toggleFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(R.string.concept_detail_favorite),
                    tint = if (state.favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    }
                )
            }
        }

        OutlinedTextField(
            value = state.sourceText,
            onValueChange = viewModel::onSourceTextChanged,
            label = { Text(stringResource(R.string.concept_detail_source_text_label, state.sourceLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.targetText,
            onValueChange = viewModel::onTargetTextChanged,
            label = { Text(stringResource(R.string.concept_detail_target_text_label, state.targetLanguage)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.pronunciation,
            onValueChange = viewModel::onPronunciationChanged,
            label = { Text(stringResource(R.string.concept_detail_pronunciation_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.example,
            onValueChange = viewModel::onExampleChanged,
            label = { Text(stringResource(R.string.concept_detail_example_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChanged,
            label = { Text(stringResource(R.string.concept_detail_notes_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.tagsText,
            onValueChange = viewModel::onTagsTextChanged,
            label = { Text(stringResource(R.string.concept_detail_tags_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(stringResource(R.string.concept_detail_category_section), style = MaterialTheme.typography.titleMedium)
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
                label = { Text(stringResource(R.string.concept_detail_new_category_label)) },
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

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            OutlinedButton(onClick = { showDeleteConfirm = true }) {
                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = viewModel::save,
                enabled = state.sourceText.isNotBlank() && state.targetText.isNotBlank() && !state.isSaving
            ) {
                Text(stringResource(if (state.isSaving) R.string.concept_detail_saving else R.string.concept_detail_save_changes))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.concept_detail_delete_confirm_title)) },
            text = { Text(stringResource(R.string.concept_detail_delete_confirm_body)) },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; viewModel.delete() }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }
}
