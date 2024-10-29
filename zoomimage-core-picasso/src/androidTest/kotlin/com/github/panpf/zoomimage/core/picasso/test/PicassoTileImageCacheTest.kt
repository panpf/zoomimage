package com.github.panpf.zoomimage.core.picasso.test

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import com.github.panpf.zoomimage.picasso.PicassoTileImageCache
import com.github.panpf.zoomimage.subsampling.BitmapTileImage
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.squareup.picasso.Picasso
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PicassoTileImageCacheTest {

    @Test
    fun test() {
        val picasso = Picasso.get()
        val tileImageCache = PicassoTileImageCache(picasso)

        val key1 = "key1"
        val bitmap1 = Bitmap.createBitmap(100, 100, ARGB_8888)
        val tileImage1 =
            BitmapTileImage(bitmap1, key1, fromCache = false)
        val imageInfo1 = ImageInfo(tileImage1.width, tileImage1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, tileImageCache.get(key1))
        tileImageCache.put(key1, tileImage1, imageUrl1, imageInfo1)
        assertEquals(
            expected = BitmapTileImage(bitmap1, key1, fromCache = true),
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
            expected = BitmapTileImage(bitmap2, key2, fromCache = true),
            actual = tileImageCache.get(key2)
        )

        assertNotEquals(illegal = tileImageCache.get(key1), actual = tileImageCache.get(key2))
    }
}