package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

class TileBitmapCacheSpecTest {

    @Test
    fun test() {
        TileBitmapCacheSpec().apply {
            assertFalse(disabled)
            assertNull(tileBitmapCache)
        }
    }
}