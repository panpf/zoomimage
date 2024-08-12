package com.github.panpf.zoomimage.compose.desktop.test.util

import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHandlerDesktopTest {

    @Test
    fun testPlatformAssistKey() {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val assistKey = if (osName.contains("mac")) AssistKey.Meta else AssistKey.Ctrl
        assertEquals(expected = assistKey, actual = platformAssistKey())
    }
}