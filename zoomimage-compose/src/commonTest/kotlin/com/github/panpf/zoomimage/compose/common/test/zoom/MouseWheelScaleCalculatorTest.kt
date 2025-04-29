package com.github.panpf.zoomimage.compose.common.test.zoom

import com.github.panpf.zoomimage.zoom.DefaultMouseWheelScaleCalculator
import com.github.panpf.zoomimage.zoom.MouseWheelScaleCalculator
import kotlin.test.Test
import kotlin.test.assertEquals

class MouseWheelScaleCalculatorTest {

    @Test
    fun testDefault() {
        assertEquals(
            expected = DefaultMouseWheelScaleCalculator(),
            actual = MouseWheelScaleCalculator.Default
        )
    }
}