package com.example.ogitts

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectionTest {

    @Test
    fun testDetectLanguageUkrainian() {
        val text = "Привіт, як справи?"
        val result = detectLanguage(text)
        assertEquals("uk", result)
    }

    @Test
    fun testDetectLanguageRussian() {
        val text = "Привет, как дела?"
        val result = detectLanguage(text)
        assertEquals("ru", result)
    }

    @Test
    fun testDetectLanguageEnglish() {
        val text = "Hello, how are you?"
        val result = detectLanguage(text)
        assertEquals("en", result)
    }

    @Test
    fun testDetectLanguageJapanese() {
        val text = "こんにちは、元気ですか？"
        val result = detectLanguage(text)
        assertEquals("ja", result)
    }

    @Test
    fun testDetectLanguageMixed() {
        val text = "Hello Привіт"
        val result = detectLanguage(text)
        assertEquals("uk", result) // Contains ukrainian specific character 'ї'
    }

    @Test
    fun testDetectLanguageEmpty() {
        val text = ""
        val result = detectLanguage(text)
        assertEquals("en", result)
    }

    @Test
    fun testGetLanguageNameUkrainian() {
        val result = getLanguageName("uk")
        assertEquals("Украинский", result)
    }

    @Test
    fun testGetLanguageNameRussian() {
        val result = getLanguageName("ru")
        assertEquals("Русский", result)
    }

    @Test
    fun testGetLanguageNameEnglish() {
        val result = getLanguageName("en")
        assertEquals("Английский", result)
    }

    @Test
    fun testGetLanguageNameJapanese() {
        val result = getLanguageName("ja")
        assertEquals("Японский", result)
    }

    @Test
    fun testGetLanguageNameUnknown() {
        val result = getLanguageName("fr")
        assertEquals("Неизвестный", result)
    }
}
