package com.app.flashlearn.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.ui.theme.DifficultyBadge
import com.app.flashlearn.ui.theme.GradientHero
import com.app.flashlearn.ui.theme.ProgressBar
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun HomeScreen(
    onStartReview: (LearningStage) -> Unit,
    onOpenStatistics: () -> Unit,
    onAddWord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dueTotal = state.dueDaily + state.dueWeekly + state.dueMonthly
    val progress = if (state.totalWords == 0) 0f else
        ((state.totalWords - dueTotal).coerceAtLeast(0).toFloat() / state.totalWords)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item { HomeHeader(state.sourceLanguage, state.targetLanguage) }

        item {
            GradientHero(
                title = if (dueTotal > 0) "$dueTotal کلمه منتظر توست" else "امروز چیزی برای مرور نداری",
                subtitle = if (dueTotal > 0) "$dueTotal کلمه از ${state.totalWords} کلمه آماده مرور است" else "خیلی خوب پیش رفتی؛ می‌توانی واژه جدید اضافه کنی."
            ) {
                ProgressBar(progress, Modifier.fillMaxWidth())
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                HomeStat(Icons.Default.LocalFireDepartment, "روز پیوسته", "پیگیری فعالیت", Color(0xFFF97316), Modifier.weight(1f))
                HomeStat(Icons.Default.CheckCircle, "${state.difficultySummary.values.sum()}", "واژه در سطوح", Color(0xFF14B8A6), Modifier.weight(1f))
                HomeStat(Icons.Default.School, "${state.totalWords}", "کل واژه‌ها", Color(0xFF3B82F6), Modifier.weight(1f))
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                QuickAction(Icons.Default.AddCircle, "افزودن واژه", Modifier.weight(1f), onAddWord)
                QuickAction(Icons.Default.Refresh, "مرور تصادفی", Modifier.weight(1f)) { onStartReview(LearningStage.DAILY) }
                QuickAction(Icons.Default.BarChart, "آمار", Modifier.weight(1f), onOpenStatistics)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.card),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("مرورهای آماده", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    ReviewTile(Icons.Default.CalendarMonth, "روزانه", state.dueDaily, "مرور امروز", Color(0xFF10B981)) { onStartReview(LearningStage.DAILY) }
                    ReviewTile(Icons.Default.CalendarMonth, "هفتگی", state.dueWeekly, "تقویت ماندگاری", Color(0xFF3B82F6)) { onStartReview(LearningStage.WEEKLY) }
                    ReviewTile(Icons.Default.CalendarMonth, "ماهانه", state.dueMonthly, "حافظه بلندمدت", Color(0xFF8B5CF6)) { onStartReview(LearningStage.MONTHLY) }
                }
            }
        }

        item {
            Text("قدرت واژگان", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Spacing.xs))
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    DifficultyLine("آسان", state.difficultySummary[Difficulty.EASY] ?: 0, state.totalWords, 0)
                    DifficultyLine("متوسط", state.difficultySummary[Difficulty.MEDIUM] ?: 0, state.totalWords, 1)
                    DifficultyLine("سخت", state.difficultySummary[Difficulty.HARD] ?: 0, state.totalWords, 2)
                    DifficultyLine("خیلی سخت", state.difficultySummary[Difficulty.VERY_HARD] ?: 0, state.totalWords, 3)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(source: String, target: String) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("امروز چه چیزی یاد می‌گیریم؟", style = MaterialTheme.typography.headlineMedium, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                Text("فلش‌لرن را هر روز کمی بهتر کن", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StreakBadge()
        }
        Row(Modifier.fillMaxWidth().padding(top = Spacing.sm), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text("$source  →  $target", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("  ${languageFlags(source, target)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StreakBadge() {
    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
            Text("روز پیوسته", fontWeight = FontWeight.Bold, color = Color(0xFFEA580C), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HomeStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(Radius.smallCard), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .08f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = Spacing.md), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(23.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(Radius.smallCard)) {
        Column(Modifier.padding(vertical = Spacing.md, horizontal = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReviewTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, count: Int, subtitle: String, tint: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .08f)), shape = RoundedCornerShape(Radius.smallCard)) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).padding(0.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(25.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$count", style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
                Text("آماده", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DifficultyLine(label: String, count: Int, total: Int, level: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DifficultyBadge(label, level)
        ProgressBar(if (total == 0) 0f else count.toFloat() / total, Modifier.weight(1f).padding(horizontal = Spacing.md), 7)
        Text("$count", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

private fun languageFlags(source: String, target: String): String {
    fun flag(code: String): String = when (code.lowercase()) {
        "es" -> "🇪🇸"; "fa" -> "🇮🇷"; "en" -> "🇬🇧"; "fr" -> "🇫🇷"; "de" -> "🇩🇪"; "it" -> "🇮🇹"; "pt" -> "🇵🇹"; "ru" -> "🇷🇺"; "ar" -> "🇸🇦"; "tr" -> "🇹🇷"; "zh" -> "🇨🇳"; "ja" -> "🇯🇵"; "ko" -> "🇰🇷"; "nl" -> "🇳🇱"; "pl" -> "🇵🇱"; else -> "🌐"
    }
    return if (source.isNotBlank() && target.isNotBlank()) "${flag(source)}  ${flag(target)}" else "🌐"
}
