package com.github.panpf.zoomimage.core.macos.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.defaultRegionDecoders
import kotlin.test.Test
import kotlin.test.assertEquals

class DecodesMacosTest {

    @Test
    fun testDefaultRegionDecoders() {
        assertEquals(
            expected = listOf(SkiaRegionDecoder.Factory()),
            actual = defaultRegionDecoders()
        )
    }
}
