package com.app.flashlearn.presentation.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.ui.theme.ErrorLight
import com.app.flashlearn.ui.theme.SuccessLight
import com.app.flashlearn.ui.theme.Warning

// نسخه نهایی: شامل منوی کامل (ویرایش/حذف)، فلگ زبان، دیالوگ تایید حذف — طبق سند ۰۹
@Composable
fun VocabularyListScreen(
    viewModel: VocabularyViewModel = hiltViewModel(),
    activePair: LanguagePair? = null,
    onConceptClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit = onConceptClick
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("جستجو...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            )

            ScrollableTabRow(selectedTabIndex = VocabularyFilter.entries.indexOf(state.selectedFilter)) {
                VocabularyFilter.entries.forEach { filter ->
                    Tab(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.onFilterChanged(filter) },
                        text = { Text(filter.label()) }
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.concepts, key = { it.id }) { concept ->
                        ConceptCard(
                            concept = concept,
                            activePair = activePair,
                            onClick = { onConceptClick(concept.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(concept.id) },
                            onEdit = { onEditClick(concept.id) },
                            onDelete = { viewModel.deleteConcept(concept.id) }
                        )
                    }
                }
            }
        }
    }
}

private fun VocabularyFilter.label() = when (this) {
    VocabularyFilter.ALL -> "همه"
    VocabularyFilter.LEARNED -> "یادگرفته"
    VocabularyFilter.LEARNING -> "در حال یادگیری"
    VocabularyFilter.NEW -> "جدید"
}

@Composable
private fun ConceptCard(
    concept: Concept,
    activePair: LanguagePair?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LanguageFlag(languageCode = activePair?.sourceLanguage)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        concept.contents.firstOrNull { it.languageCode == activePair?.sourceLanguage }?.text
                            ?: concept.contents.firstOrNull()?.text ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                concept.learningState?.let {
                    Spacer(Modifier.height(4.dp))
                    DifficultyChip(it.difficulty)
                }
            }

            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (concept.favorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "علاقه‌مندی",
                        tint = if (concept.favorite) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "منو")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("ویرایش") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = { menuExpanded = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف کلمه") },
            text = { Text("مطمئنی می‌خوای این کلمه حذف بشه؟") },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; onDelete() }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun LanguageFlag(languageCode: String?) {
    val emoji = when (languageCode) {
        "fa" -> "\ud83c\uddee\ud83c\uddf7"
        "en" -> "\ud83c\uddec\ud83c\udde7"
        "es" -> "\ud83c\uddea\ud83c\uddf8"
        "fr" -> "\ud83c\uddeb\ud83c\uddf7"
        "de" -> "\ud83c\udde9\ud83c\uddea"
        else -> "\ud83c\udff3\ufe0f"
    }
    Text(emoji, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun DifficultyChip(difficulty: Difficulty) {
    val (label, color) = when (difficulty) {
        Difficulty.EASY -> "آسان" to SuccessLight
        Difficulty.MEDIUM -> "متوسط" to Warning
        Difficulty.HARD -> "سخت" to ErrorLight
        Difficulty.VERY_HARD -> "خیلی سخت" to ErrorLight
    }
    AssistChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color)
    )
}
