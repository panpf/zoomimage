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
        val bitmap2 = createBitmap(507, 1305)

        ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = false)).apply {
            assertEquals(bitmap1.width, bitmap.width)
            assertEquals(bitmap1.height, bitmap.height)
            assertEquals("bitmap1", key)
            assertEquals(fromCache, false)
        }
        ComposeTileImage(BitmapTileImage(bitmap2, "bitmap2", fromCache = true)).apply {
            assertEquals(bitmap2.width, bitmap.width)
            assertEquals(bitmap2.height, bitmap.height)
            assertEquals("bitmap2", key)
            assertEquals(fromCache, true)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap12 = createRGB565Bitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = false)).apply {
                assertEquals(bitmap1.width, width)
                assertEquals(bitmap1.height, height)
                assertEquals(bitmap1.byteCount, byteCount)
            }
        val tileImage12 =
            ComposeTileImage(BitmapTileImage(bitmap12, "bitmap12", fromCache = false)).apply {
                assertEquals(bitmap12.width, width)
                assertEquals(bitmap12.height, height)
                assertEquals(bitmap12.byteCount, byteCount)
            }
        val tileImage2 =
            ComposeTileImage(BitmapTileImage(bitmap2, "bitmap2", fromCache = false)).apply {
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
        val tileImage =
            ComposeTileImage(BitmapTileImage(bitmap, "bitmap1", fromCache = false))
        assertEquals(false, tileImage.isRecycled)
        tileImage.recycle()
        assertEquals(false, tileImage.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = createBitmap(1101, 703)
        val bitmap2 = createBitmap(507, 1305)

        val tileImage1 =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = false))
        val tileImage12 =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = false))
        val tileImage2 =
            ComposeTileImage(BitmapTileImage(bitmap2, "bitmap2", fromCache = false))
        val tileImage3 =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap3", fromCache = false))
        val tileImage4 =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = true))

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
        val tileImage =
            ComposeTileImage(BitmapTileImage(bitmap1, "bitmap1", fromCache = false))
        assertEquals(
            expected = "ComposeTileImage(tileImage=BitmapTileImage(bitmap=${bitmap1.toLogString()}, key='bitmap1', fromCache=false))",
            actual = tileImage.toString()
        )
    }
}