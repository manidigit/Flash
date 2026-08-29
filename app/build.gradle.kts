import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

/**
 * رفع درخواست کاربر: «توی تنظیمات نسخه برنامه رو بنویسه تا ببینم هر Update واقعاً جلو
 * رفته یا نه». چون این پروژه به‌صورت دستی نسخه‌گذاری نمی‌شود (و فراموش‌کردن دستی بالا
 * بردن شماره نسخه در هر تغییر، همان دسته باگ‌های قبلی این پروژه بود)، شماره نسخه از
 * روی تعداد Commit های Git محاسبه می‌شود؛ یعنی خودکار و بدون نیاز به یادآوری دستی با هر
 * Push بالا می‌رود. اگر (به هر دلیل، مثلاً Checkout با تاریخچه ناقص) این محاسبه ممکن
 * نبود، به‌جای Crash کردن Build، مقدار ۱ در نظر گرفته می‌شود.
 */
fun gitCommitCount(): Int = try {
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().readText().trim()
    process.waitFor()
    output.toIntOrNull() ?: 1
} catch (e: Exception) {
    1
}

val computedVersionCode = gitCommitCount()
val buildTimeFormatted: String = run {
    val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm")
    formatter.timeZone = TimeZone.getTimeZone("Asia/Tehran")
    formatter.format(Date())
}

android {
    namespace = "com.app.flashlearn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.flashlearn"
        minSdk = 26
        targetSdk = 34
        versionCode = computedVersionCode
        versionName = "1.$computedVersionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BUILD_TIME", "\"$buildTimeFormatted\"")
    }

    // رفع باگ («برای Update باید اول Uninstall کنم»): چون هر Run در GitHub Actions یک
    // ماشین کاملاً تازه است، بدون این بخش، Gradle از کلید Debug پیش‌فرض سیستم (که هربار
    // به‌صورت تصادفی روی همان Runner ساخته می‌شود) استفاده می‌کرد؛ یعنی هر Build با یک
    // امضای متفاوت از Build قبلی امضا می‌شد. اندروید APK جدید را «Update» همان اپ قبلی
    // نمی‌داند مگر امضایشان دقیقاً یکی باشد، پس نصب رد می‌شد تا اول حذف دستی انجام شود.
    // با مشخص‌کردن یک فایل Keystore ثابت (که در همین Repository نگه‌داری می‌شود)، همه
    // Buildها همیشه با همان یک کلید امضا می‌شوند و Update عادی روی نسخه قبلی جواب می‌دهد.
    signingConfigs {
        getByName("debug") {
            storeFile = file("../keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Encrypted storage برای اطلاعات حساس مثل AI API Key (بند 76 - نکته امنیتی)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // Instrumented tests (Room in-memory DB tests)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
