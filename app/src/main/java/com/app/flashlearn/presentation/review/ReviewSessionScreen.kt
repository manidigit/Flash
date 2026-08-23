package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ReviewMode
import com.app.flashlearn.ui.theme.FlashLearnExtras
import com.app.flashlearn.ui.theme.Spacing

/**
 * صفحه جلسه مرور: Progress Bar بالای صفحه (بند 35)، به‌همراه دو حالت ممکن:
 * - FLASHCARD (پیش‌فرض): فلش‌کارت با Flip/Swipe (بند 32-34) و دکمه‌های «بلد نیستم / بلدم».
 * - MULTIPLE_CHOICE (تست چهارگزینه‌ای): متن مبدأ + چند گزینه ترجمه که فقط یکی درست است؛
 *   انتخاب گزینه درست/غلط دقیقاً معادل «بلدم»/«بلد نیستم» به الگوریتم مرور گزارش می‌شود.
 */
@Composable
fun ReviewSessionScreen(
    reviewType: String,
    onFinished: () -> Unit,
    viewModel: ReviewSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.isFinished -> {
            ReviewSessionSummary(
                correctCount = state.correctCount,
                wrongCount = state.wrongCount,
                emptyQueue = state.queue.isEmpty(),
                onDone = onFinished
            )
        }

        else -> {
            val concept = state.currentConcept
            if (concept == null) {
                ReviewSessionSummary(
                    correctCount = state.correctCount,
                    wrongCount = state.wrongCount,
                    emptyQueue = true,
                    onDone = onFinished
                )
                return
            }

            val front = concept.contentFor(state.sourceLanguage)
                ?: ContentItem(languageCode = state.sourceLanguage, text = "—")
            // بند 64: یک کلمه می‌تواند چند معنی داشته باشد، پس همه ترجمه‌های این زبان
            // (نه فقط اولی) به فلش‌کارت داده می‌شود تا هرکدام نمایش داده شود.
            val backs = concept.contentsFor(state.targetLanguage).ifEmpty {
                listOf(ContentItem(languageCode = state.targetLanguage, text = "—"))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onFinished) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.review_session_exit)
                            )
                        }
                        Text(state.progressLabel, style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = stringResource(R.string.review_session_correct_wrong_counter, state.correctCount, state.wrongCount),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (state.totalCount == 0) 0f else state.currentIndex / state.totalCount.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.reviewMode == ReviewMode.MULTIPLE_CHOICE) {
                    Box(modifier = Modifier.weight(1f)) {
                        MultipleChoiceCard(
                            frontText = front.text,
                            options = state.choiceOptions,
                            isLoading = state.isLoadingChoices,
                            selectedOption = state.selectedChoiceText,
                            correctOptions = backs.map { it.text.trim().lowercase() }.toSet(),
                            onSelect = viewModel::selectChoice
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FlashcardView(
                            front = front,
                            backs = backs,
                            tags = concept.tags,
                            isFlipped = state.isFlipped,
                            onFlip = viewModel::flipCard,
                            onSwipeLeft = { viewModel.answer(isCorrect = false) },
                            onSwipeRight = { viewModel.answer(isCorrect = true) }
                        )
                    }

                    if (state.isFlipped) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Button(
                                onClick = { viewModel.answer(isCorrect = false) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(stringResource(R.string.review_session_dont_know))
                            }
                            Button(
                                onClick = { viewModel.answer(isCorrect = true) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FlashLearnExtras.status.success)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(stringResource(R.string.review_session_know_it))
                            }
                        }
                    } else {
                        // بند 32-34 (رفع باگ): قبل از Flip کردن کارت، دکمه‌های بلدم/بلد نیستم
                        // اصلاً نمایش داده نمی‌شوند تا کاربر مجبور شود اول ترجمه را ببیند و
                        // نتواند کورکورانه (بدون دیدن معنی) به یک کلمه جواب بدهد.
                        OutlinedButton(
                            onClick = viewModel::flipCard,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(stringResource(R.string.review_session_show_answer))
                        }
                    }
                }
            }
        }
    }
}

/**
 * تست چهارگزینه‌ای: متن مبدأ در یک کارت بزرگ بالا، و گزینه‌های ترجمه در یک Grid دو‌ستونه
 * پایین (شبیه اپ‌های رایج آموزش زبان). بعد از انتخاب یک گزینه (رفع باگ: قبلاً بدون هیچ
 * بازخوردی فوراً می‌رفت بعدی)، گزینه درست سبز و گزینه غلطِ انتخاب‌شده (اگر اشتباه بود)
 * قرمز می‌شود؛ در همین حین همه گزینه‌ها غیرفعال می‌مانند تا رفتن خودکار به کارت بعدی.
 */
@Composable
private fun MultipleChoiceCard(
    frontText: String,
    options: List<String>,
    isLoading: Boolean,
    selectedOption: String?,
    correctOptions: Set<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = frontText,
                modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = stringResource(R.string.review_session_choice_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            options.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    pair.forEach { option ->
                        val isCorrectOption = option.trim().lowercase() in correctOptions
                        val isSelectedOption = option == selectedOption
                        val showFeedback = selectedOption != null
                        val containerColor = when {
                            showFeedback && isCorrectOption -> FlashLearnExtras.status.success
                            showFeedback && isSelectedOption && !isCorrectOption -> MaterialTheme.colorScheme.error
                            else -> CardDefaults.cardColors().containerColor
                        }
                        Card(
                            // به‌جای Card(enabled=false)، تپ‌های اضافه بعد از انتخاب اول را
                            // در خود onClick نادیده می‌گیریم (رفع باگ: enabled=false در
                            // متریال۳ به‌طور خودکار رنگ‌های سبز/قرمز سفارشی را با رنگ
                            // پیش‌فرض «غیرفعال» جایگزین می‌کرد و اصلاً دیده نمی‌شدند).
                            onClick = { if (selectedOption == null) onSelect(option) },
                            modifier = Modifier.weight(1f).height(96.dp),
                            colors = CardDefaults.cardColors(containerColor = containerColor)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = option,
                                    modifier = Modifier.padding(Spacing.sm),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (showFeedback && (isCorrectOption || isSelectedOption)) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewSessionSummary(
    correctCount: Int,
    wrongCount: Int,
    emptyQueue: Boolean,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(if (emptyQueue) R.string.review_session_nothing_to_review else R.string.review_session_finished),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        if (!emptyQueue) {
            Text(
                text = stringResource(R.string.review_session_result_summary, correctCount, wrongCount),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }
        Button(onClick = onDone, modifier = Modifier.padding(top = Spacing.xl)) {
            Text(stringResource(R.string.review_session_back_to_home))
        }
    }
}
