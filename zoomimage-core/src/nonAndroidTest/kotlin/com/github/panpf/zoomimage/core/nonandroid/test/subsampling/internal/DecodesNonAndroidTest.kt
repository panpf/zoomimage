package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.defaultRegionDecoder
import kotlin.test.Test
import kotlin.test.assertTrue

class DecodesNonAndroidTest {

    @Test
    fun testDefaultRegionDecoder() {
        val factory = defaultRegionDecoder()
        assertTrue(factory is SkiaRegionDecoder.Factory)
    }
}