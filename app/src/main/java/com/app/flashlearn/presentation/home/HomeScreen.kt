package com.app.flashlearn.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.ui.theme.LearnedLight
import com.app.flashlearn.ui.theme.ReviewDueLight

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToReview: (ReviewStage) -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(4.dp))
            Text("${state.streakDays} روز متوالی", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            ReviewStageCard("مرور روزانه", state.dailyCount, ReviewDueLight) { onNavigateToReview(ReviewStage.DAILY) }
            Spacer(Modifier.height(12.dp))
            ReviewStageCard("مرور هفتگی", state.weeklyCount, ReviewDueLight) { onNavigateToReview(ReviewStage.WEEKLY) }
            Spacer(Modifier.height(12.dp))
            ReviewStageCard("مرور ماهانه", state.monthlyCount, ReviewDueLight) { onNavigateToReview(ReviewStage.MONTHLY) }
            Spacer(Modifier.height(12.dp))
            ReviewStageCard("یادگرفته‌شده", state.learnedCount, LearnedLight) { onNavigateToReview(ReviewStage.LEARNED) }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(onClick = onNavigateToStatistics, modifier = Modifier.fillMaxWidth()) {
            Text("مشاهده آمار")
        }
    }
}

@Composable
private fun ReviewStageCard(title: String, count: Int, accentColor: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("$count", style = MaterialTheme.typography.headlineSmall, color = accentColor)
        }
    }
}
