package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import org.junit.Assert
import org.junit.Test

class ImageInfoTest {

    @Test
    fun testToString() {
        Assert.assertEquals(
            "ImageInfo(size=100x1200, mimeType='image/jpeg')",
            ImageInfo(100, 1200, "image/jpeg").toString()
        )

        Assert.assertEquals(
            "ImageInfo(size=1200x100, mimeType='image/png')",
            ImageInfo(1200, 100, "image/png").toString()
        )
    }

    @Test
    fun testToShortString() {
        Assert.assertEquals(
            "(100x1200,'image/jpeg')",
            ImageInfo(100, 1200, "image/jpeg").toShortString()
        )

        Assert.assertEquals(
            "(1200x100,'image/png')",
            ImageInfo(1200, 100, "image/png").toShortString()
        )
    }
}