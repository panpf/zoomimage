package com.github.panpf.zoomimage.view.test.zoom

import android.content.res.Resources
import android.graphics.Color
import com.github.panpf.zoomimage.view.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.view.zoom.toInsets
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ScrollBarSpecTest {

    @Test
    fun testConstructor() {
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
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(
            color = Color.BLUE,
            size = 150f,
            sideMargin = 110f,
            endsMargin = 120f,
            enabledWindowInsets = true
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
                actual = sideMargin
            )
            assertEquals(
                expected = 120f,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(Color.RED, 140f, 10f, 20f, true).apply {
            assertEquals(
                expected = Color.RED,
                actual = color
            )
            assertEquals(
                expected = 140f,
                actual = size
            )
            assertEquals(
                expected = 10f,
                actual = sideMargin
            )
            assertEquals(
                expected = 20f,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(
            color = Color.GRAY,
            size = 15f,
            margin = 11f,
        ).apply {
            assertEquals(
                expected = Color.GRAY,
                actual = color
            )
            assertEquals(
                expected = 15f,
                actual = size
            )
            assertEquals(
                expected = 11f,
                actual = sideMargin
            )
            assertEquals(
                expected = 22f,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(Color.BLACK, 5f, 1f).apply {
            assertEquals(
                expected = Color.BLACK,
                actual = color
            )
            assertEquals(
                expected = 5f,
                actual = size
            )
            assertEquals(
                expected = 1f,
                actual = sideMargin
            )
            assertEquals(
                expected = 2f,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = ScrollBarSpec.Default
        val element11 = ScrollBarSpec.Default
        val element2 = ScrollBarSpec.Default.copy(color = Color.BLUE)
        val element3 = ScrollBarSpec.Default.copy(size = 150f)
        val element4 = ScrollBarSpec.Default.copy(sideMargin = 110f)
        val element5 = ScrollBarSpec.Default.copy(endsMargin = 120f)
        val element6 = ScrollBarSpec.Default.copy(enabledWindowInsets = true)

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = element2)
        assertNotEquals(illegal = element1, actual = element3)
        assertNotEquals(illegal = element1, actual = element4)
        assertNotEquals(illegal = element1, actual = element5)
        assertNotEquals(illegal = element1, actual = element6)
        assertNotEquals(illegal = element2, actual = element3)
        assertNotEquals(illegal = element2, actual = element4)
        assertNotEquals(illegal = element2, actual = element5)
        assertNotEquals(illegal = element2, actual = element6)
        assertNotEquals(illegal = element3, actual = element4)
        assertNotEquals(illegal = element3, actual = element5)
        assertNotEquals(illegal = element3, actual = element6)
        assertNotEquals(illegal = element4, actual = element5)
        assertNotEquals(illegal = element4, actual = element6)
        assertNotEquals(illegal = element5, actual = element6)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element6.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element3.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element2.hashCode(), actual = element6.hashCode())
        assertNotEquals(illegal = element3.hashCode(), actual = element4.hashCode())
        assertNotEquals(illegal = element3.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element3.hashCode(), actual = element6.hashCode())
        assertNotEquals(illegal = element4.hashCode(), actual = element5.hashCode())
        assertNotEquals(illegal = element4.hashCode(), actual = element6.hashCode())
        assertNotEquals(illegal = element5.hashCode(), actual = element6.hashCode())
    }

    @Test
    fun testToString() {
        val scrollBarSpec = ScrollBarSpec.Default
        assertEquals(
            expected = "ScrollBarSpec(" +
                    "color=${scrollBarSpec.color}, " +
                    "size=${scrollBarSpec.size}, " +
                    "sideMargin=${scrollBarSpec.sideMargin}, " +
                    "endsMargin=${scrollBarSpec.endsMargin}, " +
                    "enabledWindowInsets=false)",
            actual = scrollBarSpec.toString()
        )
    }

    @Test
    fun testCompanion() {
        assertEquals(expected = 0xB2888888.toInt(), actual = ScrollBarSpec.DEFAULT_COLOR)
        assertEquals(expected = 3f, actual = ScrollBarSpec.DEFAULT_SIZE)
        assertEquals(expected = 6f, actual = ScrollBarSpec.DEFAULT_MARGIN)
        assertEquals(expected = 6f, actual = ScrollBarSpec.DEFAULT_SIDE_MARGIN)
        assertEquals(expected = 12f, actual = ScrollBarSpec.DEFAULT_ENDS_MARGIN)

        ScrollBarSpec.Default.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }
        ScrollBarSpec.DefaultAndWindowInsets.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIZE * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec.Medium.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = 5 * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = 10 * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = 20 * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }
        ScrollBarSpec.MediumAndWindowInsets.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = 5 * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = 10 * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = 20 * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec.Large.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = 7 * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = 14 * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = 28 * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }
        ScrollBarSpec.LargeAndWindowInsets.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = 7 * Resources.getSystem().displayMetrics.density,
                actual = size
            )
            assertEquals(
                expected = 14 * Resources.getSystem().displayMetrics.density,
                actual = sideMargin
            )
            assertEquals(
                expected = 28 * Resources.getSystem().displayMetrics.density,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }
    }

    @Test
    fun testToInsets() {
        val scrollBarSpec = ScrollBarSpec.Default
        val insets = scrollBarSpec.toInsets()
        assertEquals(expected = 0, actual = insets.left)
        assertEquals(expected = 0, actual = insets.top)
        assertEquals(
            expected = ((scrollBarSpec.sideMargin * 2) + scrollBarSpec.size).roundToInt(),
            actual = insets.right
        )
        assertEquals(
            expected = ((scrollBarSpec.sideMargin * 2) + scrollBarSpec.size).roundToInt(),
            actual = insets.bottom
        )
    }
}