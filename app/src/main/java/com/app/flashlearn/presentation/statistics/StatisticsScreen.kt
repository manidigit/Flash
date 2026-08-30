package com.app.flashlearn.presentation.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.ui.theme.DifficultyBadge
import com.app.flashlearn.ui.theme.ProgressBar
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val learned = state.stageSummary[LearningStage.LEARNED] ?: 0
    val accuracy = state.allTime.percentage

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Text("آمار و گزارش", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("تصویر واقعی‌تری از پیشرفت و نقاط قوتت.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ColorStatCard(Icons.Default.MenuBook, "${state.totalWords}", "کل واژه‌ها", Color(0xFF3B82F6), Modifier.weight(1f))
                ColorStatCard(Icons.Default.School, "$learned", "یادگرفته", Color(0xFF10B981), Modifier.weight(1f))
                ColorStatCard(Icons.Default.CheckCircle, "$accuracy٪", "دقت کل", Color(0xFFF59E0B), Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF))) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF6366F1))
                        Text("حفظ ماندگار", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text("$accuracy٪", style = MaterialTheme.typography.displaySmall, color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                    ProgressBar(accuracy / 100f, Modifier.fillMaxWidth())
                    Text("${state.allTime.correct} پاسخ درست از ${state.allTime.total}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("مراحل یادگیری", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Spacing.xs)) }
        item {
            Card(shape = RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StageLine("روزانه", state.stageSummary[LearningStage.DAILY] ?: 0, state.totalWords, Color(0xFF10B981))
                    StageLine("هفتگی", state.stageSummary[LearningStage.WEEKLY] ?: 0, state.totalWords, Color(0xFF3B82F6))
                    StageLine("ماهانه", state.stageSummary[LearningStage.MONTHLY] ?: 0, state.totalWords, Color(0xFF8B5CF6))
                    StageLine("یادگرفته", learned, state.totalWords, Color(0xFFF59E0B))
                }
            }
        }
        item { Text("پروفایل سختی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Spacing.xs)) }
        item {
            Card(shape = RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    DifficultyLine("آسان", Difficulty.EASY, state, Color(0xFF10B981), 0)
                    DifficultyLine("متوسط", Difficulty.MEDIUM, state, Color(0xFFF59E0B), 1)
                    DifficultyLine("سخت", Difficulty.HARD, state, Color(0xFFF97316), 2)
                    DifficultyLine("خیلی سخت", Difficulty.VERY_HARD, state, Color(0xFFEF4444), 3)
                }
            }
        }
        item { Text("دقت مرور", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = Spacing.xs)) }
        item { AccuracyCard("امروز", state.today, Color(0xFF14B8A6)) }
        item { AccuracyCard("این هفته", state.thisWeek, Color(0xFF3B82F6)) }
        item { AccuracyCard("این ماه", state.thisMonth, Color(0xFF8B5CF6)) }
        item { AccuracyCard("مجموع کل", state.allTime, Color(0xFFF59E0B)) }
    }
}

@Composable private fun ColorStatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(Radius.smallCard), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .09f))) {
        Column(Modifier.fillMaxWidth().padding(vertical = Spacing.md, horizontal = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = tint)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun StageLine(label: String, count: Int, total: Int, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(.28f), style = MaterialTheme.typography.bodyMedium)
        ProgressBar(count.toFloat().div(total.coerceAtLeast(1)), Modifier.weight(.57f).padding(horizontal = Spacing.md), 8)
        Text("$count", modifier = Modifier.weight(.15f), fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable private fun DifficultyLine(label: String, difficulty: Difficulty, state: StatisticsUiState, tint: Color, level: Int) {
    val count = state.difficultySummary[difficulty] ?: 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        DifficultyBadge(label, level)
        ProgressBar(count.toFloat().div(state.totalWords.coerceAtLeast(1)), Modifier.weight(1f).padding(horizontal = Spacing.md), 7)
        Text("$count", fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable private fun AccuracyCard(label: String, stat: AccuracyStat, tint: Color) {
    Card(shape = RoundedCornerShape(Radius.smallCard), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .06f))) {
        Row(Modifier.fillMaxWidth().padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = tint)
            Text(label, modifier = Modifier.weight(1f).padding(horizontal = Spacing.sm), style = MaterialTheme.typography.titleMedium)
            Text("${stat.percentage}%", style = MaterialTheme.typography.titleLarge, color = tint, fontWeight = FontWeight.Bold)
            Text("  ${stat.correct}/${stat.total}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
