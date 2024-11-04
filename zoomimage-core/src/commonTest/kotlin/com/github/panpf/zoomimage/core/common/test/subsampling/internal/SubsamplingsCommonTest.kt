package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.checkImageInfo
import com.github.panpf.zoomimage.subsampling.internal.checkNewPreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.test.TestRegionDecoder
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SubsamplingsCommonTest {

    @Test
    fun testCheckImageInfo() {
        // success
        checkImageInfo(
            imageInfo = ImageInfo(100, 100, "image/jpeg"),
            factory = TestRegionDecoder.Factory(ImageInfo(100, 100, "image/jpeg")),
            contentSize = IntSizeCompat(50, 40)
        )

        // error: imageSize empty
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(0, 100, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(0, 100, "image/jpeg")),
                contentSize = IntSizeCompat(50, 40)
            )
        }
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 0, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 0, "image/jpeg")),
                contentSize = IntSizeCompat(50, 40)
            )
        }

        // error: contentSize >= imageSize
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 100, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 100, "image/jpeg")),
                contentSize = IntSizeCompat(101, 40)
            )
        }
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 100, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 100, "image/jpeg")),
                contentSize = IntSizeCompat(40, 101)
            )
        }

        // error: aspect ratio too different
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 100, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 100, "image/jpeg")),
                contentSize = IntSizeCompat(50, 30)
            )
        }

        // error: unsupported mimeTypes
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 100, "image/jpeg"),
                factory = TestRegionDecoder.Factory(
                    ImageInfo(100, 100, "image/jpeg"),
                    unsupportedMimeTypes = listOf("image/jpeg")
                ),
                contentSize = IntSizeCompat(50, 40)
            )
        }
    }

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        val imageSize = IntSizeCompat(1000, 2000)

        listOf(
            (ScaleFactorCompat(17f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.5f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.93f) to null as Float?) to true,
            (ScaleFactorCompat(17f, 17.94f) to null as Float?) to false,
            (ScaleFactorCompat(17f, 17.94f) to 1.1f) to true,
            (ScaleFactorCompat(17.5f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(18.01f, 17f) to null as Float?) to true,
            (ScaleFactorCompat(18.02f, 17f) to null as Float?) to false,
            (ScaleFactorCompat(18.02f, 17f) to 1.2f) to true,
        ).forEachIndexed { index, it ->
            val (scaleFactor, minDifference) = it.first
            val expected = it.second
            if (minDifference != null) {
                assertEquals(
                    expected = expected,
                    actual = canUseSubsamplingByAspectRatio(
                        imageSize = imageSize,
                        thumbnailSize = imageSize / scaleFactor,
                        maxDifference = minDifference
                    ),
                    message = "item=${index + 1}, scaleFactor=$scaleFactor, minDifference=$minDifference"
                )
            } else {
                assertEquals(
                    expected = expected,
                    actual = canUseSubsamplingByAspectRatio(
                        imageSize = imageSize,
                        thumbnailSize = imageSize / scaleFactor
                    ),
                    message = "item=${index + 1}, scaleFactor=$scaleFactor, minDifference=$minDifference"
                )
            }
        }

        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(0, 2000),
                thumbnailSize = IntSizeCompat(500, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 0),
                thumbnailSize = IntSizeCompat(500, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(0, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(500, 0),
            )
        )

        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(1001, 1000),
            )
        )
        assertFalse(
            canUseSubsamplingByAspectRatio(
                imageSize = IntSizeCompat(1000, 2000),
                thumbnailSize = IntSizeCompat(500, 20001),
            )
        )
    }

    @Test
    fun testToIntroString() {
        val imageSize = IntSizeCompat(8000, 8000)
        val containerSize = IntSizeCompat(1080, 1920)
        val preferredTileSize = containerSize / 2
        calculateTileGridMap(
            imageSize = imageSize,
            preferredTileSize = preferredTileSize,
        ).apply {
            assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
        }
    }

    @Test
    fun testCalculatePreferredTileSize() {
        assertEquals(
            /* expected = */ IntSizeCompat(1080, 1920) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1080, 1920))
        )

        assertEquals(
            /* expected = */ IntSizeCompat(1000, 2000) / 2,
            /* actual = */ calculatePreferredTileSize(IntSizeCompat(1000, 2000))
        )
    }

    @Test
    fun testCheckNewPreferredTileSize() {
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(200, 100)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(100, 200)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(200, 200)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(50, 100)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(100, 50)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(50, 50)
            )
        )

        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(0, 0)
            )
        )

        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(199, 100)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(100, 199)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(199, 199)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(51, 100)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(100, 51)
            )
        )
        assertEquals(
            expected = false,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(100, 100),
                newPreferredTileSize = IntSizeCompat(51, 51)
            )
        )

        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(199, 100)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(100, 199)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(199, 199)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(51, 100)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(100, 51)
            )
        )
        assertEquals(
            expected = true,
            checkNewPreferredTileSize(
                oldPreferredTileSize = IntSizeCompat(0, 0),
                newPreferredTileSize = IntSizeCompat(51, 51)
            )
        )
    }
}