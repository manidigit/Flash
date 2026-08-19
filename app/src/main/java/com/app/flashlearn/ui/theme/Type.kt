package com.app.flashlearn.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * از FontFamily.Default استفاده می‌شود که روی اکثر دستگاه‌های Android حروف فارسی و لاتین
 * را به‌طور کامل پوشش می‌دهد. برای برندینگ اختصاصی‌تر، بعداً می‌توان فونت Vazirmatn را
 * به‌صورت Variable Font در res/font اضافه و اینجا جایگزین کرد؛ بقیه UI بدون تغییر می‌ماند
 * چون همه‌جا از این Typography استفاده خواهد شد نه فونت Hard-code شده.
 */
private val AppFontFamily = FontFamily.Default

val FlashLearnTypography = Typography(
    headlineLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
)
