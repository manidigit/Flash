package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.Difficulty

/**
 * محاسبه امتیاز و سطح سختی یک کلمه از روی کل تاریخچه واقعی مرور، نه یک شمارنده ساده.
 * این کلاس عمداً کاملاً pure و بدون هیچ وابستگی به Android/Room است تا با JUnit ساده و
 * مستقل قابل تست باشد (طبق نیازمندی «calculateDifficulty باید مستقل قابل تست باشد»).
 *
 * اصل بنیادین: اشتباه در مرحله بالاتر (WEEKLY, به‌خصوص MONTHLY) بسیار مهم‌تر از اشتباه در
 * DAILY است، و اشتباهات تکرارشونده در یک مرحله باید امتیاز تشدیدشونده (نه ثابت) بگیرند.
 *
 * چون در این State Machine هر اشتباه در WEEKLY یا MONTHLY «همیشه» باعث بازگشت به DAILY
 * می‌شود، جریمه‌ی «بازگشت» به‌عنوان یک آیتم جدا اضافه نمی‌شود - در امتیاز پایه هر مرحله از
 * قبل لحاظ شده تا دوبار شمارش نشود. مقادیر پایه/تشدید به‌گونه‌ای کالیبره شده‌اند که دقیقاً
 * با مثال‌های مشخص‌شده (یک اشتباه ایزوله = MEDIUM، اشتباهات تکراری MONTHLY = VERY_HARD و
 * غیره) همخوانی داشته باشند.
 */
object DifficultyScoreCalculator {

    /** امتیاز اضافه‌شده برای یک اشتباه در DAILY؛ occurrenceIndex = چندمین اشتباه DAILY (از ۱). */
    fun dailyIncorrectDelta(occurrenceIndex: Int): Int = 3 + (occurrenceIndex - 1) * 1

    /** امتیاز اضافه‌شده برای یک اشتباه در WEEKLY (بازگشت به DAILY را هم شامل می‌شود). */
    fun weeklyIncorrectDelta(occurrenceIndex: Int): Int = 5 + (occurrenceIndex - 1) * 2

    /** امتیاز اضافه‌شده برای یک اشتباه در MONTHLY (بالاترین وزن، طبق نیازمندی صریح). */
    fun monthlyIncorrectDelta(occurrenceIndex: Int): Int = 8 + (occurrenceIndex - 1) * 3

    /**
     * سطح سختی نمایشی از روی امتیاز خام + مکانیزم بهبود/Recovery. امتیاز خام هیچ‌وقت در
     * دیتابیس کم نمی‌شود (تاریخچه واقعی همیشه محفوظ می‌ماند)، ولی سطح سختی *نمایشی* با
     * موفقیت‌های اخیر پشت‌سرهم می‌تواند بهتر شود، بدون این‌که خودِ امتیاز خام یا شمارنده‌های
     * تاریخچه‌ای پاک/بازنویسی شوند.
     *
     * @param rawScore امتیاز خام انباشتی (فیلد difficultyScore، هرگز کاهش نمی‌یابد)
     * @param consecutiveCorrect تعداد پاسخ درست پشت‌سرهم اخیر (برای محاسبه بهبود)
     * @param monthlyIncorrectCount تعداد کل شکست‌های MONTHLY در طول عمر این کلمه
     */
    fun classify(
        rawScore: Int,
        consecutiveCorrect: Int,
        monthlyIncorrectCount: Int
    ): Difficulty {
        // بهبود فقط بعد از رشته موفقیتی که از یک عبور اولیه معمولی (Daily->Weekly->Monthly،
        // یعنی ۳ پاسخ درست پشت‌سرهم) طولانی‌تر باشد شروع می‌شود؛ وگرنه حتی همان عبور اولیه
        // که به‌طور طبیعی ۳ پاسخ درست پشت‌سرهم دارد، امتیاز یک اشتباه ایزوله را زودتر از
        // موعد پاک می‌کرد (که مثال‌های مشخص‌شده در نیازمندی را نقض می‌کرد).
        val decayThreshold = 3
        val decayableStreak = (consecutiveCorrect - decayThreshold).coerceAtLeast(0)
        val recoveryDecay = (decayableStreak * 2).coerceAtMost(rawScore)
        // کلمه‌ای که حتی یک‌بار در MONTHLY شکست خورده هرگز به‌طور کامل EASY نمی‌شود؛ حداقل
        // در ابتدای محدوده MEDIUM می‌ماند، چون شکست MONTHLY باید همیشه یک نشانه باقی بگذارد.
        val floor = if (monthlyIncorrectCount > 0) 3 else 0
        val effectiveScore = (rawScore - recoveryDecay).coerceAtLeast(floor)

        return when {
            effectiveScore <= 2 -> Difficulty.EASY
            effectiveScore <= 7 -> Difficulty.MEDIUM
            effectiveScore <= 14 -> Difficulty.HARD
            else -> Difficulty.VERY_HARD
        }
    }
}
