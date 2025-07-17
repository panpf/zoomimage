package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.internal.TileImageCacheHelper
import com.github.panpf.zoomimage.test.TestTileImage
import com.github.panpf.zoomimage.test.TestTileImageCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TileImageCacheHelperTest {

    @Test
    fun test() {
        val helper = TileImageCacheHelper()
        assertEquals(false, helper.disabled)
        assertEquals(null, helper.tileImageCache)

        val key1 = "key1"
        val tileImage1 = TestTileImage()
        val imageInfo1 = ImageInfo(tileImage1.width, tileImage1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, helper.get(key1))
        helper.put(key1, tileImage1, imageUrl1, imageInfo1)
        assertEquals(null, helper.get(key1))

        helper.tileImageCache = TestTileImageCache()
        assertNotNull(helper.tileImageCache)

        helper.put(key1, tileImage1, imageUrl1, imageInfo1)
        assertEquals(tileImage1, helper.get(key1))

        helper.disabled = true
        assertEquals(true, helper.disabled)

        assertEquals(null, helper.get(key1))

        val key2 = "key2"
        val tileImage2 = TestTileImage(width = 200, height = 200)
        val imageInfo2 = ImageInfo(tileImage2.width, tileImage2.height, "image/jpeg")
        val imageUrl2 = "url2"

        helper.put(key2, tileImage2, imageUrl2, imageInfo2)
        assertEquals(null, helper.get(key2))

        helper.disabled = false
        assertEquals(false, helper.disabled)

        assertEquals(null, helper.get(key2))
        helper.put(key2, tileImage2, imageUrl2, imageInfo2)
        assertEquals(tileImage2, helper.get(key2))

        assertNotEquals(helper.get(key1), helper.get(key2))
    }
}