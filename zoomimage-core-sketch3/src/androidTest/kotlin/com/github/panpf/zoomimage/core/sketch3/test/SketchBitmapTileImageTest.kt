/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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
import com.github.panpf.zoomimage.sketch.SketchBitmapTileImage
import com.github.panpf.zoomimage.sketch.internal.toLogString
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class SketchBitmapTileImageTest {

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
        SketchBitmapTileImage(cacheValue1).apply {
            assertSame(bitmap1, bitmap)
        }
        SketchBitmapTileImage(cacheValue2).apply {
            assertSame(bitmap2, bitmap)
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

        val tileImage1 = SketchBitmapTileImage(cacheValue1).apply {
            assertEquals(bitmap1.width, width)
            assertEquals(bitmap1.height, height)
            assertEquals(bitmap1.byteCount.toLong(), byteCount)
        }
        val tileImage2 = SketchBitmapTileImage(cacheValue2).apply {
            assertEquals(bitmap2.width, width)
            assertEquals(bitmap2.height, height)
            assertEquals(bitmap2.byteCount.toLong(), byteCount)
        }
        val tileImage3 = SketchBitmapTileImage(cacheValue3).apply {
            assertEquals(bitmap3.width, width)
            assertEquals(bitmap3.height, height)
            assertEquals(bitmap3.byteCount.toLong(), byteCount)
        }

        assertEquals(tileImage1.width, tileImage3.width)
        assertEquals(tileImage1.height, tileImage3.height)
        assertNotEquals(tileImage1.byteCount, tileImage3.byteCount)

        assertNotEquals(tileImage1.width, tileImage2.width)
        assertNotEquals(tileImage1.height, tileImage2.height)
        assertNotEquals(tileImage1.byteCount, tileImage2.byteCount)

        assertNotEquals(tileImage2.width, tileImage3.width)
        assertNotEquals(tileImage2.height, tileImage3.height)
        assertNotEquals(tileImage2.byteCount, tileImage3.byteCount)
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
        val tileImage = SketchBitmapTileImage(cacheValue)
        assertEquals(false, tileImage.isRecycled)
        tileImage.recycle()
        Thread.sleep(100)
        assertEquals(true, tileImage.isRecycled)
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

        val tileImage1 = SketchBitmapTileImage(cacheValue1)
        val tileImage12 = SketchBitmapTileImage(cacheValue1)
        val tileImage2 = SketchBitmapTileImage(cacheValue2)

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

        val tileImage1 = SketchBitmapTileImage(cacheValue1)
        val tileImage2 = SketchBitmapTileImage(cacheValue2)

        assertEquals(
            expected = "SketchBitmapTileImage(bitmap=${bitmap1.toLogString()})",
            actual = tileImage1.toString()
        )
        assertEquals(
            expected = "SketchBitmapTileImage(bitmap=${bitmap2.toLogString()})",
            actual = tileImage2.toString()
        )
    }
}