package com.app.flashlearn.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import com.app.flashlearn.ui.theme.ProgressBar
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.SectionHeader
import com.app.flashlearn.ui.theme.Spacing
import com.app.flashlearn.ui.theme.StatPill

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val learned = state.stageSummary[LearningStage.LEARNED] ?: 0
    val accuracy = state.allTime.percentage

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Text("پیشرفت تو", style = MaterialTheme.typography.headlineLarge)
            Text("آمار واقعی مرورها و میزان تسلط روی واژه‌ها", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatPill(Icons.Default.School, "${state.totalWords}", "کل واژه‌ها", Modifier.weight(1f))
                StatPill(Icons.Default.CheckCircle, "$learned", "یادگرفته", Modifier.weight(1f))
                StatPill(Icons.Default.LocalFireDepartment, "$accuracy%", "دقت کل", Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Text("حفظ ماندگار", style = MaterialTheme.typography.titleLarge)
                    Text("$accuracy٪", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    ProgressBar(accuracy / 100f)
                    Text("${state.allTime.correct} پاسخ درست از ${state.allTime.total}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { SectionHeader("مراحل یادگیری") }
        item {
            Card(shape = RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StageLine("روزانه", state.stageSummary[LearningStage.DAILY] ?: 0, state.totalWords)
                    StageLine("هفتگی", state.stageSummary[LearningStage.WEEKLY] ?: 0, state.totalWords)
                    StageLine("ماهانه", state.stageSummary[LearningStage.MONTHLY] ?: 0, state.totalWords)
                    StageLine("یادگرفته‌شده", learned, state.totalWords)
                }
            }
        }
        item { SectionHeader("پروفایل سختی") }
        item {
            Card(shape = RoundedCornerShape(Radius.card)) {
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    DifficultyLine("آسان", Difficulty.EASY, state)
                    DifficultyLine("متوسط", Difficulty.MEDIUM, state)
                    DifficultyLine("سخت", Difficulty.HARD, state)
                    DifficultyLine("خیلی سخت", Difficulty.VERY_HARD, state)
                }
            }
        }
        item { SectionHeader("دقت مرور") }
        item { AccuracyCard("امروز", state.today) }
        item { AccuracyCard("این هفته", state.thisWeek) }
        item { AccuracyCard("این ماه", state.thisMonth) }
        item { AccuracyCard("مجموع کل", state.allTime) }
    }
}

@Composable private fun StageLine(label: String, count: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(.3f), style = MaterialTheme.typography.bodyMedium)
        ProgressBar(if (total == 0) 0f else count.toFloat() / total, Modifier.weight(.55f).padding(horizontal = Spacing.md), 8)
        Text("$count", modifier = Modifier.weight(.15f), fontWeight = FontWeight.Bold)
    }
}

@Composable private fun DifficultyLine(label: String, difficulty: Difficulty, state: StatisticsUiState) {
    val count = state.difficultySummary[difficulty] ?: 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        DifficultyBadge(label, difficulty.ordinal)
        ProgressBar(if (state.totalWords == 0) 0f else count.toFloat() / state.totalWords, Modifier.weight(1f).padding(horizontal = Spacing.md), 7)
        Text("$count", fontWeight = FontWeight.Bold)
    }
}

@Composable private fun AccuracyCard(label: String, stat: AccuracyStat) {
    Card(shape = RoundedCornerShape(Radius.smallCard)) {
        Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text("${stat.percentage}%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("  ${stat.correct}/${stat.total}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
