package com.github.panpf.zoomimage.compose.common.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.ComposeTileImage
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.byteCount
import com.github.panpf.zoomimage.subsampling.height
import com.github.panpf.zoomimage.subsampling.toLogString
import com.github.panpf.zoomimage.subsampling.width
import com.github.panpf.zoomimage.test.createBitmap
import com.github.panpf.zoomimage.test.createRGB565Bitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ComposeTileImageTest {

    @Test
    fun testConstructor() {
        val bitmap1 = createBitmap(1101, 703)
        ComposeTileImage(BitmapTileImage(bitmap1)).apply {
            assertEquals(bitmap1.width, bitmap.width)
            assertEquals(bitmap1.height, bitmap.height)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap12 = createRGB565Bitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 = ComposeTileImage(BitmapTileImage(bitmap1)).apply {
            assertEquals(bitmap1.width, width)
            assertEquals(bitmap1.height, height)
            assertEquals(bitmap1.byteCount, byteCount)
        }
        val tileImage12 = ComposeTileImage(BitmapTileImage(bitmap12)).apply {
            assertEquals(bitmap12.width, width)
            assertEquals(bitmap12.height, height)
            assertEquals(bitmap12.byteCount, byteCount)
        }
        val tileImage2 = ComposeTileImage(BitmapTileImage(bitmap2)).apply {
            assertEquals(bitmap2.width, width)
            assertEquals(bitmap2.height, height)
            assertEquals(bitmap2.byteCount, byteCount)
        }

        assertEquals(tileImage1.width, tileImage12.width)
        assertEquals(tileImage1.height, tileImage12.height)
        assertNotEquals(tileImage1.byteCount, tileImage12.byteCount)

        assertNotEquals(tileImage1.width, tileImage2.width)
        assertNotEquals(tileImage1.height, tileImage2.height)
        assertNotEquals(tileImage1.byteCount, tileImage2.byteCount)

        assertNotEquals(tileImage2.width, tileImage12.width)
        assertNotEquals(tileImage2.height, tileImage12.height)
        assertNotEquals(tileImage2.byteCount, tileImage12.byteCount)
    }

    @Test
    fun testRecycle() {
        val bitmap = createBitmap(1101, 703)
        val tileImage = ComposeTileImage(BitmapTileImage(bitmap))
        assertEquals(false, tileImage.isRecycled)
        tileImage.recycle()
        assertEquals(false, tileImage.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 = ComposeTileImage(BitmapTileImage(bitmap1))
        val tileImage12 = ComposeTileImage(BitmapTileImage(bitmap1))
        val tileImage2 = ComposeTileImage(BitmapTileImage(bitmap2))

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
        val bitmap1 = createBitmap(1101, 703)
        val tileImage = ComposeTileImage(BitmapTileImage(bitmap1))
        assertEquals(
            expected = "ComposeTileImage(tileImage=BitmapTileImage(bitmap=${bitmap1.toLogString()}))",
            actual = tileImage.toString()
        )
    }
}