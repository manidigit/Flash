package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.ReviewMode
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun ReviewTypeSelectScreen(
    onStart: (String, Long?, String) -> Unit,
    viewModel: ReviewTypeSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf(ReviewMode.MULTIPLE_CHOICE) }
    var modeMenuOpen by remember { mutableStateOf(false) }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    val schedule = listOf(
        ReviewType("DAILY", "روزانه", "مرور امروز", Icons.Default.LightMode, Color(0xFF10B981)),
        ReviewType("WEEKLY", "هفتگی", "تقویت ماندگاری", Icons.Default.CalendarMonth, Color(0xFF3B82F6)),
        ReviewType("MONTHLY", "ماهانه", "حافظه بلندمدت", Icons.Default.NightsStay, Color(0xFF8B5CF6))
    )
    val difficulty = listOf(
        ReviewType("EASY", "آسان", "اعتمادبه‌نفس سریع", Icons.Default.Star, Color(0xFF10B981)),
        ReviewType("MEDIUM", "متوسط", "تمرین متعادل", Icons.Default.School, Color(0xFFF59E0B)),
        ReviewType("HARD", "سخت", "واژه‌های چالش‌برانگیز", Icons.Default.Whatshot, Color(0xFFF97316)),
        ReviewType("VERY_HARD", "خیلی سخت", "بیشترین چالش", Icons.Default.AutoAwesome, Color(0xFFEF4444))
    )
    val categoryName = state.categories.firstOrNull { it.id == state.selectedCategoryId }?.name ?: "همه دسته‌ها"
    val modeName = if (selectedMode == ReviewMode.MULTIPLE_CHOICE) "چهارگزینه‌ای" else "فلش‌کارت"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("مرور کلمات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("نوع مرور را انتخاب کن.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CompactDropdown("حالت پاسخ", modeName, modeMenuOpen, { modeMenuOpen = it }, Modifier.weight(1f)) {
                    DropdownMenuItem(text = { Text("چهارگزینه‌ای") }, onClick = { selectedMode = ReviewMode.MULTIPLE_CHOICE; modeMenuOpen = false })
                    DropdownMenuItem(text = { Text("فلش‌کارت") }, onClick = { selectedMode = ReviewMode.FLASHCARD; modeMenuOpen = false })
                }
                CompactDropdown("دسته‌بندی", categoryName, categoryMenuOpen, { categoryMenuOpen = it }, Modifier.weight(1f), state.categories.isNotEmpty()) {
                    DropdownMenuItem(text = { Text("همه دسته‌ها") }, onClick = { viewModel.onCategorySelected(null); categoryMenuOpen = false })
                    state.categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { viewModel.onCategorySelected(category.id); categoryMenuOpen = false }) }
                }
            }
        }
        item { SectionTitle("زمان‌بندی مرور") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) { schedule.forEach { item -> ReviewOption(item, countText(state, item.type), Modifier.weight(1f)) { onStart(item.type, state.selectedCategoryId, selectedMode.name) } } } }
        item { SectionTitle("مرور ویژه") }
        item { ReviewOption(ReviewType("RANDOM", "تصادفی", "از همه واژه‌ها", Icons.Default.Casino, Color(0xFF06B6D4)), countText(state, "RANDOM"), Modifier.fillMaxWidth(), prominent = true) { onStart("RANDOM", state.selectedCategoryId, selectedMode.name) } }
        item { SectionTitle("سطح دشواری") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                difficulty.take(2).forEach { item -> ReviewOption(item, countText(state, item.type), Modifier.weight(1f)) { onStart(item.type, state.selectedCategoryId, selectedMode.name) } }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                difficulty.drop(2).forEach { item -> ReviewOption(item, countText(state, item.type), Modifier.weight(1f)) { onStart(item.type, state.selectedCategoryId, selectedMode.name) } }
            }
        }
        item { ReviewOption(ReviewType("LEARNED", "یادگرفته‌شده", "مرور اختیاری برای ماندگاری", Icons.Default.School, Color(0xFF6366F1)), "${state.counts["LEARNED"]?.total ?: 0} واژه", Modifier.fillMaxWidth()) { onStart("LEARNED", state.selectedCategoryId, selectedMode.name) } }
    }
}

@Composable private fun SectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Spacing.xs)) }

private fun countText(state: ReviewTypeSelectUiState, type: String): String {
    val c = state.counts[type]
    return if (c?.due != null) "${c.due} آماده" else "${c?.total ?: 0} واژه"
}

@Composable
private fun CompactDropdown(label: String, value: String, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, modifier: Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    Box(modifier) {
        OutlinedButton(onClick = { if (enabled) onExpandedChange(true) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) { content() }
    }
}

@Composable
private fun ReviewOption(item: ReviewType, count: String, modifier: Modifier, prominent: Boolean = false, onClick: () -> Unit) {
    val x = item
    Card(onClick = onClick, modifier = modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(if (prominent) Radius.card else Radius.smallCard), colors = CardDefaults.cardColors(containerColor = x.tint.copy(alpha = if (prominent) .16f else .09f))) {
        Column(Modifier.fillMaxWidth().padding(if (prominent) Spacing.lg else Spacing.md), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Box(Modifier.size(if (prominent) 46.dp else 38.dp), contentAlignment = Alignment.Center) { Icon(x.icon, null, tint = x.tint, modifier = Modifier.size(if (prominent) 27.dp else 22.dp)) }
            Text(x.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(count, style = MaterialTheme.typography.labelSmall, color = x.tint, fontWeight = FontWeight.SemiBold)
            if (prominent) Text(x.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class ReviewType(val type: String, val title: String, val subtitle: String, val icon: ImageVector, val tint: Color)
