package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.isBottom
import com.github.panpf.zoomimage.zoom.isCenter
import com.github.panpf.zoomimage.zoom.isEnd
import com.github.panpf.zoomimage.zoom.isHorizontalCenter
import com.github.panpf.zoomimage.zoom.isStart
import com.github.panpf.zoomimage.zoom.isTop
import com.github.panpf.zoomimage.zoom.isVerticalCenter
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.valueOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AlignmentCompatTest {

    @Test
    fun testAlign() {
        val size = IntSizeCompat(100, 100)
        val space = IntSizeCompat(1000, 1000)

        listOf(
            AlignmentCompat.TopStart to IntOffsetCompat(0, 0),
            AlignmentCompat.TopCenter to IntOffsetCompat(450, 0),
            AlignmentCompat.TopEnd to IntOffsetCompat(900, 0),
            AlignmentCompat.CenterStart to IntOffsetCompat(0, 450),
            AlignmentCompat.Center to IntOffsetCompat(450, 450),
            AlignmentCompat.CenterEnd to IntOffsetCompat(900, 450),
            AlignmentCompat.BottomStart to IntOffsetCompat(0, 900),
            AlignmentCompat.BottomCenter to IntOffsetCompat(450, 900),
            AlignmentCompat.BottomEnd to IntOffsetCompat(900, 900),
        ).forEach {
            assertEquals(
                expected = it.first.align(size, space, true),
                actual = it.second,
                message = it.first.name
            )
        }
    }

    @Test
    fun testName() {
        listOf(
            AlignmentCompat.TopStart to "TopStart",
            AlignmentCompat.TopCenter to "TopCenter",
            AlignmentCompat.TopEnd to "TopEnd",
            AlignmentCompat.CenterStart to "CenterStart",
            AlignmentCompat.Center to "Center",
            AlignmentCompat.CenterEnd to "CenterEnd",
            AlignmentCompat.BottomStart to "BottomStart",
            AlignmentCompat.BottomCenter to "BottomCenter",
            AlignmentCompat.BottomEnd to "BottomEnd",
            MyAlignmentCompat.Default to "Unknown AlignmentCompat: ${MyAlignmentCompat.Default}"
        ).forEach {
            assertEquals(it.first.name, it.second)
        }
    }

    @Test
    fun testValueOf() {
        listOf(
            AlignmentCompat.TopStart to "TopStart",
            AlignmentCompat.TopCenter to "TopCenter",
            AlignmentCompat.TopEnd to "TopEnd",
            AlignmentCompat.CenterStart to "CenterStart",
            AlignmentCompat.Center to "Center",
            AlignmentCompat.CenterEnd to "CenterEnd",
            AlignmentCompat.BottomStart to "BottomStart",
            AlignmentCompat.BottomCenter to "BottomCenter",
            AlignmentCompat.BottomEnd to "BottomEnd",
        ).forEach {
            assertEquals(it.first, AlignmentCompat.valueOf(it.second))
        }

        assertFailsWith(IllegalArgumentException::class) {
            AlignmentCompat.valueOf(MyAlignmentCompat.Default.name)
        }
    }

    @Test
    fun testIsStart() {
        listOf(
            AlignmentCompat.TopStart to true,
            AlignmentCompat.TopCenter to false,
            AlignmentCompat.TopEnd to false,
            AlignmentCompat.CenterStart to true,
            AlignmentCompat.Center to false,
            AlignmentCompat.CenterEnd to false,
            AlignmentCompat.BottomStart to true,
            AlignmentCompat.BottomCenter to false,
            AlignmentCompat.BottomEnd to false,
        ).forEach {
            assertEquals(expected = it.first.isStart, actual = it.second, message = it.first.name)
        }
    }

    @Test
    fun testIsHorizontalCenter() {
        listOf(
            AlignmentCompat.TopStart to false,
            AlignmentCompat.TopCenter to true,
            AlignmentCompat.TopEnd to false,
            AlignmentCompat.CenterStart to false,
            AlignmentCompat.Center to true,
            AlignmentCompat.CenterEnd to false,
            AlignmentCompat.BottomStart to false,
            AlignmentCompat.BottomCenter to true,
            AlignmentCompat.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isHorizontalCenter,
                actual = it.second,
                message = it.first.name
            )
        }
    }

    @Test
    fun testIsCenter() {
        listOf(
            AlignmentCompat.TopStart to false,
            AlignmentCompat.TopCenter to false,
            AlignmentCompat.TopEnd to false,
            AlignmentCompat.CenterStart to false,
            AlignmentCompat.Center to true,
            AlignmentCompat.CenterEnd to false,
            AlignmentCompat.BottomStart to false,
            AlignmentCompat.BottomCenter to false,
            AlignmentCompat.BottomEnd to false,
        ).forEach {
            assertEquals(expected = it.first.isCenter, actual = it.second, message = it.first.name)
        }
    }

    @Test
    fun testIsEnd() {
        listOf(
            AlignmentCompat.TopStart to false,
            AlignmentCompat.TopCenter to false,
            AlignmentCompat.TopEnd to true,
            AlignmentCompat.CenterStart to false,
            AlignmentCompat.Center to false,
            AlignmentCompat.CenterEnd to true,
            AlignmentCompat.BottomStart to false,
            AlignmentCompat.BottomCenter to false,
            AlignmentCompat.BottomEnd to true,
        ).forEach {
            assertEquals(expected = it.first.isEnd, actual = it.second, message = it.first.name)
        }
    }

    @Test
    fun testIsTop() {
        listOf(
            AlignmentCompat.TopStart to true,
            AlignmentCompat.TopCenter to true,
            AlignmentCompat.TopEnd to true,
            AlignmentCompat.CenterStart to false,
            AlignmentCompat.Center to false,
            AlignmentCompat.CenterEnd to false,
            AlignmentCompat.BottomStart to false,
            AlignmentCompat.BottomCenter to false,
            AlignmentCompat.BottomEnd to false,
        ).forEach {
            assertEquals(expected = it.first.isTop, actual = it.second, message = it.first.name)
        }
    }

    @Test
    fun testIsVerticalCenter() {
        listOf(
            AlignmentCompat.TopStart to false,
            AlignmentCompat.TopCenter to false,
            AlignmentCompat.TopEnd to false,
            AlignmentCompat.CenterStart to true,
            AlignmentCompat.Center to true,
            AlignmentCompat.CenterEnd to true,
            AlignmentCompat.BottomStart to false,
            AlignmentCompat.BottomCenter to false,
            AlignmentCompat.BottomEnd to false,
        ).forEach {
            assertEquals(
                expected = it.first.isVerticalCenter,
                actual = it.second,
                message = it.first.name
            )
        }
    }

    @Test
    fun testIsBottom() {
        listOf(
            AlignmentCompat.TopStart to false,
            AlignmentCompat.TopCenter to false,
            AlignmentCompat.TopEnd to false,
            AlignmentCompat.CenterStart to false,
            AlignmentCompat.Center to false,
            AlignmentCompat.CenterEnd to false,
            AlignmentCompat.BottomStart to true,
            AlignmentCompat.BottomCenter to true,
            AlignmentCompat.BottomEnd to true,
        ).forEach {
            assertEquals(expected = it.first.isBottom, actual = it.second, message = it.first.name)
        }
    }

    class MyAlignmentCompat : AlignmentCompat {
        override fun align(
            size: IntSizeCompat,
            space: IntSizeCompat,
            ltrLayout: Boolean
        ): IntOffsetCompat {
            return IntOffsetCompat(0, 0)
        }

        companion object {
            val Default = MyAlignmentCompat()
        }
    }
}