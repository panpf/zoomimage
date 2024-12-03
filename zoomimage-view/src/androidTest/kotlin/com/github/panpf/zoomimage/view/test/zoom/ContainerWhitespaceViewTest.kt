package com.github.panpf.zoomimage.view.test.zoom

import android.view.View
import com.github.panpf.zoomimage.view.zoom.rtlFlipped
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerWhitespaceViewTest {

    @Test
    fun testRtlFlipped() {
        assertEquals(
            expected = ContainerWhitespace(left = 3f, top = 2f, right = 1f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped(
                View.LAYOUT_DIRECTION_RTL
            )
        )
        assertEquals(
            expected = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped(
                View.LAYOUT_DIRECTION_LTR
            )
        )
        assertEquals(
            expected = ContainerWhitespace(left = 3f, top = 2f, right = 1f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped(
                null
            )
        )
    }
}