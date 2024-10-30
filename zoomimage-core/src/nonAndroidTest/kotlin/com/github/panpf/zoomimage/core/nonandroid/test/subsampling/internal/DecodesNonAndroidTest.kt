package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.SkiaDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.createDecodeHelperFactory
import kotlin.test.Test
import kotlin.test.assertTrue

class DecodesNonAndroidTest {

    @Test
    fun testCreateDecodeHelperFactory() {
        val factory = createDecodeHelperFactory()
        assertTrue(factory is SkiaDecodeHelper.Factory)
    }
}