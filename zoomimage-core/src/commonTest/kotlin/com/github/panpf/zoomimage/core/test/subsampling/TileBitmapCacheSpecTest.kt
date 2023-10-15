package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import org.junit.Assert
import org.junit.Test

class TileBitmapCacheSpecTest {

    @Test
    fun test() {
        TileBitmapCacheSpec().apply {
            Assert.assertFalse(disabled)
            Assert.assertNull(tileBitmapCache)
        }
    }
}