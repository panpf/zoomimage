package com.github.panpf.zoomimage.compose.macos.test.util

import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyHandlerMacosTest {

    @Test
    fun testPlatformAssistKey() {
        assertEquals(expected = AssistKey.Meta, actual = platformAssistKey())
    }
}
