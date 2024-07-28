/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.core.sketch3.test

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.zoomimage.sketch.SketchTileBitmap
import com.github.panpf.zoomimage.sketch.internal.toLogString
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SketchTileBitmapTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val countBitmap1 = CountBitmap(
            cacheKey = "key1",
            originBitmap = bitmap1,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue1 = MemoryCache.Value(
            countBitmap = countBitmap1,
            imageUri = "imageUrl1",
            requestKey = "requestKey1",
            requestCacheKey = "requestCacheKey1",
            imageInfo = ImageInfo(
                bitmap1.width,
                bitmap1.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val countBitmap2 = CountBitmap(
            cacheKey = "key2",
            originBitmap = bitmap2,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue2 = MemoryCache.Value(
            countBitmap = countBitmap2,
            imageUri = "imageUrl2",
            requestKey = "requestKey2",
            requestCacheKey = "requestCacheKey2",
            imageInfo = ImageInfo(
                bitmap2.width,
                bitmap2.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        SketchTileBitmap(cacheValue1, "key1", BitmapFrom.LOCAL, "caller1").apply {
            assertSame(bitmap1, bitmap)
            assertEquals("key1", key)
            assertEquals(bitmapFrom, BitmapFrom.LOCAL)
        }
        SketchTileBitmap(cacheValue2, "key2", BitmapFrom.MEMORY_CACHE, "caller1").apply {
            assertSame(bitmap2, bitmap)
            assertEquals("key2", key)
            assertEquals(bitmapFrom, BitmapFrom.MEMORY_CACHE)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val bitmap3 = Bitmap.createBitmap(1101, 703, Bitmap.Config.RGB_565)
        val countBitmap1 = CountBitmap(
            cacheKey = "key1",
            originBitmap = bitmap1,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue1 = MemoryCache.Value(
            countBitmap = countBitmap1,
            imageUri = "imageUrl1",
            requestKey = "requestKey1",
            requestCacheKey = "requestCacheKey1",
            imageInfo = ImageInfo(
                bitmap1.width,
                bitmap1.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val countBitmap2 = CountBitmap(
            cacheKey = "key2",
            originBitmap = bitmap2,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue2 = MemoryCache.Value(
            countBitmap = countBitmap2,
            imageUri = "imageUrl2",
            requestKey = "requestKey2",
            requestCacheKey = "requestCacheKey2",
            imageInfo = ImageInfo(
                bitmap2.width,
                bitmap2.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val countBitmap3 = CountBitmap(
            cacheKey = "key3",
            originBitmap = bitmap3,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue3 = MemoryCache.Value(
            countBitmap = countBitmap3,
            imageUri = "imageUrl3",
            requestKey = "requestKey3",
            requestCacheKey = "requestCacheKey3",
            imageInfo = ImageInfo(
                bitmap3.width,
                bitmap3.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )

        val tileBitmap1 =
            SketchTileBitmap(cacheValue1, "key1", BitmapFrom.LOCAL, "caller1").apply {
                assertEquals(bitmap1.width, width)
                assertEquals(bitmap1.height, height)
                assertEquals(bitmap1.byteCount.toLong(), byteCount)
            }
        val tileBitmap2 =
            SketchTileBitmap(cacheValue2, "key2", BitmapFrom.LOCAL, "caller2").apply {
                assertEquals(bitmap2.width, width)
                assertEquals(bitmap2.height, height)
                assertEquals(bitmap2.byteCount.toLong(), byteCount)
            }
        val tileBitmap3 =
            SketchTileBitmap(cacheValue3, "key3", BitmapFrom.LOCAL, "caller3").apply {
                assertEquals(bitmap3.width, width)
                assertEquals(bitmap3.height, height)
                assertEquals(bitmap3.byteCount.toLong(), byteCount)
            }

        assertEquals(tileBitmap1.width, tileBitmap3.width)
        assertEquals(tileBitmap1.height, tileBitmap3.height)
        assertNotEquals(tileBitmap1.byteCount, tileBitmap3.byteCount)

        assertNotEquals(tileBitmap1.width, tileBitmap2.width)
        assertNotEquals(tileBitmap1.height, tileBitmap2.height)
        assertNotEquals(tileBitmap1.byteCount, tileBitmap2.byteCount)

        assertNotEquals(tileBitmap2.width, tileBitmap3.width)
        assertNotEquals(tileBitmap2.height, tileBitmap3.height)
        assertNotEquals(tileBitmap2.byteCount, tileBitmap3.byteCount)
    }

    @Test
    fun testRecycle() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()

        val bitmap = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val countBitmap = CountBitmap(
            cacheKey = "key",
            originBitmap = bitmap,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue = MemoryCache.Value(
            countBitmap = countBitmap,
            imageUri = "imageUrl",
            requestKey = "requestKey",
            requestCacheKey = "requestCacheKey",
            imageInfo = ImageInfo(
                bitmap.width,
                bitmap.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val tileBitmap = SketchTileBitmap(cacheValue, "key", BitmapFrom.LOCAL, "caller")
        assertEquals(false, tileBitmap.isRecycled)
        tileBitmap.recycle()
        assertEquals(true, tileBitmap.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val countBitmap1 = CountBitmap(
            cacheKey = "key1",
            originBitmap = bitmap1,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue1 = MemoryCache.Value(
            countBitmap = countBitmap1,
            imageUri = "imageUrl1",
            requestKey = "requestKey1",
            requestCacheKey = "requestCacheKey1",
            imageInfo = ImageInfo(
                bitmap1.width,
                bitmap1.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val countBitmap2 = CountBitmap(
            cacheKey = "key2",
            originBitmap = bitmap2,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue2 = MemoryCache.Value(
            countBitmap = countBitmap2,
            imageUri = "imageUrl2",
            requestKey = "requestKey2",
            requestCacheKey = "requestCacheKey2",
            imageInfo = ImageInfo(
                bitmap2.width,
                bitmap2.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )

        val tileBitmap1 = SketchTileBitmap(cacheValue1, "key1", BitmapFrom.LOCAL, "caller1")
        val tileBitmap12 = SketchTileBitmap(cacheValue1, "key1", BitmapFrom.LOCAL, "caller1")
        val tileBitmap2 = SketchTileBitmap(cacheValue2, "key2", BitmapFrom.LOCAL, "caller2")
        val tileBitmap3 = SketchTileBitmap(cacheValue1, "key3", BitmapFrom.LOCAL, "caller3")
        val tileBitmap4 = SketchTileBitmap(cacheValue1, "key4", BitmapFrom.MEMORY_CACHE, "caller4")

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
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val countBitmap1 = CountBitmap(
            cacheKey = "key1",
            originBitmap = bitmap1,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue1 = MemoryCache.Value(
            countBitmap = countBitmap1,
            imageUri = "imageUrl1",
            requestKey = "requestKey1",
            requestCacheKey = "requestCacheKey1",
            imageInfo = ImageInfo(
                bitmap1.width,
                bitmap1.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val countBitmap2 = CountBitmap(
            cacheKey = "key2",
            originBitmap = bitmap2,
            bitmapPool = sketch.bitmapPool,
            disallowReuseBitmap = false,
        )
        val cacheValue2 = MemoryCache.Value(
            countBitmap = countBitmap2,
            imageUri = "imageUrl2",
            requestKey = "requestKey2",
            requestCacheKey = "requestCacheKey2",
            imageInfo = ImageInfo(
                bitmap2.width,
                bitmap2.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )

        val tileBitmap1 = SketchTileBitmap(cacheValue1, "key1", BitmapFrom.LOCAL, "caller1")
        val tileBitmap2 = SketchTileBitmap(cacheValue2, "key2", BitmapFrom.MEMORY_CACHE, "caller2")

        assertEquals(
            expected = "SketchTileBitmap(key='key1', bitmap=${bitmap1.toLogString()}, bitmapFrom=LOCAL)",
            actual = tileBitmap1.toString()
        )
        assertEquals(
            expected = "SketchTileBitmap(key='key2', bitmap=${bitmap2.toLogString()}, bitmapFrom=MEMORY_CACHE)",
            actual = tileBitmap2.toString()
        )
    }
}