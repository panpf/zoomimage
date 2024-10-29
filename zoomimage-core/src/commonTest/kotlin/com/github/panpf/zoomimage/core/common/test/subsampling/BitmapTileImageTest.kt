package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.byteCount
import com.github.panpf.zoomimage.subsampling.height
import com.github.panpf.zoomimage.subsampling.toLogString
import com.github.panpf.zoomimage.subsampling.width
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.createBitmap
import com.github.panpf.zoomimage.test.createRGB565Bitmap
import com.github.panpf.zoomimage.test.current
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class BitmapTileImageTest {

    @Test
    fun testConstructor() {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        BitmapTileImage(bitmap1, "bitmap1", fromCache = false).apply {
            assertSame(bitmap1, bitmap)
            assertEquals("bitmap1", key)
            assertEquals(fromCache, false)
        }
        BitmapTileImage(bitmap2, "bitmap2", fromCache = true).apply {
            assertSame(bitmap2, bitmap)
            assertEquals("bitmap2", key)
            assertEquals(fromCache, true)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap12 = createRGB565Bitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val bitmapTileImage1 = BitmapTileImage(bitmap1, "bitmap1", fromCache = false).apply {
            assertEquals(bitmap1.width, width)
            assertEquals(bitmap1.height, height)
            assertEquals(bitmap1.byteCount, byteCount)
        }
        val bitmapTileImage12 = BitmapTileImage(bitmap12, "bitmap12", fromCache = false).apply {
            assertEquals(bitmap12.width, width)
            assertEquals(bitmap12.height, height)
            assertEquals(bitmap12.byteCount, byteCount)
        }
        val bitmapTileImage2 = BitmapTileImage(bitmap2, "bitmap2", fromCache = false).apply {
            assertEquals(bitmap2.width, width)
            assertEquals(bitmap2.height, height)
            assertEquals(bitmap2.byteCount, byteCount)
        }

        assertEquals(bitmapTileImage1.width, bitmapTileImage12.width)
        assertEquals(bitmapTileImage1.height, bitmapTileImage12.height)
        assertNotEquals(bitmapTileImage1.byteCount, bitmapTileImage12.byteCount)

        assertNotEquals(bitmapTileImage1.width, bitmapTileImage2.width)
        assertNotEquals(bitmapTileImage1.height, bitmapTileImage2.height)
        assertNotEquals(bitmapTileImage1.byteCount, bitmapTileImage2.byteCount)

        assertNotEquals(bitmapTileImage2.width, bitmapTileImage12.width)
        assertNotEquals(bitmapTileImage2.height, bitmapTileImage12.height)
        assertNotEquals(bitmapTileImage2.byteCount, bitmapTileImage12.byteCount)
    }

    @Test
    fun testRecycle() {
        val bitmap = createBitmap(1101, 703)
        val bitmapTileImage = BitmapTileImage(bitmap, "bitmap1", fromCache = false)
        assertEquals(false, bitmapTileImage.isRecycled)
        bitmapTileImage.recycle()
        assertEquals(Platform.current == Platform.Android, bitmapTileImage.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 = BitmapTileImage(bitmap1, "bitmap1", fromCache = false)
        val tileImage12 = BitmapTileImage(bitmap1, "bitmap1", fromCache = false)
        val tileImage2 = BitmapTileImage(bitmap2, "bitmap2", fromCache = false)
        val tileImage3 = BitmapTileImage(bitmap1, "bitmap3", fromCache = false)
        val tileImage4 = BitmapTileImage(bitmap1, "bitmap1", fromCache = true)

        assertEquals(expected = tileImage1, actual = tileImage1)
        assertEquals(expected = tileImage1, actual = tileImage12)
        assertNotEquals(illegal = tileImage1, actual = null as Any?)
        assertNotEquals(illegal = tileImage1, actual = Any())
        assertNotEquals(illegal = tileImage1, actual = tileImage2)
        assertNotEquals(illegal = tileImage1, actual = tileImage3)
        assertNotEquals(illegal = tileImage1, actual = tileImage4)
        assertNotEquals(illegal = tileImage2, actual = tileImage3)
        assertNotEquals(illegal = tileImage2, actual = tileImage4)
        assertNotEquals(illegal = tileImage3, actual = tileImage4)

        assertEquals(expected = tileImage1.hashCode(), actual = tileImage12.hashCode())
        assertNotEquals(illegal = tileImage1.hashCode(), actual = tileImage2.hashCode())
        assertNotEquals(illegal = tileImage1.hashCode(), actual = tileImage3.hashCode())
        assertNotEquals(illegal = tileImage1.hashCode(), actual = tileImage4.hashCode())
        assertNotEquals(illegal = tileImage2.hashCode(), actual = tileImage3.hashCode())
        assertNotEquals(illegal = tileImage2.hashCode(), actual = tileImage4.hashCode())
        assertNotEquals(illegal = tileImage3.hashCode(), actual = tileImage4.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 = BitmapTileImage(bitmap1, "bitmap1", fromCache = false)
        val tileImage2 = BitmapTileImage(bitmap2, "bitmap2", fromCache = true)

        assertEquals(
            expected = "BitmapTileImage(bitmap=${bitmap1.toLogString()}, key='bitmap1', fromCache=false)",
            actual = tileImage1.toString()
        )
        assertEquals(
            expected = "BitmapTileImage(bitmap=${bitmap2.toLogString()}, key='bitmap2', fromCache=true)",
            actual = tileImage2.toString()
        )
    }
}