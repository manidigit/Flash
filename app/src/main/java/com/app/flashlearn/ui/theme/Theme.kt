package com.app.flashlearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

/**
 * تم اصلی اپلیکیشن با پشتیبانی از Light/Dark/System Default (بند 6).
 * useDarkTheme=null یعنی از تنظیم سیستم پیروی کن؛ true/false یعنی کاربر صریحاً انتخاب کرده (بند 54).
 */
@Composable
fun FlashLearnTheme(
    useDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = useDarkTheme ?: isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val statusColors = if (darkTheme) {
        StatusColors(success = SuccessDark, warning = WarningDark, due = DueDark, learned = LearnedDark)
    } else {
        StatusColors(success = SuccessLight, warning = WarningLight, due = DueLight, learned = LearnedLight)
    }

    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FlashLearnTypography,
            content = content
        )
    }
}
