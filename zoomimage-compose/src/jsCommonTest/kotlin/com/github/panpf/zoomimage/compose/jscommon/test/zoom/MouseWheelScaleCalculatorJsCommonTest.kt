package com.github.panpf.zoomimage.compose.jscommon.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelStepScrollDelta
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorJsCommonTest {

    @Test
    fun testPlatformMouseWheelStepScrollDelta() {
        assertEquals(expected = 12f, actual = platformMouseWheelStepScrollDelta())
    }
}