package com.github.panpf.zoomimage.compose.jscommon.test.util

import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHandlerJsCommonTest {

    @Test
    fun testPlatformAssistKey() {
        assertEquals(expected = AssistKey.Ctrl, actual = platformAssistKey())
    }
}