package com.github.panpf.zoomimage.compose.apple.test.zoom

import com.github.panpf.zoomimage.zoom.platformMouseWheelScrollMultiplier
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorAppleTest {

    @Test
    fun testPlatformMouseWheelScrollMultiplier() {
        assertEquals(expected = 1f, actual = platformMouseWheelScrollMultiplier())
    }
}
