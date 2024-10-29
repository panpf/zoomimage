package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileImageCacheSpec
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class TileImageCacheSpecTest {

    @Test
    fun test() {
        TileImageCacheSpec().apply {
            assertFalse(disabled)
            assertNull(tileImageCache)
        }
    }
}