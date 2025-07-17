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
        BitmapTileImage(bitmap1).apply {
            assertSame(bitmap1, bitmap)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap12 = createRGB565Bitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val bitmapTileImage1 = BitmapTileImage(bitmap1).apply {
            assertEquals(bitmap1.width, width)
            assertEquals(bitmap1.height, height)
            assertEquals(bitmap1.byteCount, byteCount)
        }
        val bitmapTileImage12 = BitmapTileImage(bitmap12).apply {
            assertEquals(bitmap12.width, width)
            assertEquals(bitmap12.height, height)
            assertEquals(bitmap12.byteCount, byteCount)
        }
        val bitmapTileImage2 = BitmapTileImage(bitmap2).apply {
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
        val bitmapTileImage = BitmapTileImage(bitmap)
        assertEquals(false, bitmapTileImage.isRecycled)
        bitmapTileImage.recycle()
        assertEquals(Platform.current == Platform.Android, bitmapTileImage.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 = BitmapTileImage(bitmap1)
        val tileImage12 = BitmapTileImage(bitmap1)
        val tileImage2 = BitmapTileImage(bitmap2)

        assertEquals(expected = tileImage1, actual = tileImage1)
        assertEquals(expected = tileImage1, actual = tileImage12)
        assertNotEquals(illegal = tileImage1, actual = null as Any?)
        assertNotEquals(illegal = tileImage1, actual = Any())
        assertNotEquals(illegal = tileImage1, actual = tileImage2)

        assertEquals(expected = tileImage1.hashCode(), actual = tileImage12.hashCode())
        assertNotEquals(illegal = tileImage1.hashCode(), actual = tileImage2.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val bitmap = createBitmap(1101, 703)
        val tileImage = BitmapTileImage(bitmap)
        assertEquals(
            expected = "BitmapTileImage(bitmap=${bitmap.toLogString()})",
            actual = tileImage.toString()
        )
    }
}