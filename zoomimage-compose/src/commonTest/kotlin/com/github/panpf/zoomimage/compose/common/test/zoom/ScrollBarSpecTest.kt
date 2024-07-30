package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ScrollBarSpecTest {

    @Test
    fun test() {
        ScrollBarSpec().apply {
            assertEquals(
                expected = Color(0xB2888888),
                actual = color
            )
            assertEquals(
                expected = 3.dp,
                actual = size
            )
            assertEquals(
                expected = 6.dp,
                actual = margin
            )
        }

        ScrollBarSpec(
            color = Color.Blue,
            size = 150.dp,
            margin = 110.dp,
        ).apply {
            assertEquals(
                expected = Color.Blue,
                actual = color
            )
            assertEquals(
                expected = 150.dp,
                actual = size
            )
            assertEquals(
                expected = 110.dp,
                actual = margin
            )
        }

        assertEquals(
            expected = ScrollBarSpec(),
            actual = ScrollBarSpec.Default
        )
        assertSame(
            expected = ScrollBarSpec.Default,
            actual = ScrollBarSpec.Default
        )
    }
}