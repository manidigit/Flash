package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

/**
 * انتخاب نوع مرور (بند 29 و 52) به‌همراه فیلتر Category (بند 30: ترکیب فیلترها).
 * onStart با (نوع مرور، Category انتخابی یا null) صدا زده می‌شود.
 */
@Composable
fun ReviewTypeSelectScreen(
    onStart: (String, Long?) -> Unit,
    viewModel: ReviewTypeSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val types = listOf(
        "RANDOM" to stringResource(R.string.review_type_random),
        "DAILY" to stringResource(R.string.review_type_daily),
        "WEEKLY" to stringResource(R.string.review_type_weekly),
        "MONTHLY" to stringResource(R.string.review_type_monthly),
        "EASY" to stringResource(R.string.review_type_easy),
        "MEDIUM" to stringResource(R.string.review_type_medium),
        "HARD" to stringResource(R.string.review_type_hard),
        "VERY_HARD" to stringResource(R.string.review_type_very_hard),
        "LEARNED" to stringResource(R.string.review_type_learned)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        if (state.categories.isNotEmpty()) {
            item {
                Text(stringResource(R.string.review_type_filter_by_category), style = MaterialTheme.typography.titleMedium)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text(stringResource(R.string.review_type_all_categories)) }
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
            }
            item { Text(stringResource(R.string.review_type_section_title), style = MaterialTheme.typography.titleMedium) }
        }

        items(types) { (type, label) ->
            Card(onClick = { onStart(type, state.selectedCategoryId) }) {
                Text(
                    text = label,
                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
