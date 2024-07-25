package com.github.panpf.zoomimage.core.android.test.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.util.toLogString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class AndroidTileBitmapTest {

    @Test
    fun testConstructor() {
        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)

        AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL).apply {
            assertSame(bitmap1, bitmap)
            assertEquals("bitmap1", key)
            assertEquals(bitmapFrom, BitmapFrom.LOCAL)
        }
        AndroidTileBitmap(bitmap2, "bitmap2", BitmapFrom.MEMORY_CACHE).apply {
            assertSame(bitmap2, bitmap)
            assertEquals("bitmap2", key)
            assertEquals(bitmapFrom, BitmapFrom.MEMORY_CACHE)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap12 = Bitmap.createBitmap(1101, 703, Bitmap.Config.RGB_565)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)

        val androidTileBitmap1 = AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL).apply {
            assertEquals(bitmap1.width, width)
            assertEquals(bitmap1.height, height)
            assertEquals(bitmap1.byteCount.toLong(), byteCount)
        }
        val androidTileBitmap12 = AndroidTileBitmap(bitmap12, "bitmap12", BitmapFrom.LOCAL).apply {
            assertEquals(bitmap12.width, width)
            assertEquals(bitmap12.height, height)
            assertEquals(bitmap12.byteCount.toLong(), byteCount)
        }
        val androidTileBitmap2 = AndroidTileBitmap(bitmap2, "bitmap2", BitmapFrom.LOCAL).apply {
            assertEquals(bitmap2.width, width)
            assertEquals(bitmap2.height, height)
            assertEquals(bitmap2.byteCount.toLong(), byteCount)
        }

        assertEquals(androidTileBitmap1.width, androidTileBitmap12.width)
        assertEquals(androidTileBitmap1.height, androidTileBitmap12.height)
        assertEquals(androidTileBitmap1.byteCount, androidTileBitmap12.byteCount)

        assertEquals(androidTileBitmap1.width, androidTileBitmap2.width)
        assertEquals(androidTileBitmap1.height, androidTileBitmap2.height)
        assertEquals(androidTileBitmap1.byteCount, androidTileBitmap2.byteCount)

        assertEquals(androidTileBitmap12.width, androidTileBitmap2.width)
        assertEquals(androidTileBitmap12.height, androidTileBitmap2.height)
        assertEquals(androidTileBitmap12.byteCount, androidTileBitmap2.byteCount)
    }

    @Test
    fun testRecycle() {
        val bitmap = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val androidTileBitmap = AndroidTileBitmap(bitmap, "bitmap1", BitmapFrom.LOCAL)
        assertEquals(false, androidTileBitmap.isRecycled)
        androidTileBitmap.recycle()
        assertEquals(true, androidTileBitmap.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)

        val tileBitmap1 = AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL)
        val tileBitmap12 = AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL)
        val tileBitmap2 = AndroidTileBitmap(bitmap2, "bitmap2", BitmapFrom.LOCAL)
        val tileBitmap3 = AndroidTileBitmap(bitmap1, "bitmap3", BitmapFrom.LOCAL)
        val tileBitmap4 = AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.MEMORY_CACHE)

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
        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)

        val tileBitmap1 = AndroidTileBitmap(bitmap1, "bitmap1", BitmapFrom.LOCAL)
        val tileBitmap2 = AndroidTileBitmap(bitmap2, "bitmap2", BitmapFrom.MEMORY_CACHE)

        assertEquals(
            expected = "AndroidTileBitmap(key='bitmap1', bitmap=${bitmap1.toLogString()}, bitmapFrom=LOCAL)",
            actual = tileBitmap1.toString()
        )
        assertEquals(
            expected = "AndroidTileBitmap(key='bitmap2', bitmap=${bitmap2.toLogString()}, bitmapFrom=MEMORY_CACHE)",
            actual = tileBitmap2.toString()
        )
    }
}