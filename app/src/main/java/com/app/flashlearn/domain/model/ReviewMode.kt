package com.app.flashlearn.domain.model

/**
 * حالت نمایش جلسه مرور (ویژگی جدید): FLASHCARD همان حالت قبلی (Flip + بلدم/بلد نیستم)،
 * MULTIPLE_CHOICE یک تست چهارگزینه‌ای است که کاربر باید ترجمه درست را از میان چند گزینه
 * پیدا کند؛ انتخاب گزینه درست معادل «بلدم» و گزینه غلط معادل «بلد نیستم» به الگوریتم مرور
 * گزارش می‌شود.
 */
enum class ReviewMode {
    FLASHCARD,
    MULTIPLE_CHOICE
}
