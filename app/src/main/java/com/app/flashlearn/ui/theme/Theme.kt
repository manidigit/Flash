package com.app.flashlearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFE9E7FF),
    onPrimaryContainer = Color(0xFF26205E),
    secondary = SecondaryLight,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD8F7F2),
    onSecondaryContainer = Color(0xFF073C36),
    tertiary = PinkLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceAltLight,
    error = ErrorLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF29224E),
    primaryContainer = Color(0xFF38306D),
    onPrimaryContainer = Color(0xFFE9E7FF),
    secondary = SecondaryDark,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF174D46),
    onSecondaryContainer = Color(0xFFD8F7F2),
    tertiary = PinkDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceAltDark,
    error = ErrorDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun FlashLearnTheme(
    useDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = useDarkTheme ?: isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val statusColors = if (darkTheme) {
        StatusColors(SuccessDark, WarningDark, DueDark, LearnedDark)
    } else {
        StatusColors(SuccessLight, WarningLight, DueLight, LearnedLight)
    }

    CompositionLocalProvider(
        LocalStatusColors provides statusColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FlashLearnTypography,
            shapes = FlashLearnShapes,
            content = content
        )
    }
}
