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
import android.graphics.Bitmap.Config.ARGB_8888
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.CountBitmap
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.zoomimage.sketch.SketchBitmapTileImage
import com.github.panpf.zoomimage.sketch.SketchTileImageCache
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SketchTileImageCacheTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val tileImageCache = SketchTileImageCache(sketch)

        val key1 = "key1"
        val bitmap1 = Bitmap.createBitmap(100, 100, ARGB_8888)
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
            imageInfo = com.github.panpf.sketch.decode.ImageInfo(
                bitmap1.width,
                bitmap1.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val tileImage1 = BitmapTileImage(bitmap1, key1, fromCache = false)
        val imageInfo1 = ImageInfo(tileImage1.width, tileImage1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, tileImageCache.get(key1))
        tileImageCache.put(key1, tileImage1, imageUrl1, imageInfo1)
        assertEquals(
            expected = SketchBitmapTileImage(
                cacheValue1,
                key1,
                fromCache = true,
                "SketchTileImageCache"
            ),
            actual = tileImageCache.get(key1)
        )

        val key2 = "key2"
        val bitmap2 = Bitmap.createBitmap(200, 200, ARGB_8888)
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
            imageInfo = com.github.panpf.sketch.decode.ImageInfo(
                bitmap2.width,
                bitmap2.height,
                "image/png",
                0
            ),
            transformedList = null,
            extras = null,
        )
        val tileImage2 = BitmapTileImage(bitmap2, key2, fromCache = false)
        val imageInfo2 = ImageInfo(tileImage2.width, tileImage2.height, "image/jpeg")
        val imageUrl2 = "url2"

        assertEquals(null, tileImageCache.get(key2))
        tileImageCache.put(key2, tileImage2, imageUrl2, imageInfo2)
        assertEquals(
            expected = SketchBitmapTileImage(
                cacheValue2,
                key2,
                fromCache = true,
                "SketchTileImageCache"
            ),
            actual = tileImageCache.get(key2)
        )

        assertNotEquals(illegal = tileImageCache.get(key1), actual = tileImageCache.get(key2))
    }
}