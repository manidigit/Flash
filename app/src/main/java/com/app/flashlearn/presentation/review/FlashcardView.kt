package com.app.flashlearn.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

/**
 * فلش‌کارت با Flip (لمس) و Swipe (چپ=بلد نیستم، راست=بلدم) طبق بند 32-34.
 * دکمه‌های واضح جدا از این Composable در ReviewSessionScreen قرار دارند، همیشه در دسترس‌اند.
 */
@Composable
fun FlashcardView(
    front: ContentItem,
    back: ContentItem,
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
                val shown = if (isFlipped) back else front

                Text(
                    text = shown.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                shown.pronunciation?.let {
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
                }

                if (isFlipped) {
                    shown.example?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.md),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    shown.definition?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
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
