package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.subsampling.internal.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.createDecodeHelper
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TileDecoderTest {

    @Test
    fun test() {
        val logger = Logger("Test")
        val imageSource = ResourceImages.hugeCard.toImageSource()
        val decodeHelper = createDecodeHelper(imageSource)
        val scope = CoroutineScope(Dispatchers.Main)
        val dispatcher = Dispatchers.IO.limitedParallelism(3)
        val tileDecoder = TileDecoder(logger, imageSource, decodeHelper)
        tileDecoder.use { tileDecoder ->
            assertEquals(1, tileDecoder.decoderPoolSize)

            val results = runBlocking {
                val jobs = mutableListOf<Deferred<TileBitmap?>>()
                repeat(20) {
                    val job = scope.async {
                        withContext(dispatcher) {
                            tileDecoder.decode("test", IntRectCompat(100, 100, 300, 300), 1)
                        }
                    }
                    jobs.add(job)
                }
                jobs.awaitAll()
            }
            assertEquals(expected = 20, actual = results.size)
            assertEquals(expected = true, actual = results.all { it != null })
            assertEquals(3, tileDecoder.decoderPoolSize)
        }

        assertFailsWith(IllegalStateException::class) {
            tileDecoder.decode("test", IntRectCompat(100, 100, 300, 300), 1)
        }
    }
}