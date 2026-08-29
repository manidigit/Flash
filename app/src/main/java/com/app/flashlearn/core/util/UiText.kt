package com.app.flashlearn.core.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * پیام‌های قابل‌نمایش در UI که از ViewModel می‌آیند (مثلاً پیام خطا).
 *
 * چرا این کلاس لازم است: طبق بند 83 (Localization) هیچ متن ثابت فارسی نباید مستقیم در کد
 * بماند، اما ViewModel ها (طبق معماری Clean Architecture این پروژه) به Android Context/
 * Resources دسترسی ندارند تا خودشان `context.getString()` صدا بزنند. راه‌حل استاندارد:
 * ViewModel به‌جای رشته خام، یک [UiText] برمی‌گرداند (یا یک Resource Id ثابت با آرگومان‌ها،
 * یا یک رشته پویا مثل پیام خام یک Exception که اصلاً قابل ترجمه نیست) و فقط لایه Compose
 * (که به Context دسترسی دارد) در نهایت آن را با [asString] به متن واقعی تبدیل می‌کند.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText

    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    companion object {
        fun of(@StringRes resId: Int, vararg args: Any): UiText = Resource(resId, args.toList())
    }
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(resId, *args.toTypedArray())
}
