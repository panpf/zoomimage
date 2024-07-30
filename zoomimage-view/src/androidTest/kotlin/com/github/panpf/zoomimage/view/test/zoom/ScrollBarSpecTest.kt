package com.github.panpf.zoomimage.view.test.zoom

import android.content.res.Resources
import android.graphics.Color
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ScrollBarSpecTest {

    @Test
    fun test() {
        ScrollBarSpec().apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = margin
            )
        }

        ScrollBarSpec(
            color = Color.BLUE,
            size = 150f,
            margin = 110f,
        ).apply {
            assertEquals(
                expected = Color.BLUE,
                actual = color
            )
            assertEquals(
                expected = 150f,
                actual = size
            )
            assertEquals(
                expected = 110f,
                actual = margin
            )
        }

        assertEquals(
            expected = 0xB2888888.toInt(),
            actual = ScrollBarSpec.DEFAULT_COLOR
        )
        assertEquals(
            expected = 3f,
            actual = ScrollBarSpec.DEFAULT_SIZE
        )
        assertEquals(
            expected = 6f,
            actual = ScrollBarSpec.DEFAULT_MARGIN
        )
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