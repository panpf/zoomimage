package com.github.panpf.zoomimage.core.android.test.subsampling

import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.Config.RGB_565
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.test.copyWith
import com.github.panpf.zoomimage.test.createBitmap
import com.github.panpf.zoomimage.test.decode
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.github.panpf.zoomimage.subsampling.byteCount as expectByteCount
import com.github.panpf.zoomimage.subsampling.height as expectHeight
import com.github.panpf.zoomimage.subsampling.isImmutable as expectIsImmutable
import com.github.panpf.zoomimage.subsampling.isMutable as expectIsMutable
import com.github.panpf.zoomimage.subsampling.isRecycled as expectIsRecycled
import com.github.panpf.zoomimage.subsampling.recycle as expectRecycle
import com.github.panpf.zoomimage.subsampling.size as expectSize
import com.github.panpf.zoomimage.subsampling.width as expectWidth

class TileBitmapAndroidTest {

    @Test
    fun testBitmapTypealias() {
        assertEquals(
            expected = android.graphics.Bitmap::class,
            actual = com.github.panpf.zoomimage.subsampling.TileBitmap::class
        )
    }

    @Test
    fun testWidth() {
        assertEquals(expected = 100, actual = createBitmap(100, 200).expectWidth)
        assertEquals(expected = 200, actual = createBitmap(200, 100).expectWidth)
    }

    @Test
    fun testHeight() {
        assertEquals(expected = 200, actual = createBitmap(100, 200).expectHeight)
        assertEquals(expected = 100, actual = createBitmap(200, 100).expectHeight)
    }

    @Test
    fun testSize() {
        assertEquals(expected = IntSizeCompat(100, 200), actual = createBitmap(100, 200).expectSize)
        assertEquals(expected = IntSizeCompat(200, 100), actual = createBitmap(200, 100).expectSize)
    }

    @Test
    fun testByteCount() {
        assertEquals(expected = 80000, actual = createBitmap(100, 200, ARGB_8888).expectByteCount)
        assertEquals(expected = 40000, actual = createBitmap(200, 100, RGB_565).expectByteCount)
    }

    @Test
    fun testIsMutable() = runTest {
        assertTrue(ComposeResImageFiles.dog.decode().copyWith(isMutable = true).expectIsMutable)
        assertFalse(ComposeResImageFiles.dog.decode().expectIsMutable)
    }

    @Test
    fun testIsImmutable() = runTest {
        assertFalse(ComposeResImageFiles.dog.decode().copyWith(isMutable = true).expectIsImmutable)
        assertTrue(ComposeResImageFiles.dog.decode().expectIsImmutable)
    }

    @Test
    fun testIsRecycled() {
        val bitmap = createBitmap(100, 200)
        assertFalse(bitmap.expectIsRecycled)
        bitmap.expectRecycle()
        assertTrue(bitmap.expectIsRecycled)
    }

    @Test
    fun testRecycle() {
        val bitmap = createBitmap(100, 200)
        assertFalse(bitmap.expectIsRecycled)
        bitmap.expectRecycle()
        assertTrue(bitmap.expectIsRecycled)
    }
}