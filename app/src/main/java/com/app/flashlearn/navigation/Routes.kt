package com.app.flashlearn.navigation

/** مسیرهای اصلی Navigation Graph (بند 51-53). */
object Routes {
    const val ONBOARDING = "onboarding"

    const val HOME = "home"
    const val REVIEW_TYPE_SELECT = "review_type_select"
    const val REVIEW_SESSION = "review_session/{reviewType}?categoryId={categoryId}"
    const val VOCABULARY_LIST = "vocabulary_list"
    const val CONCEPT_DETAIL = "concept_detail/{conceptId}"
    const val ADD_HOME = "add_home"
    const val ADD_MANUAL = "add_manual"
    const val ADD_AI = "add_ai"
    const val ADD_PASTE_TEXT = "add_paste_text"
    const val ADD_IMPORT_FILE = "add_import_file"
    const val SETTINGS_HOME = "settings_home"
    const val BACKUP_RESTORE = "backup_restore"
    const val AI_SETTINGS = "ai_settings"
    const val STATISTICS = "statistics"

    fun reviewSession(reviewType: String, categoryId: Long? = null): String {
        val base = "review_session/$reviewType"
        return if (categoryId != null) "$base?categoryId=$categoryId" else base
    }
    fun conceptDetail(conceptId: Long) = "concept_detail/$conceptId"
}

/** مقصدهای Bottom Navigation (بند 51). */
enum class BottomNavDestination(val route: String, val label: String) {
    HOME(Routes.HOME, "خانه"),
    REVIEW(Routes.REVIEW_TYPE_SELECT, "مرور"),
    VOCABULARY(Routes.VOCABULARY_LIST, "واژگان"),
    ADD(Routes.ADD_HOME, "افزودن"),
    SETTINGS(Routes.SETTINGS_HOME, "تنظیمات")
}
