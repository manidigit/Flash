package com.app.flashlearn.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.ui.theme.FlashLearnExtras
import com.app.flashlearn.ui.theme.Spacing

/**
 * Home Dashboard (بند 36-37). کارت‌های Daily/Weekly/Monthly تعداد آماده مرور را نشان می‌دهند
 * و در صورت خالی بودن همه، پیام "You're all caught up!" نمایش داده می‌شود.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (شروع Localization طبق بند 83).
 */
@Composable
fun HomeScreen(
    onStartReview: (LearningStage) -> Unit,
    onOpenStatistics: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.home_total_words, state.totalWords),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.allCaughtUp) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_all_caught_up),
                    modifier = Modifier.padding(Spacing.lg),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            ReviewStageCard(
                title = stringResource(R.string.home_daily_review),
                dueCount = state.dueDaily,
                onClick = { onStartReview(LearningStage.DAILY) }
            )
            ReviewStageCard(
                title = stringResource(R.string.home_weekly_review),
                dueCount = state.dueWeekly,
                onClick = { onStartReview(LearningStage.WEEKLY) }
            )
            ReviewStageCard(
                title = stringResource(R.string.home_monthly_review),
                dueCount = state.dueMonthly,
                onClick = { onStartReview(LearningStage.MONTHLY) }
            )
        }

        Text(stringResource(R.string.home_difficulty_summary), style = MaterialTheme.typography.titleMedium)
        DifficultySummaryRow(state.difficultySummary)

        TextButton(onClick = onOpenStatistics) {
            Text(stringResource(R.string.home_view_statistics))
        }
    }
}

@Composable
private fun ReviewStageCard(title: String, dueCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            val label = if (dueCount > 0) {
                stringResource(R.string.home_due_count, dueCount)
            } else {
                stringResource(R.string.home_all_done)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (dueCount > 0) FlashLearnExtras.status.due else FlashLearnExtras.status.success
            )
        }
    }
}

@Composable
private fun DifficultySummaryRow(summary: Map<Difficulty, Int>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Difficulty.values().forEach { difficulty ->
            Card {
                Column(
                    modifier = Modifier.padding(Spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${summary[difficulty] ?: 0}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = difficulty.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
