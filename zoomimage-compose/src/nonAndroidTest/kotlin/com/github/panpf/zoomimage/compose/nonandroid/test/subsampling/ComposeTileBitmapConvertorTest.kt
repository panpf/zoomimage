package com.github.panpf.zoomimage.compose.nonandroid.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.ComposeSkiaTileBitmap
import com.github.panpf.zoomimage.compose.subsampling.ComposeTileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import com.github.panpf.zoomimage.test.TestTileBitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposeTileBitmapConvertorTest {

    @Test
    fun test() = runTest {
        val convertor = ComposeTileBitmapConvertor()
        val bitmap = SkiaBitmap().apply {
            allocN32Pixels(1101, 703, opaque = false)
        }
        val androidTileBitmap = SkiaTileBitmap(bitmap, "bitmap1", BitmapFrom.LOCAL)
        val tileBitmap = convertor.convert(androidTileBitmap)
        assertEquals(true, tileBitmap is ComposeSkiaTileBitmap)

        val testTileBitmap = TestTileBitmap("Test1")
        assertFailsWith(ClassCastException::class) {
            convertor.convert(testTileBitmap)
        }
    }
}