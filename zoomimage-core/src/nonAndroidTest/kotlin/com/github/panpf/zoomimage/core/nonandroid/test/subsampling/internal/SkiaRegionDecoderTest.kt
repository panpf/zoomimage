package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.subsampling.toFactory
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.TestImageSource
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.hammingDistance
import com.github.panpf.zoomimage.test.produceFingerPrint
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SkiaRegionDecoderTest {

    @Test
    fun testFactoryAccept() = runTest {
        val factory = SkiaRegionDecoder.Factory()
        assertEquals(true, factory.accept(SubsamplingImage(TestImageSource())))
    }

    @Test
    fun testFactoryCheckSupport() {
        val factory = SkiaRegionDecoder.Factory()
        assertEquals(true, factory.checkSupport("image/jpeg"))
        assertEquals(true, factory.checkSupport("image/png"))
        assertEquals(true, factory.checkSupport("image/webp"))
        assertEquals(true, factory.checkSupport("image/bmp"))
        assertEquals(true, factory.checkSupport("image/gif"))
        assertEquals(false, factory.checkSupport("image/heic"))
        assertEquals(false, factory.checkSupport("image/heif"))
        assertEquals(false, factory.checkSupport("image/avif"))
        assertEquals(false, factory.checkSupport("video/mp4"))
        assertEquals(null, factory.checkSupport("image/fake"))
    }

    @Test
    fun testFactoryCreate() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        val imageSource1 = TestImageSource()
        val imageSource2 = TestImageSource()
        val subsamplingImage1 = SubsamplingImage(imageSource1)
        val subsamplingImage2 = SubsamplingImage(imageSource2)

        SkiaRegionDecoder.Factory()
            .create(subsamplingImage1, imageSource1)
            .apply {
                assertSame(imageSource1, imageSource)
                assertSame(subsamplingImage1, subsamplingImage)
            }

        SkiaRegionDecoder.Factory()
            .create(subsamplingImage2, imageSource2)
            .apply {
                assertSame(imageSource2, imageSource)
                assertSame(subsamplingImage2, subsamplingImage)
            }

        val imageFile = ResourceImages.exifRotate90
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder.Factory().create(SubsamplingImage(imageSource), imageSource).apply {
            assertEquals(expected = imageFile.size, actual = imageInfo.size)
            assertEquals(expected = "image/jpeg", actual = imageInfo.mimeType)
        }

        SkiaRegionDecoder.Factory().create(
            SubsamplingImage(imageSource, ImageInfo(imageFile.size * 2, "image/png")),
            imageSource
        ).apply {
            assertEquals(expected = imageFile.size * 2, actual = imageInfo.size)
            assertEquals(expected = "image/png", actual = imageInfo.mimeType)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val element1 = SkiaRegionDecoder.Factory()
        val element11 = SkiaRegionDecoder.Factory()
        val element2 = SkiaRegionDecoder.Factory()

        assertEquals(element1, element11)
        assertEquals(element1, element2)

        assertEquals(element1.hashCode(), element11.hashCode())
        assertEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        val element = SkiaRegionDecoder.Factory()
        assertEquals(expected = "SkiaRegionDecoder", actual = element.toString())
    }

    @Test
    fun testImageInfo() {
        val imageFile = ResourceImages.exifRotate90
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder(SubsamplingImage(imageSource), imageSource).apply {
            assertEquals(expected = imageFile.size, actual = imageInfo.size)
            assertEquals(expected = "image/jpeg", actual = imageInfo.mimeType)
        }

        SkiaRegionDecoder(
            SubsamplingImage(
                imageSource,
                ImageInfo(imageFile.size * 2, "image/png")
            ), imageSource
        ).apply {
            assertEquals(expected = imageFile.size * 2, actual = imageInfo.size)
            assertEquals(expected = "image/png", actual = imageInfo.mimeType)
        }
    }

    @Test
    fun testPrepare() {
        val imageFile = ResourceImages.exifRotate90
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder(SubsamplingImage(imageSource), imageSource).use {
            it.prepare()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            SkiaRegionDecoder(SubsamplingImage(TestImageSource()), TestImageSource()).use {
                it.prepare()
            }
        }
    }

    @Test
    fun testDecodeRegion() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        val imageSource1 = ResourceImages.exifNormal.toImageSource()
        val bitmap11: Bitmap
        SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(imageSource1.toFactory()), imageSource1).use { decodeHelper1 ->
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
            }

        val imageSource2 = ResourceImages.exifRotate90.toImageSource()
        val bitmap2: Bitmap
        SkiaRegionDecoder.Factory()
            .create(SubsamplingImage(imageSource2.toFactory()), imageSource2)
            .use { tileDecoder2 ->
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
            }
        val bitmapFinger = bitmap11.produceFingerPrint()
        val bitmap2Finger = bitmap2.produceFingerPrint()
        val hanming2 = hammingDistance(bitmapFinger, bitmap2Finger)
        assertTrue(hanming2 <= 2)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val imageSource1 = TestImageSource()
        val imageSource2 = TestImageSource()
        val element1 = SkiaRegionDecoder(SubsamplingImage(imageSource1), imageSource1)
        val element11 = SkiaRegionDecoder(SubsamplingImage(imageSource1), imageSource1)
        val element2 = SkiaRegionDecoder(SubsamplingImage(imageSource2), imageSource1)
        val element3 = SkiaRegionDecoder(SubsamplingImage(imageSource1), imageSource2)

        assertEquals(element1, element11)
        assertNotEquals(element1, element2)
        assertNotEquals(element1, element3)
        assertNotEquals(element2, element3)

        assertEquals(element1.hashCode(), element11.hashCode())
        assertNotEquals(element1.hashCode(), element2.hashCode())
        assertNotEquals(element1.hashCode(), element3.hashCode())
        assertNotEquals(element2.hashCode(), element3.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val imageSource = TestImageSource()
        val subsamplingImage = SubsamplingImage(imageSource)
        val element = SkiaRegionDecoder(subsamplingImage, imageSource)
        assertEquals(
            "SkiaRegionDecoder(subsamplingImage=$subsamplingImage, imageSource=$imageSource)",
            element.toString()
        )
    }
}