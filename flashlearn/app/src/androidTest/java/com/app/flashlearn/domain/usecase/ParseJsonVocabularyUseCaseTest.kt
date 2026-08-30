package com.app.flashlearn.domain.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * org.json روی JVM خالص (بدون Android) Stub است و پرتاب خطا می‌کند؛ به همین دلیل این تست
 * به‌عنوان Instrumented Test اجرا می‌شود، نه JVM Unit Test (مثل بقیه تست‌های وابسته به org.json
 * در این پروژه، از جمله JsonBackupServiceImpl).
 */
@RunWith(AndroidJUnit4::class)
class ParseJsonVocabularyUseCaseTest {

    private val useCase = ParseJsonVocabularyUseCase()

    @Test
    fun parsesArrayOfObjects() {
        val json = """
            [
              {"source": "manzana", "target": "سیب", "notes": "fruit"},
              {"source": "viajar", "target": "سفر کردن"}
            ]
        """.trimIndent()

        val result = useCase(json)

        assertEquals(2, result.size)
        assertEquals("manzana", result[0].sourceText)
        assertEquals("fruit", result[0].extraLabel)
        assertEquals("viajar", result[1].sourceText)
    }

    @Test
    fun supportsAlternateKeyNames() {
        val json = """[{"text": "casa", "translation": "خانه"}]"""
        val result = useCase(json)

        assertEquals(1, result.size)
        assertEquals("casa", result[0].sourceText)
        assertEquals("خانه", result[0].targetText)
    }

    @Test
    fun skipsObjectsMissingRequiredFields() {
        val json = """[{"source": "onlysource"}, {"source": "hotel", "target": "هتل"}]"""
        val result = useCase(json)

        assertEquals(1, result.size)
        assertEquals("hotel", result[0].sourceText)
    }
}
