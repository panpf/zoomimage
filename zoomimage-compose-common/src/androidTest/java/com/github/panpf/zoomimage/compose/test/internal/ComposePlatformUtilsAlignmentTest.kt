package com.github.panpf.zoomimage.compose.test.internal

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.compose.internal.isBottom
import com.github.panpf.zoomimage.compose.internal.isCenter
import com.github.panpf.zoomimage.compose.internal.isEnd
import com.github.panpf.zoomimage.compose.internal.isHorizontalCenter
import com.github.panpf.zoomimage.compose.internal.isStart
import com.github.panpf.zoomimage.compose.internal.isTop
import com.github.panpf.zoomimage.compose.internal.isVerticalCenter
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.valueOf
import org.junit.Assert
import org.junit.Test

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
            Assert.assertEquals(
                it.first.name,
                it.first.align(size, space, LayoutDirection.Ltr),
                it.second
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
            Assert.assertEquals(it.first.name, it.second)
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
            Assert.assertEquals(it.first, Alignment.valueOf(it.second))
        }

        assertThrow(IllegalArgumentException::class) {
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
            Assert.assertEquals(it.first.name, it.first.isStart, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isHorizontalCenter, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isCenter, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isEnd, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isTop, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isVerticalCenter, it.second)
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
            Assert.assertEquals(it.first.name, it.first.isBottom, it.second)
        }
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