package com.app.flashlearn.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

/**
 * فلش‌کارت با Flip (لمس) و Swipe (چپ=بلد نیستم، راست=بلدم) طبق بند 32-34.
 * دکمه‌های واضح جدا از این Composable در ReviewSessionScreen قرار دارند، همیشه در دسترس‌اند.
 *
 * بند 64 (رفع باگ «کلمه با چند معنی»): یک کلمه می‌تواند چند معنی/ترجمه داشته باشد
 * (مثلاً «banco» هم «نیمکت» هم «بانک»)، پس پشت کارت یک لیست از ترجمه‌هاست، نه یک متن تنها.
 */
@Composable
fun FlashcardView(
    front: ContentItem,
    backs: List<ContentItem>,
    tags: List<String>,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = tween(durationMillis = 150),
        label = "cardOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .graphicsLayer {
                translationX = animatedOffset
                rotationZ = (animatedOffset / 40).coerceIn(-12f, 12f)
            }
            .pointerInput(isFlipped) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        // بند 32-34: تا کارت Flip نشده (ترجمه دیده نشده)، Swipe نباید
                        // پاسخ را ثبت کند، وگرنه کاربر می‌تواند بدون دیدن معنی، کور جواب بدهد.
                        if (isFlipped) {
                            when {
                                dragOffsetX > 220f -> onSwipeRight()
                                dragOffsetX < -220f -> onSwipeLeft()
                            }
                        }
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (isFlipped) dragOffsetX += dragAmount
                    }
                )
            }
    ) {
        Card(
            onClick = onFlip,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.card),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = front.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                front.pronunciation?.let {
                    Text(
                        text = "/$it/",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.sm)
                    )
                }

                if (!isFlipped) {
                    Text(
                        text = stringResource(R.string.review_session_tap_to_reveal),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Spacing.md)
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(top = Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        backs.forEachIndexed { index, back ->
                            TranslationBlock(
                                back = back,
                                showNumber = backs.size > 1,
                                number = index + 1
                            )
                        }
                    }

                    if (tags.isNotEmpty()) {
                        Text(
                            text = tags.joinToString(" · "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TranslationBlock(back: ContentItem, showNumber: Boolean, number: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (showNumber) "$number. ${back.text}" else back.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        back.pronunciation?.let {
            Text(
                text = "/$it/",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        back.example?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
        back.definition?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
    }
}
