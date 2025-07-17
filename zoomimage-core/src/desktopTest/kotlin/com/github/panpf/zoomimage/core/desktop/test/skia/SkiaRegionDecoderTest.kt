package com.github.panpf.zoomimage.core.desktop.test.skia

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SkiaRegionDecoderTest {

    @Test
    fun testDecodeRegion() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val imageSource = imageFile.toImageSource()
        var decoder: SkiaRegionDecoder? = null
        try {
            decoder = SkiaRegionDecoder(SubsamplingImage(imageSource), imageSource)
            val srcRect = IntRectCompat(
                left = imageSize.width - tileSize.width,
                top = imageSize.height - tileSize.height,
                right = imageSize.width,
                bottom = imageSize.height
            )
            val bitmap = decoder.decodeRegion(srcRect, 1)
            val color = bitmap.getColor(bitmap.width - 1, bitmap.height - 1)
            assertEquals(tileSize.width, bitmap.width)
            assertEquals(tileSize.height, bitmap.height)
            assertNotEquals(0, Color.getR(color))
            assertNotEquals(0, Color.getG(color))
            assertNotEquals(0, Color.getB(color))
        } finally {
            decoder?.close()
        }
    }

    @Test
    fun testDecodeRegionOutOfRange1() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val imageSource = imageFile.toImageSource()
        var decoder: SkiaRegionDecoder? = null
        try {
            decoder = SkiaRegionDecoder(SubsamplingImage(imageSource), imageSource)
            val srcRect = IntRectCompat(
                left = imageSize.width - tileSize.width,
                top = imageSize.height - tileSize.height,
                right = imageSize.width + 1,
                bottom = imageSize.height + 1
            )
            val bitmap = decoder.decodeRegion(srcRect, 1)
            val color = bitmap.getColor(bitmap.width - 1, bitmap.height - 1)
            assertEquals(tileSize.width, bitmap.width)
            assertEquals(tileSize.height, bitmap.height)
            assertNotEquals(0, Color.getR(color))
            assertNotEquals(0, Color.getG(color))
            assertNotEquals(0, Color.getB(color))
        } finally {
            decoder?.close()
        }
    }

    @Test
    fun testDecodeRegionOutOfRange2() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val imageSource = imageFile.toImageSource()
        var decoder: SkiaRegionDecoder? = null
        try {
            decoder = SkiaRegionDecoder(SubsamplingImage(imageSource), imageSource)
            val srcRect = IntRectCompat(
                imageSize.width - tileSize.width,
                imageSize.height - tileSize.height,
                imageSize.width + 2,
                imageSize.height + 2
            )
            val bitmap = decoder.decodeRegion(srcRect, 1)
            val color1 = bitmap.getColor(bitmap.width - 1, bitmap.height - 1)
            val color2 = bitmap.getColor(bitmap.width - 2, bitmap.height - 2)
            assertEquals(tileSize.width, bitmap.width)
            assertEquals(tileSize.height, bitmap.height)
            assertNotEquals(0, Color.getR(color1))
            assertNotEquals(0, Color.getG(color1))
            assertNotEquals(0, Color.getB(color1))
            assertNotEquals(0, Color.getR(color2))
            assertNotEquals(0, Color.getG(color2))
            assertNotEquals(0, Color.getB(color2))
        } finally {
            decoder?.close()
        }
    }
}