package com.app.flashlearn.presentation.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.ui.theme.ErrorLight
import com.app.flashlearn.ui.theme.LearnedLight
import com.app.flashlearn.ui.theme.SuccessLight
import com.app.flashlearn.ui.theme.Warning

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("آمار", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = StatsPeriod.entries.indexOf(state.period)) {
            StatsPeriod.entries.forEach { period ->
                Tab(
                    selected = state.period == period,
                    onClick = { viewModel.onPeriodChanged(period) },
                    text = { Text(period.label) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = LearnedLight),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("درصد موفقیت", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Text("${state.accuracyPercent}%", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("کل مرورها", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Text("${state.totalReviews}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("مرور روزانه", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        DailyLineChart(state.dailyCounts, modifier = Modifier.fillMaxWidth().height(160.dp))

        Spacer(Modifier.height(24.dp))
        Text("توزیع سختی", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        DifficultyDonutChart(state.difficultyDistribution, modifier = Modifier.size(180.dp))
    }
}

@Composable
private fun DailyLineChart(data: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) { Text("داده‌ای برای این بازه نیست") }
        return
    }
    val maxValue = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    val lineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, (_, count) ->
            Offset(x = index * stepX, y = size.height - (count.toFloat() / maxValue) * size.height)
        }
        for (i in 0 until points.size - 1) {
            drawLine(color = lineColor, start = points[i], end = points[i + 1], strokeWidth = 4f, cap = StrokeCap.Round)
        }
        points.forEach { drawCircle(color = lineColor, radius = 5f, center = it) }
    }
}

@Composable
private fun DifficultyDonutChart(distribution: Map<Difficulty, Int>, modifier: Modifier = Modifier) {
    val total = distribution.values.sum()
    if (total == 0) {
        Box(modifier, contentAlignment = Alignment.Center) { Text("داده‌ای نیست") }
        return
    }

    val colors = mapOf(
        Difficulty.EASY to SuccessLight,
        Difficulty.MEDIUM to Warning,
        Difficulty.HARD to ErrorLight,
        Difficulty.VERY_HARD to Color(0xFF991B1B)
    )

    Canvas(modifier = modifier) {
        var startAngle = -90f
        distribution.forEach { (difficulty, count) ->
            val sweep = 360f * count / total
            drawArc(
                color = colors[difficulty] ?: Color.Gray,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = size.minDimension * 0.2f, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}
