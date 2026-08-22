package com.app.flashlearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

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

    CompositionLocalProvider(
        LocalStatusColors provides statusColors,
        // رفع باگ: جهت UI باید همیشه راست‌به‌چپ باشد چون تمام محتوای اپ فارسی است،
        // مستقل از زبان سیستم گوشی (که ممکن است انگلیسی باشد و باعث چپ‌چین شدن منوها،
        // آیکون‌ها، و ترتیب نمایش اعداد/کلمات لاتین وسط جمله فارسی می‌شد).
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FlashLearnTypography,
            content = content
        )
    }
}
