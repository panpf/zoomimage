package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.toWindowInsets
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
                expected = ScrollBarSpec.DEFAULT_SIZE,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(
            color = Color.Blue,
            size = 150.dp,
            sideMargin = 110.dp,
            endsMargin = 120.dp,
            enabledWindowInsets = true
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
                actual = sideMargin
            )
            assertEquals(
                expected = 120.dp,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(Color.Red, 140.dp, 10.dp, 20.dp, true).apply {
            assertEquals(
                expected = Color.Red,
                actual = color
            )
            assertEquals(
                expected = 140.dp,
                actual = size
            )
            assertEquals(
                expected = 10.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 20.dp,
                actual = endsMargin
            )
            assertEquals(
                expected = true,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(
            color = Color.Gray,
            size = 15.dp,
            margin = 11.dp,
        ).apply {
            assertEquals(
                expected = Color.Gray,
                actual = color
            )
            assertEquals(
                expected = 15.dp,
                actual = size
            )
            assertEquals(
                expected = 11.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 22.dp,
                actual = endsMargin
            )
            assertEquals(
                expected = false,
                actual = enabledWindowInsets
            )
        }

        ScrollBarSpec(Color.Black, 5.dp, 1.dp).apply {
            assertEquals(
                expected = Color.Black,
                actual = color
            )
            assertEquals(
                expected = 5.dp,
                actual = size
            )
            assertEquals(
                expected = 1.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 2.dp,
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
        val element2 = ScrollBarSpec.Default.copy(color = Color.Blue)
        val element3 = ScrollBarSpec.Default.copy(size = 150.dp)
        val element4 = ScrollBarSpec.Default.copy(sideMargin = 110.dp)
        val element5 = ScrollBarSpec.Default.copy(endsMargin = 120.dp)
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
        assertEquals(expected = Color(0xB2888888), actual = ScrollBarSpec.DEFAULT_COLOR)
        assertEquals(expected = 3.dp, actual = ScrollBarSpec.DEFAULT_SIZE)
        assertEquals(expected = 6.dp, actual = ScrollBarSpec.DEFAULT_MARGIN)
        assertEquals(expected = 6.dp, actual = ScrollBarSpec.DEFAULT_SIDE_MARGIN)
        assertEquals(expected = 12.dp, actual = ScrollBarSpec.DEFAULT_ENDS_MARGIN)

        ScrollBarSpec.Default.apply {
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_COLOR,
                actual = color
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIZE,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN,
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
                expected = ScrollBarSpec.DEFAULT_SIZE,
                actual = size
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_SIDE_MARGIN,
                actual = sideMargin
            )
            assertEquals(
                expected = ScrollBarSpec.DEFAULT_ENDS_MARGIN,
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
                expected = 5.dp,
                actual = size
            )
            assertEquals(
                expected = 10.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 20.dp,
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
                expected = 5.dp,
                actual = size
            )
            assertEquals(
                expected = 10.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 20.dp,
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
                expected = 7.dp,
                actual = size
            )
            assertEquals(
                expected = 14.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 28.dp,
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
                expected = 7.dp,
                actual = size
            )
            assertEquals(
                expected = 14.dp,
                actual = sideMargin
            )
            assertEquals(
                expected = 28.dp,
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
        val density = Density(3f)
        val layoutDirection = LayoutDirection.Ltr
        val scrollBarSpec = ScrollBarSpec.Default
        val insets = scrollBarSpec.toWindowInsets()
        assertEquals(expected = 0, actual = insets.getLeft(density, layoutDirection))
        assertEquals(expected = 0, actual = insets.getTop(density))
        assertEquals(
            expected = with(density) { ((scrollBarSpec.sideMargin * 2) + scrollBarSpec.size).roundToPx() },
            actual = insets.getRight(density, layoutDirection)
        )
        assertEquals(
            expected = with(density) { ((scrollBarSpec.sideMargin * 2) + scrollBarSpec.size).roundToPx() },
            actual = insets.getBottom(density)
        )
    }
}