package com.app.flashlearn.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Text(stringResource(R.string.statistics_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item {
            StatRow(stringResource(R.string.statistics_total_words), "${state.totalWords}")
        }

        item { Text(stringResource(R.string.statistics_stages_section), style = MaterialTheme.typography.titleMedium) }
        items(LearningStage.values().toList()) { stage ->
            StatRow(stringResource(stageLabelRes(stage)), "${state.stageSummary[stage] ?: 0}")
        }

        item { Text(stringResource(R.string.statistics_accuracy_section), style = MaterialTheme.typography.titleMedium) }
        item { AccuracyRow(stringResource(R.string.statistics_period_today), state.today) }
        item { AccuracyRow(stringResource(R.string.statistics_period_this_week), state.thisWeek) }
        item { AccuracyRow(stringResource(R.string.statistics_period_this_month), state.thisMonth) }
        item { AccuracyRow(stringResource(R.string.statistics_period_all_time), state.allTime) }
    }
}

private fun stageLabelRes(stage: LearningStage): Int = when (stage) {
    LearningStage.DAILY -> R.string.review_type_daily
    LearningStage.WEEKLY -> R.string.review_type_weekly
    LearningStage.MONTHLY -> R.string.review_type_monthly
    LearningStage.LEARNED -> R.string.review_type_learned
}

@Composable
private fun StatRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AccuracyRow(label: String, stat: AccuracyStat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.statistics_accuracy_format, stat.percentage, stat.correct, stat.total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
