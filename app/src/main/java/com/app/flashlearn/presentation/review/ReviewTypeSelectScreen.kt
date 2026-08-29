package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.ReviewMode
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.SectionHeader
import com.app.flashlearn.ui.theme.Spacing
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

@Composable
fun ReviewTypeSelectScreen(
    onStart: (String, Long?, String) -> Unit,
    viewModel: ReviewTypeSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf(ReviewMode.MULTIPLE_CHOICE) }
    var modeMenuOpen by remember { mutableStateOf(false) }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    data class ReviewType(val type: String, val title: String, val subtitle: String, val icon: ImageVector)
    val types = listOf(
        ReviewType("DAILY", "روزانه", "حافظه فعال", Icons.Default.LightMode),
        ReviewType("WEEKLY", "هفتگی", "تقویت ماندگاری", Icons.Default.CalendarMonth),
        ReviewType("MONTHLY", "ماهانه", "حافظه بلندمدت", Icons.Default.NightsStay),
        ReviewType("RANDOM", "تصادفی", "هر چیزی که آماده است", Icons.Default.Casino),
        ReviewType("EASY", "آسان", "اعتمادبه‌نفس سریع", Icons.Default.Star),
        ReviewType("MEDIUM", "متوسط", "تمرین متعادل", Icons.Default.School),
        ReviewType("HARD", "سخت", "واژه‌های چالش‌برانگیز", Icons.Default.Whatshot),
        ReviewType("VERY_HARD", "خیلی سخت", "آخرین خط دفاع حافظه", Icons.Default.AutoAwesome)
    )

    val categoryName = state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name ?: "همه دسته‌ها"
    val modeName = if (selectedMode == ReviewMode.MULTIPLE_CHOICE) "چهارگزینه‌ای" else "فلش‌کارت"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            Text("وقت مرور است", style = MaterialTheme.typography.headlineMedium)
            Text("نوع تمرین و جلسه را انتخاب کن.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                CompactDropdown(
                    label = "حالت پاسخ",
                    value = modeName,
                    expanded = modeMenuOpen,
                    onExpandedChange = { modeMenuOpen = it },
                    modifier = Modifier.weight(1f)
                ) {
                    DropdownMenuItem(text = { Text("چهارگزینه‌ای") }, onClick = { selectedMode = ReviewMode.MULTIPLE_CHOICE; modeMenuOpen = false })
                    DropdownMenuItem(text = { Text("فلش‌کارت") }, onClick = { selectedMode = ReviewMode.FLASHCARD; modeMenuOpen = false })
                }
                CompactDropdown(
                    label = "دسته‌بندی",
                    value = categoryName,
                    expanded = categoryMenuOpen,
                    onExpandedChange = { categoryMenuOpen = it },
                    modifier = Modifier.weight(1f),
                    enabled = state.categories.isNotEmpty()
                ) {
                    DropdownMenuItem(text = { Text("همه دسته‌ها") }, onClick = { viewModel.onCategorySelected(null); categoryMenuOpen = false })
                    state.categories.forEach { category ->
                        DropdownMenuItem(text = { Text(category.name) }, onClick = { viewModel.onCategorySelected(category.id); categoryMenuOpen = false })
                    }
                }
            }
        }

        item { SectionHeader("جلسه خودت را انتخاب کن", modifier = Modifier.padding(top = Spacing.xs)) }

        items(types.chunked(2), key = { it.joinToString("-") { t -> t.type } }) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                pair.forEach { item ->
                    val c = state.counts[item.type]
                    val count = if (c?.due != null) "${c.due} آماده از ${c.total}" else "${c?.total ?: 0} واژه"
                    CompactSessionCard(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        count = count,
                        modifier = Modifier.weight(1f)
                    ) { onStart(item.type, state.selectedCategoryId, selectedMode.name) }
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }

        item {
            CompactSessionCard(
                icon = Icons.Default.School,
                title = "یادگرفته‌شده",
                subtitle = "برای اطمینان از ماندگاری",
                count = "${state.counts["LEARNED"]?.total ?: 0} واژه",
                modifier = Modifier.fillMaxWidth()
            ) { onStart("LEARNED", state.selectedCategoryId, selectedMode.name) }
        }
    }
}

@Composable
private fun CompactDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier.widthIn(min = 0.dp)) {
        OutlinedButton(
            onClick = { if (enabled) onExpandedChange(true) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) { content() }
    }
}

@Composable
private fun CompactSessionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.smallCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .48f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(end = Spacing.sm), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(count, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
