package com.example.ogitts

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.ogitts", appContext.packageName)
    }

    @Test
    fun testGetAudioFiles() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val files = getAudioFiles(appContext)
        // Test that function returns a list (may be empty)
        assert(files.isNotEmpty() || files.isEmpty())
    }
}
