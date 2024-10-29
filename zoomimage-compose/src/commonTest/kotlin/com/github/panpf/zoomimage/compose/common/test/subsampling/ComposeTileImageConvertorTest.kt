package com.github.panpf.zoomimage.compose.common.test.subsampling

import com.github.panpf.zoomimage.compose.subsampling.ComposeTileImage
import com.github.panpf.zoomimage.compose.subsampling.ComposeTileImageConvertor
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.test.TestTileImage
import com.github.panpf.zoomimage.test.createBitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposeTileImageConvertorTest {

    @Test
    fun test() = runTest {
        val convertor = ComposeTileImageConvertor()
        val bitmap = createBitmap(1101, 703)
        val tileImage = BitmapTileImage(bitmap, "bitmap1", fromCache = false)
        val newTileImage = convertor.convert(tileImage)
        assertEquals(true, newTileImage is ComposeTileImage)

        val testTileImage = TestTileImage("Test1")
        assertFailsWith(ClassCastException::class) {
            convertor.convert(testTileImage)
        }
    }
}