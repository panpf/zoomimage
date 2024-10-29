package com.github.panpf.zoomimage.core.glide.test

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.glide.GlideBitmapTileImage
import com.github.panpf.zoomimage.glide.GlideTileImageCache
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GlideTileImageCacheTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }
        val tileImageCache = GlideTileImageCache(glide)

        val key1 = "key1"
        val bitmap1 = Bitmap.createBitmap(100, 100, ARGB_8888)
        val tileImage1 =
            BitmapTileImage(bitmap1, key1, fromCache = false)
        val imageInfo1 = ImageInfo(tileImage1.width, tileImage1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, tileImageCache.get(key1))
        tileImageCache.put(key1, tileImage1, imageUrl1, imageInfo1)
        assertEquals(
            expected = GlideBitmapTileImage(
                resource = EngineResourceWrapper(
                    glideEngine.newEngineResource(
                        bitmap1,
                        newEngineKey(key1)
                    )
                ),
                key = key1,
                fromCache = true
            ),
            actual = tileImageCache.get(key1)
        )

        val key2 = "key2"
        val bitmap2 = Bitmap.createBitmap(200, 200, ARGB_8888)
        val tileImage2 =
            BitmapTileImage(bitmap2, key2, fromCache = false)
        val imageInfo2 = ImageInfo(tileImage2.width, tileImage2.height, "image/jpeg")
        val imageUrl2 = "url2"

        assertEquals(null, tileImageCache.get(key2))
        tileImageCache.put(key2, tileImage2, imageUrl2, imageInfo2)
        assertEquals(
            expected = GlideBitmapTileImage(
                resource = EngineResourceWrapper(
                    glideEngine.newEngineResource(
                        bitmap2,
                        newEngineKey(key2)
                    )
                ),
                key = key2,
                fromCache = true
            ),
            actual = tileImageCache.get(key2)
        )

        assertNotEquals(illegal = tileImageCache.get(key1), actual = tileImageCache.get(key2))
    }
}