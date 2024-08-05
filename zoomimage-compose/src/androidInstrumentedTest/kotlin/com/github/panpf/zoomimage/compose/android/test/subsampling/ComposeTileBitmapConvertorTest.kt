package com.github.panpf.zoomimage.compose.android.test.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.compose.subsampling.ComposeAndroidTileBitmap
import com.github.panpf.zoomimage.compose.subsampling.ComposeTileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.test.TestTileBitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposeTileBitmapConvertorTest {

    @Test
    fun test() = runTest {
        val convertor = ComposeTileBitmapConvertor()
        val bitmap = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val androidTileBitmap = AndroidTileBitmap(bitmap, "bitmap1", BitmapFrom.LOCAL)
        val tileBitmap = convertor.convert(androidTileBitmap)
        assertEquals(true, tileBitmap is ComposeAndroidTileBitmap)

        val testTileBitmap = TestTileBitmap("Test1")
        assertFailsWith(ClassCastException::class) {
            convertor.convert(testTileBitmap)
        }
    }
}