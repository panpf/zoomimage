package com.github.panpf.zoomimage.compose.desktop.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelScrollMultiplier
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorDesktopTest {

    @Test
    fun testPlatformMouseWheelStepScrollDelta() {
        assertEquals(expected = 1f, actual = platformMouseWheelScrollMultiplier())
    }
}