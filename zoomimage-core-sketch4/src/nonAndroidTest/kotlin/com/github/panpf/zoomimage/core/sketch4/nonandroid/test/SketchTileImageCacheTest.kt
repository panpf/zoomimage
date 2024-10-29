package com.github.panpf.zoomimage.core.sketch4.nonandroid.test

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.sketch.Sketch.Builder
import com.github.panpf.zoomimage.sketch.SketchTileImageCache
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SketchTileImageCacheTest {

    @Test
    fun test() {
        val context = PlatformContext.INSTANCE
        val sketch = Builder(context).build()
        try {
            val tileImageCache = SketchTileImageCache(sketch)

            val key1 = "key1"
            val bitmap1 = Bitmap().apply {
                allocN32Pixels(100, 100, false)
            }
            val tileImage1 = BitmapTileImage(bitmap1, key1, fromCache = false)
            val imageInfo1 = ImageInfo(tileImage1.width, tileImage1.height, "image/jpeg")
            val imageUrl1 = "url1"

            assertEquals(null, tileImageCache.get(key1))
            tileImageCache.put(key1, tileImage1, imageUrl1, imageInfo1)
            assertEquals(
                expected = BitmapTileImage(bitmap1, key1, fromCache = true),
                actual = tileImageCache.get(key1)
            )

            val key2 = "key2"
            val bitmap2 = Bitmap().apply {
                allocN32Pixels(200, 200, false)
            }
            val tileImage2 = BitmapTileImage(bitmap2, key2, fromCache = false)
            val imageInfo2 = ImageInfo(tileImage2.width, tileImage2.height, "image/jpeg")
            val imageUrl2 = "url2"

            assertEquals(null, tileImageCache.get(key2))
            tileImageCache.put(key2, tileImage2, imageUrl2, imageInfo2)
            assertEquals(
                expected = BitmapTileImage(bitmap2, key2, fromCache = true),
                actual = tileImageCache.get(key2)
            )

            assertNotEquals(illegal = tileImageCache.get(key1), actual = tileImageCache.get(key2))
        } finally {
            sketch.shutdown()
        }
    }
}