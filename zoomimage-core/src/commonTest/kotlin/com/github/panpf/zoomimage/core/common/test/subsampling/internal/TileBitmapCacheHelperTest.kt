package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapCacheHelper
import com.github.panpf.zoomimage.test.TestTileBitmap
import com.github.panpf.zoomimage.test.TestTileBitmapCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class TileBitmapCacheHelperTest {

    @Test
    fun test() {
        val helper = TileBitmapCacheHelper(TileBitmapCacheSpec())
        assertEquals(false, helper.tileBitmapCacheSpec.disabled)
        assertEquals(null, helper.tileBitmapCacheSpec.tileBitmapCache)

        val key1 = "key1"
        val tileBitmap1 = TestTileBitmap(key1)
        val imageInfo1 = ImageInfo(tileBitmap1.width, tileBitmap1.height, "image/jpeg")
        val imageUrl1 = "url1"

        assertEquals(null, helper.get(key1))
        helper.put(key1, tileBitmap1, imageUrl1, imageInfo1)
        assertEquals(null, helper.get(key1))

        helper.tileBitmapCacheSpec.tileBitmapCache = TestTileBitmapCache()
        assertNotNull(helper.tileBitmapCacheSpec.tileBitmapCache)

        helper.put(key1, tileBitmap1, imageUrl1, imageInfo1)
        assertEquals(tileBitmap1, helper.get(key1))

        helper.tileBitmapCacheSpec.disabled = true
        assertEquals(true, helper.tileBitmapCacheSpec.disabled)

        assertEquals(null, helper.get(key1))

        val key2 = "key2"
        val tileBitmap2 = TestTileBitmap(key2, bitmapWidth = 200, bitmapHeight = 200)
        val imageInfo2 = ImageInfo(tileBitmap2.width, tileBitmap2.height, "image/jpeg")
        val imageUrl2 = "url2"

        helper.put(key2, tileBitmap2, imageUrl2, imageInfo2)
        assertEquals(null, helper.get(key2))

        helper.tileBitmapCacheSpec.disabled = false
        assertEquals(false, helper.tileBitmapCacheSpec.disabled)

        assertEquals(null, helper.get(key2))
        helper.put(key2, tileBitmap2, imageUrl2, imageInfo2)
        assertEquals(tileBitmap2, helper.get(key2))

        assertNotEquals(helper.get(key1), helper.get(key2))
    }
}