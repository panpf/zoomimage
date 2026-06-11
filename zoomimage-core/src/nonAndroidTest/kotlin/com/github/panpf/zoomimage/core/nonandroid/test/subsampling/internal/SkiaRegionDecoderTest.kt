package com.github.panpf.zoomimage.core.nonandroid.test.subsampling.internal

import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.SkiaRegionDecoder
import com.github.panpf.zoomimage.subsampling.size
import com.github.panpf.zoomimage.test.TestImageSource
import com.github.panpf.zoomimage.test.calculateSampledBitmapSize
import com.github.panpf.zoomimage.test.similarity
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SkiaRegionDecoderTest {

    @Test
    fun testFactoryAccept() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to true,
            ComposeResImageFiles.horse to true,
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            assertEquals(
                expected = exceptedOk,
                actual = SkiaRegionDecoder.Factory()
                    .accept(SubsamplingImage(imageSource)),
                message = "Factory should ${if (exceptedOk) "accept" else "not accept"} ${imageFile.name}"
            )
        }
    }

    @Test
    fun testFactoryCheckSupport() {
        listOf(
            "image/jpeg" to true,
            "image/png" to true,
            "image/webp" to true,
            "image/bmp" to true,
            "image/gif" to true,
            "image/svg+xml" to false,
            "image/heic" to false,
            "image/heif" to false,
            "image/avif" to false,
            "video/mp4" to false,
            "image/fake" to null,
        ).forEach { (mimeType, expectedSupport) ->
            assertEquals(
                expected = expectedSupport,
                actual = SkiaRegionDecoder.Factory().checkSupport(mimeType),
                message = "Factory should ${if (expectedSupport == true) "support" else if (expectedSupport == false) "not support" else "return null for"} MIME type $mimeType"
            )
        }
    }

    @Test
    fun testFactoryCreate() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to true,
            ComposeResImageFiles.horse to true,
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            if (exceptedOk) {
                SkiaRegionDecoder.Factory()
                    .create(SubsamplingImage(imageSource), imageSource)
            } else {
                assertFailsWith(
                    exceptionClass = Exception::class,
                    message = "Factory should throw Exception for ${imageFile.name}"
                ) {
                    SkiaRegionDecoder.Factory()
                        .create(SubsamplingImage(imageSource), imageSource)
                }
            }
        }

        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }

        SkiaRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource, ImageInfo(1, 1, "image/fake")),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val element1 = SkiaRegionDecoder.Factory()
        val element2 = SkiaRegionDecoder.Factory()

        assertEquals(element1, element2)
        assertEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        val element = SkiaRegionDecoder.Factory()
        assertEquals(expected = "SkiaRegionDecoder", actual = element.toString())
    }

    @Test
    fun testImageInfo() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to false,
            ComposeResImageFiles.horse to false,
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            val decoder = SkiaRegionDecoder(
                subsamplingImage = SubsamplingImage(imageSource),
                imageSource = imageSource,
            )
            if (exceptedOk) {
                val imageInfo = try {
                    decoder.imageInfo
                } catch (e: Exception) {
                    throw Exception("Decode ImageInfo should succeed: ${imageFile.name}", e)
                }
                assertEquals(
                    expected = imageFile.imageInfo,
                    actual = imageInfo,
                    message = imageFile.name
                )
            } else {
                assertFailsWith(exceptionClass = Exception::class, message = imageFile.name) {
                    decoder.imageInfo
                }
            }
        }

        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource,
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }

        SkiaRegionDecoder(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource,
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.imageInfo)
        }
    }

    @Test
    fun testPrepareAndClose() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
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
    fun testCopy() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        SkiaRegionDecoder(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource,
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
            assertEquals(imageFile.imageInfo, this.copy().imageInfo)
        }

        SkiaRegionDecoder(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource,
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.imageInfo)
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.copy().imageInfo)
        }
    }

    @Test
    fun testDecodeRegion() = runTest {
        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        val fullRegion = IntRectCompat(0, 0, imageFile.size.width, imageFile.size.height)
        val region = IntRectCompat(200, 300, 703, 503)
        SkiaRegionDecoder(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource,
        ).apply {
            assertEquals(
                expected = imageFile.size,
                actual = decodeRegion(sampleSize = 1, region = fullRegion).size
            )

            assertEquals(
                expected = calculateSampledBitmapSize(imageSize = imageFile.size, sampleSize = 2),
                actual = decodeRegion(sampleSize = 2, region = fullRegion).size
            )

            assertEquals(
                expected = calculateSampledBitmapSize(imageSize = imageFile.size, sampleSize = 4),
                actual = decodeRegion(sampleSize = 4, region = fullRegion).size
            )

            val regionSize = IntSizeCompat(region.width, region.height)
            assertEquals(
                expected = regionSize,
                actual = decodeRegion(sampleSize = 1, region = region).size
            )

            assertEquals(
                expected = calculateSampledBitmapSize(imageSize = regionSize, sampleSize = 2),
                actual = decodeRegion(sampleSize = 2, region = region).size
            )

            assertEquals(
                expected = calculateSampledBitmapSize(imageSize = regionSize, sampleSize = 4),
                actual = decodeRegion(sampleSize = 4, region = region).size
            )

            val bitmap1 = decodeRegion(sampleSize = 1, region = region)
            val region2 = region.translate(200, 200)
            val bitmap2 = decodeRegion(sampleSize = 1, region = region2)
            val similarity = bitmap1.similarity(bitmap2)
            assertTrue(
                similarity >= 5,
                "Similarity should be greater than or equal to 5, but was $similarity"
            )
        }

        // test exif orientation
        val bitmap1 = ComposeResImageFiles.exifNormal.let { imageFile ->
            val imageSource = imageFile.toImageSource()
            SkiaRegionDecoder(
                subsamplingImage = SubsamplingImage(imageSource),
                imageSource = imageSource,
            )
        }.decodeRegion(
            region = IntRectCompat(100, 200, 300, 300),
            sampleSize = 1
        )
        val bitmap2 = ComposeResImageFiles.exifRotate90.let { imageFile ->
            val imageSource = imageFile.toImageSource()
            SkiaRegionDecoder(
                subsamplingImage = SubsamplingImage(imageSource),
                imageSource = imageSource,
            )
        }.decodeRegion(
            region = IntRectCompat(100, 200, 300, 300),
            sampleSize = 1
        )
        assertEquals(bitmap1.size, bitmap2.size)
        val similarity = bitmap1.similarity(bitmap2)
        assertTrue(
            similarity <= 2,
            "Similarity should be less than or equal to 2, but was $similarity"
        )
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