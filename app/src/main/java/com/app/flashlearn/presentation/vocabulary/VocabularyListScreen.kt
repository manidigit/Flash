package com.app.flashlearn.presentation.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.VocabularySortOrder
import com.app.flashlearn.ui.theme.Spacing

/**
 * لیست کامل واژگان با جستجوی واقعی (بند 38-39). ضربه روی یک ردیف به صفحه ویرایش/حذف
 * می‌رود؛ ستاره برای Favorite مستقیماً در همین لیست قابل تغییر است (Quick Action).
 */
@Composable
fun VocabularyListScreen(
    onConceptClick: (Long) -> Unit,
    viewModel: VocabularyListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= state.items.size - 5
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.vocabulary_search_placeholder)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        if (state.categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text(stringResource(R.string.vocabulary_all_categories)) }
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
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            FilterChip(
                selected = state.sortOrder == VocabularySortOrder.RECENT,
                onClick = { viewModel.onSortOrderSelected(VocabularySortOrder.RECENT) },
                label = { Text(stringResource(R.string.vocabulary_sort_recent)) }
            )
            FilterChip(
                selected = state.sortOrder == VocabularySortOrder.ALPHABETICAL,
                onClick = { viewModel.onSortOrderSelected(VocabularySortOrder.ALPHABETICAL) },
                label = { Text(stringResource(R.string.vocabulary_sort_alphabetical)) }
            )
        }
        Spacer(modifier = Modifier.height(Spacing.sm))

        if (state.isLoading && state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = if (state.searchQuery.isBlank() && state.selectedCategoryId == null) {
                            stringResource(R.string.vocabulary_empty_no_words)
                        } else {
                            stringResource(R.string.vocabulary_empty_no_match)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(bottom = Spacing.xl)
            ) {
                items(state.items, key = { it.id }) { concept ->
                    VocabularyRow(
                        concept = concept,
                        sourceLanguage = state.sourceLanguage,
                        targetLanguage = state.targetLanguage,
                        onClick = { onConceptClick(concept.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(concept) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabularyRow(
    concept: Concept,
    sourceLanguage: String,
    targetLanguage: String,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = concept.contentFor(sourceLanguage)?.text ?: "—",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    // بند 64: اگر کلمه چند معنی داشته باشد، همه با «، » جدا نمایش داده می‌شوند.
                    text = concept.contentsFor(targetLanguage)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("، ") { it.text }
                        ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = stringResource(if (concept.favorite) R.string.vocabulary_remove_favorite else R.string.vocabulary_add_favorite),
                    tint = if (concept.favorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    }
                )
            }
        }
    }
}
