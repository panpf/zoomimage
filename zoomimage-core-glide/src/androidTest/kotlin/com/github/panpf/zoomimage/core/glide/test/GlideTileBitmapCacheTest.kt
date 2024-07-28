package com.github.panpf.zoomimage.core.glide.test

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.glide.GlideTileBitmap
import com.github.panpf.zoomimage.glide.GlideTileBitmapCache
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class GlideTileBitmapCacheTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }
        val tileBitmapCache = GlideTileBitmapCache(glide)

        val key1 = "key1"
        val bitmap1 = Bitmap.createBitmap(100, 100, ARGB_8888)
        val tileBitmap1 =
            AndroidTileBitmap(bitmap1, key1, BitmapFrom.LOCAL)
        val imageInfo1 = ImageInfo(tileBitmap1.width, tileBitmap1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, tileBitmapCache.get(key1))
        tileBitmapCache.put(key1, tileBitmap1, imageUrl1, imageInfo1)
        assertEquals(
            expected = GlideTileBitmap(
                EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey(key1))),
                key1,
                BitmapFrom.MEMORY_CACHE
            ),
            actual = tileBitmapCache.get(key1)
        )

        val key2 = "key2"
        val bitmap2 = Bitmap.createBitmap(200, 200, ARGB_8888)
        val tileBitmap2 =
            AndroidTileBitmap(bitmap2, key2, BitmapFrom.LOCAL)
        val imageInfo2 = ImageInfo(tileBitmap2.width, tileBitmap2.height, "image/jpeg")
        val imageUrl2 = "url2"

        assertEquals(null, tileBitmapCache.get(key2))
        tileBitmapCache.put(key2, tileBitmap2, imageUrl2, imageInfo2)
        assertEquals(
            expected = GlideTileBitmap(
                EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey(key2))),
                key2,
                BitmapFrom.MEMORY_CACHE
            ),
            actual = tileBitmapCache.get(key2)
        )

        assertNotEquals(illegal = tileBitmapCache.get(key1), actual = tileBitmapCache.get(key2))
    }
}