package com.app.flashlearn.presentation.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.core.util.asString
import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import com.app.flashlearn.ui.theme.Spacing

/**
 * Paste Text Import (بند 42): Textarea برای Paste کردن، دکمه Parse، و یک Preview Table
 * قابل ویرایش/حذف قبل از Import نهایی.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (بند 83).
 *
 * درخواست کاربر: انتخاب یک دسته‌بندی (یا ساخت دسته‌بندی جدید) قبل از Import، تا همه
 * کلمات این دسته یک‌جا به همان Category تعلق بگیرند.
 *
 * رفع باگ فضای صفحه: قبلاً بخش دسته‌بندی (عنوان + ردیف Chip ها + فیلد دسته‌بندی جدید)
 * همیشه به‌طور کامل باز بود و فضای زیادی از صفحه کوچک موبایل را می‌گرفت، طوری که لیست
 * پیش‌نمایش (که مهم‌تر است) خیلی کوچک و سخت برای دیدن می‌شد. حالا این بخش پیش‌فرض بسته
 * است و فقط با لمس یک ردیف کوچک (که دسته‌بندی فعلی را هم نشان می‌دهد) باز می‌شود.
 */
@Composable
fun AddPasteTextScreen(
    onImported: () -> Unit,
    viewModel: AddPasteTextViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var newCategoryText by remember { mutableStateOf("") }
    var categorySectionExpanded by remember { mutableStateOf(false) }

    // بند 64 (رفع باگ): قبلاً به‌محض ست‌شدن importedCount بلافاصله از صفحه خارج می‌شدیم و
    // کاربر هیچ‌وقت نمی‌فهمید چند مورد به‌خاطر تکراری بودن رد شده‌اند. حالا فقط وقتی
    // duplicateCount صفر است خودکار خارج می‌شویم؛ در غیر این صورت خلاصه نتیجه نمایش داده
    // می‌شود و کاربر با دکمه خودش خارج می‌شود.
    LaunchedEffect(state.importedCount) {
        if (state.importedCount != null && state.duplicateCount == 0 && state.translationsAddedCount == 0) onImported()
    }

    Column(modifier = Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text(stringResource(R.string.add_paste_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.add_paste_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.sm)
        )

        OutlinedTextField(
            value = state.rawText,
            onValueChange = viewModel::onRawTextChanged,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            placeholder = { Text(stringResource(R.string.add_paste_placeholder)) }
        )

        val selectedCategoryName = state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm)
                .clickable { categorySectionExpanded = !categorySectionExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.add_paste_category_section) +
                    (selectedCategoryName?.let { " - $it" } ?: ""),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (categorySectionExpanded) "▲" else "▼",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (categorySectionExpanded) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.padding(top = Spacing.xs)
            ) {
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
                modifier = Modifier.padding(top = Spacing.xs)
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
        }

        Button(
            onClick = viewModel::parse,
            enabled = state.rawText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
        ) {
            Text(stringResource(R.string.add_paste_parse_button))
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
                stringResource(R.string.add_paste_preview_count, state.includedCount, state.entries.size),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.sm)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(state.entries, key = { it.localId }) { entry ->
                    EntryRow(
                        entry = entry,
                        onToggle = { viewModel.toggleIncluded(entry.localId) },
                        onEdit = { source, target -> viewModel.updateEntry(entry.localId, source, target) },
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
                    if (state.isImporting) stringResource(R.string.add_paste_importing)
                    else stringResource(R.string.add_paste_import_all_button, state.includedCount)
                )
            }
        }
    }
}

@Composable
private fun EntryRow(
    entry: ParsedVocabularyEntry,
    onToggle: () -> Unit,
    onEdit: (String, String) -> Unit,
    onRemove: () -> Unit
) {
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(checked = entry.included, onCheckedChange = { onToggle() })
                Column(modifier = Modifier.weight(1f)) {
                    // رفع باگ نمایشی: singleLine=true قبلاً باعث می‌شد جمله‌های بلند
                    // اسپانیایی/لاتین داخل این فیلد به‌جای شروع، از وسط (نزدیک انتهای متن)
                    // نمایش داده شوند - چون فیلد تک‌خطی برای نمایش کرسر (که پیش‌فرض انتهای
                    // متن است) به‌صورت افقی اسکرول می‌شود، و این با جهت راست‌چین اجباری اپ
                    // ترکیب بدی داشت. حالا اجازه شکستن خط داده می‌شود تا کل متن دیده شود.
                    OutlinedTextField(
                        value = entry.sourceText,
                        onValueChange = { onEdit(it, entry.targetText) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = entry.targetText,
                        onValueChange = { onEdit(entry.sourceText, it) },
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs)
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}
