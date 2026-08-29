package com.app.flashlearn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * رنگ‌های وضعیتی که در Material3 ColorScheme پیش‌فرض جایی ندارند
 * (Success, Warning, Due, Learned) اما در سند Design System خواسته شده‌اند.
 * از طریق CompositionLocal در دسترس همه Composable ها قرار می‌گیرند.
 */
data class StatusColors(
    val success: Color,
    val warning: Color,
    val due: Color,
    val learned: Color
)

val LocalStatusColors = staticCompositionLocalOf {
    StatusColors(
        success = SuccessLight,
        warning = WarningLight,
        due = DueLight,
        learned = LearnedLight
    )
}

object FlashLearnExtras {
    val status: StatusColors
        @Composable get() = LocalStatusColors.current
}
