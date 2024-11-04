package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.hammingDistance
import com.github.panpf.zoomimage.test.produceFingerPrint
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SkiaDecoderHelperTest {

    @Test
    fun testFactory() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val dogImageFile = ResourceImages.dog
        val dogImageSource = dogImageFile.toImageSource()
        SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(dogImageSource.toFactory()), dogImageSource).apply {
            assertSame(dogImageSource, imageSource)
            assertEquals(dogImageFile.size, imageInfo.size)
        }

        val animImageFile = ResourceImages.anim
        val animImageSource = animImageFile.toImageSource()
        SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(animImageSource.toFactory()), animImageSource).apply {
            assertSame(animImageSource, imageSource)
            assertEquals(animImageFile.size, imageInfo.size)
        }

        val exifRotate180ImageFile = ResourceImages.exifRotate180
        val exifRotate180ImageSource = exifRotate180ImageFile.toImageSource()
        SkiaRegionDecoder.Factory().create(
            SubsamplingImage(exifRotate180ImageSource.toFactory()),
            exifRotate180ImageSource
        ).apply {
            assertSame(exifRotate180ImageSource, imageSource)
            assertEquals(exifRotate180ImageFile.size, imageInfo.size)
        }

        val exifTransposeImageFile = ResourceImages.exifTranspose
        val exifTransposeImageSource = exifTransposeImageFile.toImageSource()
        SkiaRegionDecoder.Factory().create(
            SubsamplingImage(exifTransposeImageSource.toFactory()),
            exifTransposeImageSource
        ).apply {
            assertSame(exifTransposeImageSource, imageSource)
            assertEquals(exifTransposeImageFile.size, imageInfo.size)
        }
    }

    @Test
    fun testFactoryCheckSupport() {
        val factory = SkiaRegionDecoder.Factory()
        assertEquals(true, factory.checkSupport("image/jpeg"))
        assertEquals(true, factory.checkSupport("image/png"))
        assertEquals(true, factory.checkSupport("image/webp"))
        assertEquals(true, factory.checkSupport("image/bmp"))
        assertEquals(null, factory.checkSupport("image/heic"))
        assertEquals(null, factory.checkSupport("image/heif"))
        assertEquals(null, factory.checkSupport("image/gif"))
        assertEquals(null, factory.checkSupport("image/avif"))
        assertEquals(null, factory.checkSupport("image/fake"))
    }

    @Test
    fun test() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val imageSource1 = ResourceImages.exifNormal.toImageSource()
        val decodeHelper1 = SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(imageSource1.toFactory()), imageSource1)
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
        val tileDecoder2 = SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(imageSource2.toFactory()), imageSource2)
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

    @Test
    fun testToString() {
        val bitmap = Bitmap().apply {
            allocN32Pixels(width = 100, height = 100, opaque = true)
        }
        val image = Image.makeFromBitmap(bitmap)
        val bytes = image.encodeToData()!!.bytes
        val imageSource = ResourceImages.hugeCard.toImageSource()
        assertEquals(
            expected = "SkiaDecodeHelper(subsamplingImage=SubsamplingImage(imageSource=WrapperFactory(KotlinResourceImageSource('huge_card.jpg')), imageInfo=null), imageInfo=ImageInfo(size=1100x5321, mimeType='image/jpeg'))",
            actual = SkiaRegionDecoder(
                subsamplingImage = SubsamplingImage(imageSource.toFactory()),
                imageSource = imageSource,
                imageInfo = ImageInfo(1100, 5321, "image/jpeg"),
                bytes = bytes,
            ).toString()
        )
    }
}