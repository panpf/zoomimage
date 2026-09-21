package com.github.panpf.zoomimage.compose.nonmacos.test.zoom

import com.github.panpf.zoomimage.compose.zoom.platformDefaultReverseMouseWheelScale
import kotlin.test.Test
import kotlin.test.assertFalse

class MouseZoomNonMacosTest {

    @Test
    fun testPlatformDefaultReverseMouseWheelScale() {
        assertFalse(platformDefaultReverseMouseWheelScale())
    }
}