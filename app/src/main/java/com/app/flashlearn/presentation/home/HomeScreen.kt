package com.app.flashlearn.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.ui.theme.DifficultyBadge
import com.app.flashlearn.ui.theme.GradientHero
import com.app.flashlearn.ui.theme.ProgressBar
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.SectionHeader
import com.app.flashlearn.ui.theme.Spacing
import com.app.flashlearn.ui.theme.StatPill

@Composable
fun HomeScreen(
    onStartReview: (LearningStage) -> Unit,
    onOpenStatistics: () -> Unit,
    onAddWord: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val dueTotal = state.dueDaily + state.dueWeekly + state.dueMonthly
    val progress = if (state.totalWords == 0) 0f else (state.totalWords - dueTotal).coerceAtLeast(0).toFloat() / state.totalWords

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("امروز چه چیزی یاد می‌گیریم؟", style = MaterialTheme.typography.headlineLarge)
                    Text(if (state.sourceLanguage.isNotBlank()) "${state.sourceLanguage}  →  ${state.targetLanguage}" else "جفت زبان فعال", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatPill(Icons.Default.LocalFireDepartment, "7", "روز پیوسته")
            }
        }

        item {
            GradientHero(
                title = "مسیر امروزت آماده است ✦",
                subtitle = if (dueTotal > 0) "$dueTotal واژه برای مرور منتظر توست" else "همه مرورهای امروز انجام شده‌اند"
            ) {
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
                ProgressBar(progress, Modifier.fillMaxWidth())
                Text("$dueTotal باقی‌مانده  •  ${state.totalWords} واژه در کتابخانه", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    SectionHeader("مرورهای آماده")
                    ReviewTile("☀", "روزانه", state.dueDaily, "حافظه فعال", MaterialTheme.colorScheme.primary) { onStartReview(LearningStage.DAILY) }
                    ReviewTile("◒", "هفتگی", state.dueWeekly, "تقویت ماندگاری", MaterialTheme.colorScheme.secondary) { onStartReview(LearningStage.WEEKLY) }
                    ReviewTile("☾", "ماهانه", state.dueMonthly, "حافظه بلندمدت", MaterialTheme.colorScheme.tertiary) { onStartReview(LearningStage.MONTHLY) }
                }
            }
        }

        item { SectionHeader("قدرت واژگان", action = { OutlinedButton(onClick = onOpenStatistics) { Text("جزئیات") } }) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    DifficultyLine("آسان", state.difficultySummary[Difficulty.EASY] ?: 0, state.totalWords, 0)
                    DifficultyLine("متوسط", state.difficultySummary[Difficulty.MEDIUM] ?: 0, state.totalWords, 1)
                    DifficultyLine("سخت", state.difficultySummary[Difficulty.HARD] ?: 0, state.totalWords, 2)
                    DifficultyLine("خیلی سخت", state.difficultySummary[Difficulty.VERY_HARD] ?: 0, state.totalWords, 3)
                }
            }
        }

        item { SectionHeader("دسترسی سریع") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                QuickAction(Icons.Default.AddCircle, "افزودن واژه", Modifier.weight(1f), onAddWord)
                QuickAction(Icons.Default.Refresh, "مرور تصادفی", Modifier.weight(1f)) { onStartReview(LearningStage.DAILY) }
                QuickAction(Icons.Default.BarChart, "آمار", Modifier.weight(1f), onOpenStatistics)
            }
        }
    }
}

@Composable
private fun ReviewTile(icon: String, title: String, count: Int, subtitle: String, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .08f)), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.smallCard)) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$count", style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
                Text("آماده", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.smallCard)) {
        Column(Modifier.padding(vertical = Spacing.lg, horizontal = Spacing.sm), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
