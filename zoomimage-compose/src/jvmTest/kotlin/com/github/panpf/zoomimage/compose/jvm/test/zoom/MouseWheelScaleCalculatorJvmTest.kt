package com.github.panpf.zoomimage.compose.jvm.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelScrollMultiplier
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorJvmTest {

    @Test
    fun testPlatformMouseWheelStepScrollDelta() {
        assertEquals(expected = 1f, actual = platformMouseWheelScrollMultiplier())
    }
}