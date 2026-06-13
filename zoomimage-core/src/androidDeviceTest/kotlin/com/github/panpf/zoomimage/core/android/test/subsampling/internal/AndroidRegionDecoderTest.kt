package com.github.panpf.zoomimage.core.android.test.subsampling.internal

import android.os.Build
import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.internal.AndroidRegionDecoder
import com.github.panpf.zoomimage.subsampling.size
import com.github.panpf.zoomimage.test.TestImageSource
import com.github.panpf.zoomimage.test.assertSizeEquals
import com.github.panpf.zoomimage.test.calculateSampledBitmapSize
import com.github.panpf.zoomimage.test.isVersionAtLeast
import com.github.panpf.zoomimage.test.similarity
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AndroidRegionDecoderTest {

    @Test
    fun testFactoryConstructor() {
        AndroidRegionDecoder.Factory()
    }

    @Test
    fun testFactoryAccept() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to isVersionAtLeast(27),
            ComposeResImageFiles.horse to isVersionAtLeast(37),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            assertEquals(
                expected = exceptedOk,
                actual = AndroidRegionDecoder.Factory()
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
            "image/bmp" to false,
            "image/gif" to false,
            "image/svg+xml" to false,
            "image/heic" to isVersionAtLeast(27),
            "image/heif" to isVersionAtLeast(27),
            "image/avif" to isVersionAtLeast(37),
            "video/mp4" to false,
            "image/fake" to null,
        ).forEach { (mimeType, expectedSupport) ->
            assertEquals(
                expected = expectedSupport,
                actual = AndroidRegionDecoder.Factory().checkSupport(mimeType),
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
                AndroidRegionDecoder.Factory()
                    .create(SubsamplingImage(imageSource), imageSource)
            } else {
                assertFailsWith(
                    exceptionClass = Exception::class,
                    message = "Factory should throw Exception for ${imageFile.name}"
                ) {
                    AndroidRegionDecoder.Factory()
                        .create(SubsamplingImage(imageSource), imageSource)
                }
            }
        }

        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        AndroidRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.getImageInfo())
        }

        AndroidRegionDecoder.Factory().create(
            subsamplingImage = SubsamplingImage(imageSource, ImageInfo(1, 1, "image/fake")),
            imageSource = imageSource
        ).apply {
            assertEquals(imageFile.imageInfo, this.getImageInfo())
        }
    }

    @Test
    fun testFactoryEqualsAndHashCode() = runTest {
        val element1 = AndroidRegionDecoder.Factory()
        val element2 = AndroidRegionDecoder.Factory()

        assertEquals(element1, element2)
        assertEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testFactoryToString() = runTest {
        val element = AndroidRegionDecoder.Factory()
        assertEquals(expected = "AndroidRegionDecoder", actual = element.toString())
    }

    @Test
    fun testConstructor() = runTest {
        val imageFile = ComposeResImageFiles.hugeCard
        val imageSource = imageFile.toImageSource()
        AndroidRegionDecoder(
            imageSource = imageSource,
            imageInfo = ImageInfo(100, 100, "image/jpeg"),
        )
        AndroidRegionDecoder(
            imageSource,
            ImageInfo(100, 100, "image/jpeg"),
        )
        AndroidRegionDecoder(imageSource = imageSource)
        AndroidRegionDecoder(imageSource)
    }

    @Test
    fun testImageInfo() = runTest {
        listOf(
            ComposeResImageFiles.hugeCard to true,
            ComposeResImageFiles.anim to true,
            ComposeResImageFiles.exifRotate90 to true,
            ComposeResImageFiles.giraffe to isVersionAtLeast(27),
            ComposeResImageFiles.horse to isVersionAtLeast(37),
        ).forEach { (imageFile, exceptedOk) ->
            val imageSource = imageFile.toImageSource()
            val decoder = AndroidRegionDecoder(
                imageSource = imageSource,
            )
            val exceptedError = imageFile == ComposeResImageFiles.giraffe
                    && (Build.VERSION.SDK_INT == 36 || Build.VERSION.SDK_INT == 37)
            if (exceptedOk && !exceptedError) {
                val imageInfo = try {
                    decoder.getImageInfo()
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
                    decoder.getImageInfo()
                }
            }
        }

        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        AndroidRegionDecoder(
            imageSource = imageSource,
        ).apply {
            assertEquals(imageFile.imageInfo, this.getImageInfo())
        }

        AndroidRegionDecoder(
            imageSource = imageSource,
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.getImageInfo())
        }
    }

    @Test
    fun testPrepareAndClose() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        AndroidRegionDecoder(imageSource).use {
            it.prepare()
        }

        assertFailsWith(UnsupportedOperationException::class) {
            AndroidRegionDecoder(TestImageSource()).use {
                it.prepare()
            }
        }
    }

    @Test
    fun testCopy() = runTest {
        val imageFile = ComposeResImageFiles.exifRotate90
        val imageSource = imageFile.toImageSource()
        AndroidRegionDecoder(imageSource = imageSource).apply {
            assertEquals(imageFile.imageInfo, this.getImageInfo())
            assertEquals(imageFile.imageInfo, this.copy().getImageInfo())
        }

        AndroidRegionDecoder(
            imageSource = imageSource,
            imageInfo = ImageInfo(1, 1, "image/fake2")
        ).apply {
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.getImageInfo())
            assertEquals(ImageInfo(1, 1, "image/fake2"), this.copy().getImageInfo())
        }
    }

    @Test
    fun testDecodeRegion() = runTest {
        val imageFile = ComposeResImageFiles.dog
        val imageSource = imageFile.toImageSource()
        val fullRegion = IntRectCompat(0, 0, imageFile.size.width, imageFile.size.height)
        val region = IntRectCompat(200, 300, 703, 503)
        AndroidRegionDecoder(imageSource = imageSource).apply {
            assertSizeEquals(
                expected = imageFile.size,
                actual = decodeRegion(sampleSize = 1, region = fullRegion).size,
                delta = IntSizeCompat(1, 1)
            )

            assertSizeEquals(
                expected = calculateSampledBitmapSize(imageSize = imageFile.size, sampleSize = 2),
                actual = decodeRegion(sampleSize = 2, region = fullRegion).size,
                delta = IntSizeCompat(1, 1)
            )

            assertSizeEquals(
                expected = calculateSampledBitmapSize(imageSize = imageFile.size, sampleSize = 4),
                actual = decodeRegion(sampleSize = 4, region = fullRegion).size,
                delta = IntSizeCompat(1, 1)
            )

            val regionSize = IntSizeCompat(region.width, region.height)
            assertSizeEquals(
                expected = regionSize,
                actual = decodeRegion(sampleSize = 1, region = region).size,
                delta = IntSizeCompat(1, 1)
            )

            assertSizeEquals(
                expected = calculateSampledBitmapSize(imageSize = regionSize, sampleSize = 2),
                actual = decodeRegion(sampleSize = 2, region = region).size,
                delta = IntSizeCompat(1, 1)
            )

            assertSizeEquals(
                expected = calculateSampledBitmapSize(imageSize = regionSize, sampleSize = 4),
                actual = decodeRegion(sampleSize = 4, region = region).size,
                delta = IntSizeCompat(1, 1)
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
            AndroidRegionDecoder(imageSource = imageSource)
        }.decodeRegion(
            region = IntRectCompat(100, 200, 300, 300),
            sampleSize = 1
        )
        val bitmap2 = ComposeResImageFiles.exifRotate90.let { imageFile ->
            val imageSource = imageFile.toImageSource()
            AndroidRegionDecoder(imageSource = imageSource)
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
        val element1 = AndroidRegionDecoder(imageSource1)
        val element11 = AndroidRegionDecoder(imageSource1)
        val element2 = AndroidRegionDecoder(imageSource2)

        assertEquals(element1, element11)
        assertNotEquals(element1, element2)

        assertEquals(element1.hashCode(), element11.hashCode())
        assertNotEquals(element1.hashCode(), element2.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val imageSource = TestImageSource()
        val element = AndroidRegionDecoder(imageSource)
        assertEquals(
            "AndroidRegionDecoder(imageSource=$imageSource)",
            element.toString()
        )
    }
}