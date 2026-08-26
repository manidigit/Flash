package com.app.flashlearn.presentation.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.ui.theme.Spacing

/**
 * صفحه «کلمات تکراری» (درخواست کاربر): کلماتی که متن مبدأشان (در زبان مبدأ فعلی) دقیقاً
 * یکسان است را گروه‌بندی نشان می‌دهد؛ کاربر می‌تواند هرکدام را برای حذف علامت بزند
 * (پیش‌فرض: نگه‌داشتن قدیمی‌ترین/اولین نسخه، علامت‌زدن بقیه) و همه را یک‌جا حذف کند.
 */
@Composable
fun DuplicateWordsScreen(
    onBack: () -> Unit,
    viewModel: DuplicateWordsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text(
            text = stringResource(R.string.duplicate_words_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.duplicate_words_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm)
        )

        state.deletedCount?.let {
            Text(
                text = stringResource(R.string.duplicate_words_deleted_summary, it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )
        }

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.groups.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.duplicate_words_none_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    itemsIndexed(state.groups) { index, group ->
                        DuplicateGroupCard(
                            group = group,
                            targetLanguage = state.targetLanguage,
                            onToggle = { conceptId -> viewModel.toggleMarked(index, conceptId) }
                        )
                    }
                }

                Button(
                    onClick = viewModel::deleteMarked,
                    enabled = state.totalMarkedCount > 0 && !state.isDeleting,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
                ) {
                    Text(
                        if (state.isDeleting) {
                            stringResource(R.string.duplicate_words_deleting)
                        } else {
                            stringResource(R.string.duplicate_words_delete_marked, state.totalMarkedCount)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(
    group: DuplicateGroupUi,
    targetLanguage: String,
    onToggle: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            group.concepts.forEachIndexed { i, concept ->
                DuplicateConceptRow(
                    concept = concept,
                    targetLanguage = targetLanguage,
                    isMarked = concept.id in group.markedForDeletion,
                    isOriginal = i == 0,
                    onToggle = { onToggle(concept.id) }
                )
            }
        }
    }
}

@Composable
private fun DuplicateConceptRow(
    concept: Concept,
    targetLanguage: String,
    isMarked: Boolean,
    isOriginal: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isMarked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = concept.contentsFor(targetLanguage).joinToString("، ") { it.text }.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyLarge
                )
                if (isOriginal) {
                    Text(
                        text = stringResource(R.string.duplicate_words_original_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                }
            }
        }
    }
}
