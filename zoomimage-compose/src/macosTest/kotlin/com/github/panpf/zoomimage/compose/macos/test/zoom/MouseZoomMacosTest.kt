package com.github.panpf.zoomimage.compose.macos.test.zoom

import com.github.panpf.zoomimage.compose.zoom.platformDefaultReverseMouseWheelScale
import kotlin.test.Test
import kotlin.test.assertTrue

class MouseZoomMacosTest {

    @Test
    fun testPlatformDefaultReverseMouseWheelScale() {
        assertTrue(platformDefaultReverseMouseWheelScale())
    }
}