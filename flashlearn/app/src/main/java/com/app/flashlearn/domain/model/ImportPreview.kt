package com.app.flashlearn.domain.model

/**
 * خلاصه‌ای که قبل از اعمال واقعی Import نمایش داده می‌شود (بند 46):
 * چند مورد جدید است، چند مورد از قبل وجود دارد و دقیقاً یکسان است،
 * و چند مورد Conflict دارد (uuid یکسان ولی محتوای متفاوت).
 */
data class ImportPreview(
    val newCount: Int,
    val identicalExistingCount: Int,
    val conflictingUuids: List<String>
) {
    val conflictCount: Int get() = conflictingUuids.size
    val totalInFile: Int get() = newCount + identicalExistingCount + conflictCount
}
