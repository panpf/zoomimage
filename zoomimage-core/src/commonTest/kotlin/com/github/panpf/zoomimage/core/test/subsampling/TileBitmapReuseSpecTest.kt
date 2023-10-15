package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import org.junit.Assert
import org.junit.Test

class TileBitmapReuseSpecTest {

    @Test
    fun test() {
        TileBitmapReuseSpec().apply {
            Assert.assertFalse(disabled)
            Assert.assertNull(tileBitmapPool)
        }
    }
}