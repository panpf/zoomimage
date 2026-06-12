package com.github.panpf.zoomimage.core.ios.test.subsampling.internal

import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.UIImageRegionDecoder
import com.github.panpf.zoomimage.subsampling.size
import com.github.panpf.zoomimage.test.TestImageSource
import com.github.panpf.zoomimage.test.calculateSampledBitmapSize
import com.github.panpf.zoomimage.test.similarity
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.isVersionAtLeast
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UIImageRegionDecoderTest {

    @Test
    fun testFactoryConstructor() {
        UIImageRegionDecoder.Factory()
    }

    @Test
    fun testFactoryAccept() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to isVersionAtLeast(11),
            ComposeResImageFiles.horse to isVersionAtLeast(16),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            assertEquals(
                expected = exceptedOk,
                actual = UIImageRegionDecoder.Factory()
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
            "image/heic" to isVersionAtLeast(11),
            "image/heif" to isVersionAtLeast(11),
            "image/avif" to isVersionAtLeast(16),
            "video/mp4" to false,
            "image/fake" to null,
        ).forEach { (mimeType, expectedSupport) ->
            assertEquals(
                expected = expectedSupport,
                actual = UIImageRegionDecoder.Factory().checkSupport(mimeType),
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
            ComposeResImageFiles.giraffe to isVersionAtLeast(11),
            ComposeResImageFiles.horse to isVersionAtLeast(16),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            if (exceptedOk) {
                UIImageRegionDecoder.Factory()
                    .create(SubsamplingImage(imageSource), imageSource)
            } else {
                assertFailsWith(
                    exceptionClass = Exception::class,
                    message = "Factory should throw Exception for ${imageFile.name}"
                ) {
                    UIImageRegionDecoder.Factory()
                        .create(SubsamplingImage(imageSource), imageSource)
                }
            }
        }

        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        UIImageRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }

        UIImageRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource, ImageInfo(1, 1, "image/fake")),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val element1 = UIImageRegionDecoder.Factory()
        val element2 = UIImageRegionDecoder.Factory()

        assertEquals(element1, element2)
        assertEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        val element = UIImageRegionDecoder.Factory()
        assertEquals(expected = "UIImageRegionDecoder", actual = element.toString())
    }

    @Test
    fun testSupplementSkiaFactoryAccept() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to false,
            ComposeResImageFiles.anim to false,
            ComposeResImageFiles.exifRotate90 to false,
            ComposeResImageFiles.giraffe to isVersionAtLeast(11),
            ComposeResImageFiles.horse to isVersionAtLeast(16),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            assertEquals(
                expected = exceptedOk,
                actual = UIImageRegionDecoder.SupplementSkiaFactory()
                    .accept(SubsamplingImage(imageSource)),
                message = "Factory should ${if (exceptedOk) "accept" else "not accept"} ${imageFile.name}"
            )
        }
    }

    @Test
    fun testSupplementSkiaFactoryCheckSupport() {
        listOf(
            "image/jpeg" to false,
            "image/png" to false,
            "image/webp" to false,
            "image/bmp" to false,
            "image/gif" to false,
            "image/heic" to isVersionAtLeast(11),
            "image/heif" to isVersionAtLeast(11),
            "image/avif" to isVersionAtLeast(16),
            "video/mp4" to false,
            "image/fake" to false,
        ).forEach { (mimeType, expectedSupport) ->
            assertEquals(
                expected = expectedSupport,
                actual = UIImageRegionDecoder.SupplementSkiaFactory().checkSupport(mimeType),
                message = "Factory should ${if (expectedSupport == true) "support" else if (expectedSupport == false) "not support" else "return null for"} MIME type $mimeType"
            )
        }
    }

    @Test
    fun testSupplementSkiaFactoryCreate() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to false,
            ComposeResImageFiles.anim to false,
            ComposeResImageFiles.exifRotate90 to false,
            ComposeResImageFiles.giraffe to isVersionAtLeast(11),
            ComposeResImageFiles.horse to isVersionAtLeast(16),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            if (exceptedOk) {
                UIImageRegionDecoder.SupplementSkiaFactory()
                    .create(SubsamplingImage(imageSource), imageSource)
            } else {
                assertFailsWith(
                    exceptionClass = Exception::class,
                    message = "Factory should throw Exception for ${imageFile.name}"
                ) {
                    UIImageRegionDecoder.SupplementSkiaFactory()
                        .create(SubsamplingImage(imageSource), imageSource)
                }
            }
        }

        val imageFile = ComposeResImageFiles.horse
        val imageSource = imageFile.toImageSource()
        UIImageRegionDecoder.SupplementSkiaFactory().create(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }

        UIImageRegionDecoder.SupplementSkiaFactory().create(
            subsamplingImage = SubsamplingImage(imageSource, ImageInfo(1, 1, "image/fake")),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }
    }

    @Test
    fun testSupplementSkiaFactoryEqualsAndHashCode() = runTest {
        val element1 = UIImageRegionDecoder.SupplementSkiaFactory()
        val element2 = UIImageRegionDecoder.SupplementSkiaFactory()

        assertEquals(element1, element2)
        assertEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testSupplementSkiaFactoryToString() = runTest {
        val element = UIImageRegionDecoder.SupplementSkiaFactory()
        assertEquals(expected = "SupplementSkiaUIImageRegionDecoder", actual = element.toString())
    }

    @Test
    fun testConstructor() = runTest {
        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = imageFile.mimeType,
            imageInfo = ImageInfo(100, 100, "image/jpeg"),
            bytes = byteArrayOf(1, 2, 3),
        )
        UIImageRegionDecoder(
            imageSource,
            imageFile.mimeType,
            ImageInfo(100, 100, "image/jpeg"),
            byteArrayOf(1, 2, 3)
        )
        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = imageFile.mimeType,
        )
        UIImageRegionDecoder(imageSource, imageFile.mimeType)
    }

    @Test
    fun testImageInfo() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to isVersionAtLeast(11),
            ComposeResImageFiles.horse to isVersionAtLeast(16),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            val decoder = UIImageRegionDecoder(
                imageSource = imageSource,
                mimeType = imageFile.mimeType
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
        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = imageFile.mimeType
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
        }

        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = "image/fake"
        ).apply {
            assertEquals(ImageInfo(imageFile.size, "image/fake"), this.imageInfo)
        }

        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = "image/fake",
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.imageInfo)
        }
    }

    @Test
    fun testPrepareAndClose() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        val decoder =
            UIImageRegionDecoder(imageSource, imageFile.mimeType)
        decoder.prepare()
        decoder.close()
    }

    @Test
    fun testCopy() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = imageFile.mimeType
        ).apply {
            assertEquals(imageFile.imageInfo, this.imageInfo)
            assertEquals(imageFile.imageInfo, this.copy().imageInfo)
        }

        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = "image/fake"
        ).apply {
            assertEquals(ImageInfo(imageFile.size, "image/fake"), this.imageInfo)
            assertEquals(ImageInfo(imageFile.size, "image/fake"), this.copy().imageInfo)
        }

        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = "image/fake",
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.imageInfo)
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.copy().imageInfo)
        }
    }

    @Test
    fun testDecodeRegion() = runTest {
        val imageFile = ComposeResImageFiles.horse
        val imageSource = imageFile.toImageSource()
        val fullRegion = IntRectCompat(0, 0, imageFile.size.width, imageFile.size.height)
        val region = IntRectCompat(200, 300, 703, 503)
        UIImageRegionDecoder(
            imageSource = imageSource,
            mimeType = imageFile.mimeType
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
            UIImageRegionDecoder(
                imageSource = imageSource,
                mimeType = imageFile.mimeType
            )
        }.decodeRegion(
            region = IntRectCompat(100, 200, 300, 300),
            sampleSize = 1
        )
        val bitmap2 = ComposeResImageFiles.exifRotate90.let { imageFile ->
            val imageSource = imageFile.toImageSource()
            UIImageRegionDecoder(
                imageSource = imageSource,
                mimeType = imageFile.mimeType
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
        val element1 = UIImageRegionDecoder(imageSource1, "image/jpeg")
        val element11 = UIImageRegionDecoder(imageSource1, "image/jpeg")
        val element2 = UIImageRegionDecoder(imageSource2, "image/jpeg")
        val element3 = UIImageRegionDecoder(imageSource2, "image/png")

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
        val element = UIImageRegionDecoder(imageSource, "image/jpeg")
        assertEquals(
            "UIImageRegionDecoder(imageSource=$imageSource, mimeType='image/jpeg')",
            element.toString()
        )
    }
}