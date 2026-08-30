package com.app.flashlearn.presentation.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.VocabularySortOrder
import com.app.flashlearn.ui.theme.DifficultyBadge
import com.app.flashlearn.ui.theme.EmptyState
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.SectionHeader
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun VocabularyListScreen(
    onConceptClick: (Long) -> Unit,
    onDuplicateWordsClick: () -> Unit,
    viewModel: VocabularyListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(listState.firstVisibleItemIndex, listState.layoutInfo.totalItemsCount) {
        if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1) viewModel.loadNextPage()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text("کتابخانه واژگان", style = MaterialTheme.typography.headlineLarge)
                Text("واژه‌ها، معنی‌ها و مسیر یادگیری‌ات را یکجا مدیریت کن.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("جستجو در واژگان…") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.button)
            )
        }
        if (state.isRefreshingNotes) {
            item {
                Text("در حال بررسی واژه‌های قبلی و انتقال پرانتزها به یادداشت…", color = MaterialTheme.colorScheme.primary)
            }
        }
        state.refreshMessage?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.primary) }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                item { FilterChip(state.sortOrder == VocabularySortOrder.RECENT, { viewModel.onSortOrderSelected(VocabularySortOrder.RECENT) }, label = { Text("جدیدترین") }) }
                item { FilterChip(state.sortOrder == VocabularySortOrder.ALPHABETICAL, { viewModel.onSortOrderSelected(VocabularySortOrder.ALPHABETICAL) }, label = { Text("الفبایی") }) }
                item { FilterChip(false, onDuplicateWordsClick, label = { Text("تکراری‌ها") }) }
                item {
                    IconButton(
                        onClick = viewModel::refreshParentheticalNotes,
                        enabled = !state.isRefreshingNotes
                    ) {
                        if (state.isRefreshingNotes) {
                            CircularProgressIndicator()
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "بررسی و انتقال پرانتزها به یادداشت")
                        }
                    }
                }
            }
        }
        if (state.categories.isNotEmpty()) {
            item { SectionHeader("دسته‌بندی‌ها") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item { FilterChip(state.selectedCategoryId == null, { viewModel.onCategorySelected(null) }, label = { Text("همه") }) }
                    items(state.categories, key = { it.id }) { category ->
                        FilterChip(state.selectedCategoryId == category.id, { viewModel.onCategorySelected(if (state.selectedCategoryId == category.id) null else category.id) }, label = { Text(category.name) })
                    }
                }
            }
        }
        if (state.isLoading && state.items.isEmpty()) {
            item { CircularProgressIndicator(Modifier.padding(Spacing.xxl)) }
        } else if (state.items.isEmpty()) {
            item {
                EmptyState(title = if (state.searchQuery.isBlank() && state.selectedCategoryId == null) "کتابخانه هنوز خالی است" else "چیزی پیدا نشد", subtitle = "فیلترها را تغییر بده یا اولین واژه‌ات را اضافه کن.")
            }
        } else {
            items(state.items, key = { it.id }) { concept ->
                VocabularyCard(concept, state.sourceLanguage, state.targetLanguage, { onConceptClick(concept.id) }, { viewModel.toggleFavorite(concept) })
            }
        }
    }
}

@Composable
private fun VocabularyCard(concept: Concept, sourceLanguage: String, targetLanguage: String, onClick: () -> Unit, onFavorite: () -> Unit) {
    val source = concept.contentFor(sourceLanguage)?.text ?: "—"
    val translations = concept.contentsFor(targetLanguage).map { it.text }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(source, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(translations.take(3).joinToString("  •  ").ifBlank { "بدون ترجمه" }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onFavorite) { Icon(Icons.Default.Star, null, tint = if (concept.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .35f)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                concept.tags.firstOrNull()?.let { Text("#${it}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                Text("یادگیری فعال", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
