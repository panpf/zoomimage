package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.test.TestImageSource
import kotlin.test.Test
import kotlin.test.assertEquals

class SubsamplingImageTest {

    @Test
    fun testKey() {
        assertEquals(
            expected = "TestImageSource&imageInfo=null",
            actual = SubsamplingImage(TestImageSource()).key
        )

        assertEquals(
            expected = "TestImageSource&imageInfo=ImageInfo(size=101x202, mimeType='image/jpeg')",
            actual = SubsamplingImage(TestImageSource(), ImageInfo(101, 202, "image/jpeg")).key
        )
    }
}