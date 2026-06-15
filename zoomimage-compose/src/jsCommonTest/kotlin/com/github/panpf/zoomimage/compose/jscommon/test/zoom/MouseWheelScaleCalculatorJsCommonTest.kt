package com.github.panpf.zoomimage.compose.jscommon.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelScrollMultiplier
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorJsCommonTest {

    @Test
    fun testPlatformMouseWheelStepScrollDelta() {
        assertEquals(expected = 30f, actual = platformMouseWheelScrollMultiplier())
    }
}