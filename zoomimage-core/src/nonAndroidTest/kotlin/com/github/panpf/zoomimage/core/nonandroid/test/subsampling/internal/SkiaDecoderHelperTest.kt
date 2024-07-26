package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.internal.SkiaDecodeHelper
import com.github.panpf.zoomimage.test.hammingDistance
import com.github.panpf.zoomimage.test.produceFingerPrint
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.toIntSizeCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SkiaDecoderHelperTest {

    @Test
    fun testFactory() {
        val dogImageFile = ResourceImages.dog
        val dogImageSource = dogImageFile.toImageSource()
        SkiaDecodeHelper.Factory().create(dogImageSource).apply {
            assertSame(dogImageSource, imageSource)
            assertEquals(dogImageFile.size.toIntSizeCompat(), imageInfo.size)
            assertEquals(true, supportRegion)
        }

        val animImageFile = ResourceImages.anim
        val animImageSource = animImageFile.toImageSource()
        SkiaDecodeHelper.Factory().create(animImageSource).apply {
            assertSame(animImageSource, imageSource)
            assertEquals(animImageFile.size.toIntSizeCompat(), imageInfo.size)
            assertEquals(false, supportRegion)
        }

        val exifRotate180ImageFile = ResourceImages.exifRotate180
        val exifRotate180ImageSource = exifRotate180ImageFile.toImageSource()
        SkiaDecodeHelper.Factory().create(exifRotate180ImageSource).apply {
            assertSame(exifRotate180ImageSource, imageSource)
            assertEquals(exifRotate180ImageFile.size.toIntSizeCompat(), imageInfo.size)
            assertEquals(true, supportRegion)
        }

        val exifTransposeImageFile = ResourceImages.exifTranspose
        val exifTransposeImageSource = exifTransposeImageFile.toImageSource()
        SkiaDecodeHelper.Factory().create(exifTransposeImageSource).apply {
            assertSame(exifTransposeImageSource, imageSource)
            assertEquals(exifTransposeImageFile.size.toIntSizeCompat(), imageInfo.size)
            assertEquals(true, supportRegion)
        }
    }

    @Test
    fun test() {
        val imageSource1 = ResourceImages.exifNormal.toImageSource()
        val decodeHelper1 = SkiaDecodeHelper.Factory().create(imageSource1)
        val bitmap11: Bitmap
        try {
            bitmap11 = decodeHelper1.decodeRegion(
                key = "",
                region = IntRectCompat(100, 200, 300, 300),
                sampleSize = 1
            ).bitmap
            bitmap11.apply {
                assertEquals(200, width)
                assertEquals(100, height)
            }

            val bitmap12 = decodeHelper1.decodeRegion(
                key = "",
                region = IntRectCompat(100, 200, 300, 300),
                sampleSize = 4
            ).bitmap
            bitmap12.apply {
                assertEquals(50, width)
                assertEquals(25, height)
            }
        } finally {
            decodeHelper1.close()
        }

        val imageSource2 = ResourceImages.exifRotate90.toImageSource()
        val tileDecoder2 = SkiaDecodeHelper.Factory().create(imageSource2)
        val bitmap2: Bitmap
        try {
            bitmap2 = tileDecoder2
                .decodeRegion(
                    key = "",
                    region = IntRectCompat(100, 200, 300, 300),
                    sampleSize = 1
                ).bitmap
            bitmap2.apply {
                assertEquals(200, width)
                assertEquals(100, height)
            }
        } finally {
            tileDecoder2.close()
        }
        val bitmapFinger = produceFingerPrint(bitmap11)
        val bitmap2Finger = produceFingerPrint(bitmap2)
        val hanming2 = hammingDistance(bitmapFinger, bitmap2Finger)
        assertTrue(hanming2 <= 2)
    }
}