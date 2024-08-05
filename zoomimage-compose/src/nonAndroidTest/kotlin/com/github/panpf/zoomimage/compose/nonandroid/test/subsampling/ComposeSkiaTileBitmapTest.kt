package com.github.panpf.zoomimage.compose.nonandroid.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.ComposeSkiaTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.test.toLogString
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ComposeSkiaTileBitmapTest {

    @Test
    fun testConstructor() {
        val bitmap1 = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val bitmap2 = SkiaBitmap().apply {
            allocN32Pixels(507, 1305, opaque = false)
        }

        ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL)).apply {
            assertEquals(bitmap1.width, bitmap.width)
            assertEquals(bitmap1.height, bitmap.height)
            assertEquals("bitmap1", key)
            assertEquals(bitmapFrom, BitmapFrom.LOCAL)
        }
        ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap2, "bitmap2", BitmapFrom.MEMORY_CACHE)).apply {
            assertEquals(bitmap2.width, bitmap.width)
            assertEquals(bitmap2.height, bitmap.height)
            assertEquals("bitmap2", key)
            assertEquals(bitmapFrom, BitmapFrom.MEMORY_CACHE)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val bitmap12 = SkiaBitmap().apply {
            allocPixels(ImageInfo.makeA8(1101, 703))
        }
        val bitmap2 = SkiaBitmap().apply {
            allocN32Pixels(507, 1305, opaque = false)
        }

        val skiaTileBitmap1 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL)).apply {
                assertEquals(bitmap1.width, width)
                assertEquals(bitmap1.height, height)
                assertEquals(bitmap1.let { ((it.rowBytes * it.height).toLong()) }, byteCount)
            }
        val skiaTileBitmap12 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap12, "bitmap12", BitmapFrom.LOCAL)).apply {
                assertEquals(bitmap12.width, width)
                assertEquals(bitmap12.height, height)
                assertEquals(bitmap12.let { ((it.rowBytes * it.height).toLong()) }, byteCount)
            }
        val skiaTileBitmap2 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap2, "bitmap2", BitmapFrom.LOCAL)).apply {
                assertEquals(bitmap2.width, width)
                assertEquals(bitmap2.height, height)
                assertEquals(bitmap2.let { ((it.rowBytes * it.height).toLong()) }, byteCount)
            }

        assertEquals(skiaTileBitmap1.width, skiaTileBitmap12.width)
        assertEquals(skiaTileBitmap1.height, skiaTileBitmap12.height)
        assertNotEquals(skiaTileBitmap1.byteCount, skiaTileBitmap12.byteCount)

        assertNotEquals(skiaTileBitmap1.width, skiaTileBitmap2.width)
        assertNotEquals(skiaTileBitmap1.height, skiaTileBitmap2.height)
        assertNotEquals(skiaTileBitmap1.byteCount, skiaTileBitmap2.byteCount)

        assertNotEquals(skiaTileBitmap2.width, skiaTileBitmap12.width)
        assertNotEquals(skiaTileBitmap2.height, skiaTileBitmap12.height)
        assertNotEquals(skiaTileBitmap2.byteCount, skiaTileBitmap12.byteCount)
    }

    @Test
    fun testRecycle() {
        val bitmap = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val skiaTileBitmap =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap, "bitmap1", BitmapFrom.LOCAL))
        assertEquals(false, skiaTileBitmap.isRecycled)
        skiaTileBitmap.recycle()
        assertEquals(false, skiaTileBitmap.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val bitmap2 = SkiaBitmap().apply {
            allocN32Pixels(507, 1305, opaque = false)
        }

        val tileBitmap1 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL))
        val tileBitmap12 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL))
        val tileBitmap2 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap2, "bitmap2", BitmapFrom.LOCAL))
        val tileBitmap3 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap3", BitmapFrom.LOCAL))
        val tileBitmap4 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.MEMORY_CACHE))

        assertEquals(expected = tileBitmap1, actual = tileBitmap12)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap2)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap3)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap4)
        assertNotEquals(illegal = tileBitmap2, actual = tileBitmap3)
        assertNotEquals(illegal = tileBitmap2, actual = tileBitmap4)
        assertNotEquals(illegal = tileBitmap3, actual = tileBitmap4)

        assertEquals(expected = tileBitmap1.hashCode(), actual = tileBitmap12.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap2.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap3.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap4.hashCode())
        assertNotEquals(illegal = tileBitmap2.hashCode(), actual = tileBitmap3.hashCode())
        assertNotEquals(illegal = tileBitmap2.hashCode(), actual = tileBitmap4.hashCode())
        assertNotEquals(illegal = tileBitmap3.hashCode(), actual = tileBitmap4.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val bitmap1 = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val bitmap2 = SkiaBitmap().apply {
            allocN32Pixels(507, 1305, opaque = false)
        }

        val tileBitmap1 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL))
        val tileBitmap2 =
            ComposeSkiaTileBitmap(SkiaTileBitmap(bitmap2, "bitmap2", BitmapFrom.MEMORY_CACHE))

        assertEquals(
            expected = "ComposeSkiaTileBitmap(SkiaTileBitmap(key='bitmap1', bitmap=${bitmap1.toLogString()}, bitmapFrom=LOCAL))",
            actual = tileBitmap1.toString()
        )
        assertEquals(
            expected = "ComposeSkiaTileBitmap(SkiaTileBitmap(key='bitmap2', bitmap=${bitmap2.toLogString()}, bitmapFrom=MEMORY_CACHE))",
            actual = tileBitmap2.toString()
        )
    }
}