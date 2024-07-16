package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageInfoTest {

    @Test
    fun testToString() {
        assertEquals(
            "ImageInfo(size=100x1200, mimeType='image/jpeg')",
            ImageInfo(100, 1200, "image/jpeg").toString()
        )

        assertEquals(
            "ImageInfo(size=1200x100, mimeType='image/png')",
            ImageInfo(1200, 100, "image/png").toString()
        )
    }

    @Test
    fun testToShortString() {
        assertEquals(
            "(100x1200,'image/jpeg')",
            ImageInfo(100, 1200, "image/jpeg").toShortString()
        )

        assertEquals(
            "(1200x100,'image/png')",
            ImageInfo(1200, 100, "image/png").toShortString()
        )
    }
}