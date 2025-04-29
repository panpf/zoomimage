package com.github.panpf.zoomimage.compose.android.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelStepScrollDelta
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorAndroidTest {

    @Test
    fun testPlatformMouseWheelStepScrollDelta() {
        assertEquals(expected = 1.2f, actual = platformMouseWheelStepScrollDelta())
    }
}