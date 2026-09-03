package com.app.flashlearn.data.importexport

import android.content.Context
import com.app.flashlearn.core.util.DateTimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * بند 50: "Backup Before Restore" — قبل از هر Import واقعی، یک نسخه از وضعیت فعلی دیتابیس
 * در حافظه داخلی (خصوصی) اپلیکیشن ذخیره می‌شود، بدون نیاز به تعامل کاربر با SAF.
 * این فایل‌ها برای کاربر قابل مشاهده مستقیم نیستند (Export دستی برای آن منظور است)؛
 * هدف این است که اگر Import اشتباه بود، حداقل یک نسخه محلی برای بازیابی دستی وجود داشته باشد.
 */
class AutoBackupWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun writePreRestoreSnapshot(json: String): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "pre_restore_backups").apply { mkdirs() }

        // فقط چند نسخه آخر نگه داشته می‌شود تا حافظه داخلی پر نشود.
        dir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(4)?.forEach { it.delete() }

        val file = File(dir, "backup_${DateTimeUtils.now()}.json")
        file.writeText(json)
        file.absolutePath
    }
}
