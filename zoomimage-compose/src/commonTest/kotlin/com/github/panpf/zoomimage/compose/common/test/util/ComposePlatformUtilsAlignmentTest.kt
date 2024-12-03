package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.zoomimage.compose.util.isBottom
import com.github.panpf.zoomimage.compose.util.isCenter
import com.github.panpf.zoomimage.compose.util.isEnd
import com.github.panpf.zoomimage.compose.util.isHorizontalCenter
import com.github.panpf.zoomimage.compose.util.isStart
import com.github.panpf.zoomimage.compose.util.isTop
import com.github.panpf.zoomimage.compose.util.isVerticalCenter
import com.github.panpf.zoomimage.compose.util.name
import com.github.panpf.zoomimage.compose.util.rtlFlipped
import com.github.panpf.zoomimage.compose.util.valueOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposePlatformUtilsAlignmentTest {

    @Test
    fun testAlign() {
        val size = IntSize(100, 100)
        val space = IntSize(1000, 1000)

        listOf(
            Alignment.TopStart to IntOffset(0, 0),
            Alignment.TopCenter to IntOffset(450, 0),
            Alignment.TopEnd to IntOffset(900, 0),
            Alignment.CenterStart to IntOffset(0, 450),
            Alignment.Center to IntOffset(450, 450),
            Alignment.CenterEnd to IntOffset(900, 450),
            Alignment.BottomStart to IntOffset(0, 900),
            Alignment.BottomCenter to IntOffset(450, 900),
            Alignment.BottomEnd to IntOffset(900, 900),
        ).forEach {
            assertEquals(
                expected = it.first.align(size, space, LayoutDirection.Ltr),
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testName() {
        listOf(
            Alignment.TopStart to "TopStart",
            Alignment.TopCenter to "TopCenter",
            Alignment.TopEnd to "TopEnd",
            Alignment.CenterStart to "CenterStart",
            Alignment.Center to "Center",
            Alignment.CenterEnd to "CenterEnd",
            Alignment.BottomStart to "BottomStart",
            Alignment.BottomCenter to "BottomCenter",
            Alignment.BottomEnd to "BottomEnd",
            MyAlignment.Default to "Unknown Alignment: ${MyAlignment.Default}"
        ).forEach {
            assertEquals(it.first.name, it.second)
        }
    }

    @Test
    fun testValueOf() {
        listOf(
            Alignment.TopStart to "TopStart",
            Alignment.TopCenter to "TopCenter",
            Alignment.TopEnd to "TopEnd",
            Alignment.CenterStart to "CenterStart",
            Alignment.Center to "Center",
            Alignment.CenterEnd to "CenterEnd",
            Alignment.BottomStart to "BottomStart",
            Alignment.BottomCenter to "BottomCenter",
            Alignment.BottomEnd to "BottomEnd",
        ).forEach {
            assertEquals(it.first, Alignment.valueOf(it.second))
        }

        assertFailsWith(IllegalArgumentException::class) {
            Alignment.valueOf(MyAlignment.Default.name)
        }
    }

    @Test
    fun testIsStart() {
        listOf(
            Alignment.TopStart to true,
            Alignment.TopCenter to false,
            Alignment.TopEnd to false,
            Alignment.CenterStart to true,
            Alignment.Center to false,
            Alignment.CenterEnd to false,
            Alignment.BottomStart to true,
            Alignment.BottomCenter to false,
            Alignment.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isStart,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsHorizontalCenter() {
        listOf(
            Alignment.TopStart to false,
            Alignment.TopCenter to true,
            Alignment.TopEnd to false,
            Alignment.CenterStart to false,
            Alignment.Center to true,
            Alignment.CenterEnd to false,
            Alignment.BottomStart to false,
            Alignment.BottomCenter to true,
            Alignment.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isHorizontalCenter,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsCenter() {
        listOf(
            Alignment.TopStart to false,
            Alignment.TopCenter to false,
            Alignment.TopEnd to false,
            Alignment.CenterStart to false,
            Alignment.Center to true,
            Alignment.CenterEnd to false,
            Alignment.BottomStart to false,
            Alignment.BottomCenter to false,
            Alignment.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isCenter,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsEnd() {
        listOf(
            Alignment.TopStart to false,
            Alignment.TopCenter to false,
            Alignment.TopEnd to true,
            Alignment.CenterStart to false,
            Alignment.Center to false,
            Alignment.CenterEnd to true,
            Alignment.BottomStart to false,
            Alignment.BottomCenter to false,
            Alignment.BottomEnd to true,
        ).forEach {
            assertEquals(
                expected = it.first.isEnd,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsTop() {
        listOf(
            Alignment.TopStart to true,
            Alignment.TopCenter to true,
            Alignment.TopEnd to true,
            Alignment.CenterStart to false,
            Alignment.Center to false,
            Alignment.CenterEnd to false,
            Alignment.BottomStart to false,
            Alignment.BottomCenter to false,
            Alignment.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isTop,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsVerticalCenter() {
        listOf(
            Alignment.TopStart to false,
            Alignment.TopCenter to false,
            Alignment.TopEnd to false,
            Alignment.CenterStart to true,
            Alignment.Center to true,
            Alignment.CenterEnd to true,
            Alignment.BottomStart to false,
            Alignment.BottomCenter to false,
            Alignment.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isVerticalCenter,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testIsBottom() {
        listOf(
            Alignment.TopStart to false,
            Alignment.TopCenter to false,
            Alignment.TopEnd to false,
            Alignment.CenterStart to false,
            Alignment.Center to false,
            Alignment.CenterEnd to false,
            Alignment.BottomStart to true,
            Alignment.BottomCenter to true,
            Alignment.BottomEnd to true,
        ).forEach {
            assertEquals(
                expected = it.first.isBottom,
                actual = it.second,
                message = it.first.name,
            )
        }
    }

    @Test
    fun testRtlFlipped() {
        assertEquals(
            expected = Alignment.TopStart,
            actual = Alignment.TopStart.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.TopCenter,
            actual = Alignment.TopCenter.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.TopEnd,
            actual = Alignment.TopEnd.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.CenterStart,
            actual = Alignment.CenterStart.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.Center,
            actual = Alignment.Center.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.CenterEnd,
            actual = Alignment.CenterEnd.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.BottomStart,
            actual = Alignment.BottomStart.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.BottomCenter,
            actual = Alignment.BottomCenter.rtlFlipped(LayoutDirection.Ltr)
        )
        assertEquals(
            expected = Alignment.BottomEnd,
            actual = Alignment.BottomEnd.rtlFlipped(LayoutDirection.Ltr)
        )

        assertEquals(
            expected = Alignment.TopEnd,
            actual = Alignment.TopStart.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.TopCenter,
            actual = Alignment.TopCenter.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.TopStart,
            actual = Alignment.TopEnd.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.CenterEnd,
            actual = Alignment.CenterStart.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.Center,
            actual = Alignment.Center.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.CenterStart,
            actual = Alignment.CenterEnd.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.BottomEnd,
            actual = Alignment.BottomStart.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.BottomCenter,
            actual = Alignment.BottomCenter.rtlFlipped(LayoutDirection.Rtl)
        )
        assertEquals(
            expected = Alignment.BottomStart,
            actual = Alignment.BottomEnd.rtlFlipped(LayoutDirection.Rtl)
        )

        assertEquals(
            expected = Alignment.TopEnd,
            actual = Alignment.TopStart.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.TopCenter,
            actual = Alignment.TopCenter.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.TopStart,
            actual = Alignment.TopEnd.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.CenterEnd,
            actual = Alignment.CenterStart.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.Center,
            actual = Alignment.Center.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.CenterStart,
            actual = Alignment.CenterEnd.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.BottomEnd,
            actual = Alignment.BottomStart.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.BottomCenter,
            actual = Alignment.BottomCenter.rtlFlipped(null)
        )
        assertEquals(
            expected = Alignment.BottomStart,
            actual = Alignment.BottomEnd.rtlFlipped(null)
        )

        assertEquals(
            expected = Alignment.TopEnd,
            actual = Alignment.TopStart.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.TopCenter,
            actual = Alignment.TopCenter.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.TopStart,
            actual = Alignment.TopEnd.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.CenterEnd,
            actual = Alignment.CenterStart.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.Center,
            actual = Alignment.Center.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.CenterStart,
            actual = Alignment.CenterEnd.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.BottomEnd,
            actual = Alignment.BottomStart.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.BottomCenter,
            actual = Alignment.BottomCenter.rtlFlipped()
        )
        assertEquals(
            expected = Alignment.BottomStart,
            actual = Alignment.BottomEnd.rtlFlipped()
        )
    }

    class MyAlignment : Alignment {
        override fun align(
            size: IntSize,
            space: IntSize,
            layoutDirection: LayoutDirection
        ): IntOffset {
            return IntOffset(0, 0)
        }

        companion object {
            val Default = MyAlignment()
        }
    }
}