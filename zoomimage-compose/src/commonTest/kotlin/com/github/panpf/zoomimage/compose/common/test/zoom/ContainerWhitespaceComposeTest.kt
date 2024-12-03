package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.compose.zoom.rtlFlipped
import com.github.panpf.zoomimage.zoom.ContainerWhitespace
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerWhitespaceComposeTest {

    @Test
    fun testRtlFlipped() {
        assertEquals(
            expected = ContainerWhitespace(left = 3f, top = 2f, right = 1f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped(
                LayoutDirection.Rtl
            )
        )
        assertEquals(
            expected = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f),
            actual = ContainerWhitespace(left = 1f, top = 2f, right = 3f, bottom = 4f).rtlFlipped(
                LayoutDirection.Ltr
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