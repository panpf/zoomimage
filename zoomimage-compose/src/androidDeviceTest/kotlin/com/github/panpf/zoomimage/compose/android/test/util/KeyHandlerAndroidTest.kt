package com.github.panpf.zoomimage.compose.android.test.util

import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHandlerAndroidTest {

    @Test
    fun testPlatformAssistKey() {
        assertEquals(expected = AssistKey.Ctrl, actual = platformAssistKey())
    }
}