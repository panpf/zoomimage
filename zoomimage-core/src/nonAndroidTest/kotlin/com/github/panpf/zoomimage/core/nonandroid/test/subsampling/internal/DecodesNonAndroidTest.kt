package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.internal.SkiaDecodeHelper
import com.github.panpf.zoomimage.subsampling.internal.checkSupportSubsamplingByMimeType
import com.github.panpf.zoomimage.subsampling.internal.createDecodeHelper
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.toImageSource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecodesNonAndroidTest {

    @Test
    fun testCreateDecodeHelper() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val decodeHelper = createDecodeHelper(ResourceImages.dog.toImageSource())
        assertTrue(decodeHelper is SkiaDecodeHelper)
    }

    @Test
    fun testCheckSupportSubsamplingByMimeType() {
        assertTrue(checkSupportSubsamplingByMimeType("image/heic"))
        assertTrue(checkSupportSubsamplingByMimeType("image/heif"))
        assertTrue(checkSupportSubsamplingByMimeType("image/bmp"))
        assertFalse(checkSupportSubsamplingByMimeType("image/gif"))
        assertTrue(checkSupportSubsamplingByMimeType("image/jpeg"))
        assertTrue(checkSupportSubsamplingByMimeType("image/png"))
        assertTrue(checkSupportSubsamplingByMimeType("image/webp"))
    }
}