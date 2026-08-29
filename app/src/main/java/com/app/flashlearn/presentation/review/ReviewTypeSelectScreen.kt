package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.ReviewMode
import com.app.flashlearn.ui.theme.ReviewModeCard
import com.app.flashlearn.ui.theme.SectionHeader
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun ReviewTypeSelectScreen(
    onStart: (String, Long?, String) -> Unit,
    viewModel: ReviewTypeSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMode by remember { mutableStateOf(ReviewMode.MULTIPLE_CHOICE) }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Text("وقت مرور است", style = MaterialTheme.typography.headlineLarge)
            Text("نوع تمرین را انتخاب کن و شروع کن.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            SectionHeader("حالت پاسخ")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(top = Spacing.sm)) {
                item { FilterChip(selectedMode == ReviewMode.MULTIPLE_CHOICE, { selectedMode = ReviewMode.MULTIPLE_CHOICE }, label = { Text("چهارگزینه‌ای") }) }
                item { FilterChip(selectedMode == ReviewMode.FLASHCARD, { selectedMode = ReviewMode.FLASHCARD }, label = { Text("فلش‌کارت") }) }
            }
        }
        if (state.categories.isNotEmpty()) {
            item { SectionHeader("دسته‌بندی") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    item { FilterChip(state.selectedCategoryId == null, { viewModel.onCategorySelected(null) }, label = { Text("همه") }) }
                    items(state.categories, key = { it.id }) { category ->
                        FilterChip(state.selectedCategoryId == category.id, { viewModel.onCategorySelected(if (state.selectedCategoryId == category.id) null else category.id) }, label = { Text(category.name) })
                    }
                }
            }
        }
        item { SectionHeader("جلسه خودت را انتخاب کن") }
        items(types, key = { it.type }) { item ->
            val type = item.type
            val title = item.title
            val subtitle = item.subtitle
            val icon = item.icon
            val c = state.counts[type]
            val count = if (c?.due != null) "${c.due} آماده از ${c.total}" else "${c?.total ?: 0} واژه"
            ReviewModeCard(icon, title, subtitle, count, false) { onStart(type, state.selectedCategoryId, selectedMode.name) }
        }
        item { SectionHeader("یادگرفته‌شده") }
        item {
            val c = state.counts["LEARNED"]
            ReviewModeCard(Icons.Default.School, "مرور یادگرفته‌شده‌ها", "برای اطمینان از ماندگاری", "${c?.total ?: 0} واژه", false) { onStart("LEARNED", state.selectedCategoryId, selectedMode.name) }
        }
    }
}
