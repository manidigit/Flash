package com.app.flashlearn.domain.model

/** استراتژی حل تعارض هنگام Import یک Concept که uuid آن از قبل با محتوای متفاوت وجود دارد (بند 47). */
enum class ConflictResolution {
    KEEP_EXISTING,
    USE_IMPORTED,
    /** فیلد-به-فیلد: مقدار موجود اولویت دارد، فقط فیلدهای خالی از نسخه Import پر می‌شوند؛ Tags اجتماع دو مجموعه است. */
    MERGE,
    SKIP
}
