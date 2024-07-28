package com.github.panpf.zoomimage.core.sketch.nonandroid.test

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sketch.SketchTileBitmapCache
import com.github.panpf.zoomimage.subsampling.BitmapFrom.LOCAL
import com.github.panpf.zoomimage.subsampling.BitmapFrom.MEMORY_CACHE
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.SkiaTileBitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SketchTileBitmapCacheTest {

    @Test
    fun test() {
        val context = PlatformContext.INSTANCE
        val sketch = Sketch.Builder(context).build()
        try {
            val tileBitmapCache = SketchTileBitmapCache(sketch)

            val key1 = "key1"
            val bitmap1 = SkiaBitmap().apply {
                allocN32Pixels(100, 100, false)
            }
            val tileBitmap1 = SkiaTileBitmap(bitmap1, key1, LOCAL)
            val imageInfo1 = ImageInfo(tileBitmap1.width, tileBitmap1.height, "image/jpeg")
            val imageUrl1 = "url1"

            assertEquals(null, tileBitmapCache.get(key1))
            tileBitmapCache.put(key1, tileBitmap1, imageUrl1, imageInfo1)
            assertEquals(
                expected = SkiaTileBitmap(bitmap1, key1, MEMORY_CACHE),
                actual = tileBitmapCache.get(key1)
            )

            val key2 = "key2"
            val bitmap2 = SkiaBitmap().apply {
                allocN32Pixels(200, 200, false)
            }
            val tileBitmap2 = SkiaTileBitmap(bitmap2, key2, LOCAL)
            val imageInfo2 = ImageInfo(tileBitmap2.width, tileBitmap2.height, "image/jpeg")
            val imageUrl2 = "url2"

            assertEquals(null, tileBitmapCache.get(key2))
            tileBitmapCache.put(key2, tileBitmap2, imageUrl2, imageInfo2)
            assertEquals(
                expected = SkiaTileBitmap(bitmap2, key2, MEMORY_CACHE),
                actual = tileBitmapCache.get(key2)
            )

            assertNotEquals(illegal = tileBitmapCache.get(key1), actual = tileBitmapCache.get(key2))
        } finally {
            sketch.shutdown()
        }
    }
}