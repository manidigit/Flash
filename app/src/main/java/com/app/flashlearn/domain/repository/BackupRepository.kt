package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.ConflictResolution
import com.app.flashlearn.domain.model.BackupMode
import com.app.flashlearn.domain.model.ImportPreview
import com.app.flashlearn.domain.model.ImportResult

/**
 * Export/Import کامل دیتابیس به/از JSON (بند 44-47). پیاده‌سازی در لایه Data مستقیماً
 * روی Entity ها کار می‌کند (استثنا در Clean Architecture، چون Backup ماهیتاً یک عملیات
 * سطح-دیتابیس است، نه یک Use Case دامنه‌ای معمولی).
 */
interface BackupRepository {
    suspend fun exportToJson(): String
    suspend fun exportToJson(mode: BackupMode): String = exportToJson()

    /** فقط می‌خواند و مقایسه می‌کند؛ هیچ تغییری در دیتابیس اعمال نمی‌کند (بند 46). */
    suspend fun previewImport(json: String): ImportPreview

    /**
     * Import واقعی را انجام می‌دهد. resolution فقط برای مواردی که Conflict دارند اعمال می‌شود؛
     * موارد جدید همیشه درج می‌شوند و موارد کاملاً یکسان همیشه Skip می‌شوند.
     */
    suspend fun applyImport(json: String, resolution: ConflictResolution): ImportResult
}
