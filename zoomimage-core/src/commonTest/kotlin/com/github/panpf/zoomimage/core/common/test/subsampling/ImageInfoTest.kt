package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageInfoTest {

    @Test
    fun testConstructor() {
        ImageInfo(IntSizeCompat(100, 1200), "image/jpeg").apply {
            assertEquals(100, width)
            assertEquals(1200, height)
            assertEquals(IntSizeCompat(100, 1200), size)
            assertEquals("image/jpeg", mimeType)
        }

        ImageInfo(200, 3200, "image/png").apply {
            assertEquals(200, width)
            assertEquals(3200, height)
            assertEquals(IntSizeCompat(200, 3200), size)
            assertEquals("image/png", mimeType)
        }
    }

    @Test
    fun testCopy() {
        ImageInfo(IntSizeCompat(100, 1200), "image/jpeg").apply {
            assertEquals(100, width)
            assertEquals(1200, height)
            assertEquals(IntSizeCompat(100, 1200), size)
            assertEquals("image/jpeg", mimeType)
        }.copy().apply {
            assertEquals(100, width)
            assertEquals(1200, height)
            assertEquals(IntSizeCompat(100, 1200), size)
            assertEquals("image/jpeg", mimeType)
        }.copy(width = 150).apply {
            assertEquals(150, width)
            assertEquals(1200, height)
            assertEquals(IntSizeCompat(150, 1200), size)
            assertEquals("image/jpeg", mimeType)
        }.copy(height = 1300).apply {
            assertEquals(150, width)
            assertEquals(1300, height)
            assertEquals(IntSizeCompat(150, 1300), size)
            assertEquals("image/jpeg", mimeType)
        }.copy(mimeType = "image/png").apply {
            assertEquals(150, width)
            assertEquals(1300, height)
            assertEquals(IntSizeCompat(150, 1300), size)
            assertEquals("image/png", mimeType)
        }.copy(width = 200, height = 3200, mimeType = "image/webp").apply {
            assertEquals(200, width)
            assertEquals(3200, height)
            assertEquals(IntSizeCompat(200, 3200), size)
            assertEquals("image/webp", mimeType)
        }
    }

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