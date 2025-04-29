package com.github.panpf.zoomimage.core.common.test.subsampling.internal

import com.github.panpf.zoomimage.core.common.test.util.RoundMode
import com.github.panpf.zoomimage.core.common.test.util.roundToIntWithMode
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.internal.checkImageInfo
import com.github.panpf.zoomimage.subsampling.internal.checkNewPreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.test.TestRegionDecoder
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class SubsamplingsCommonTest {

    @Test
    fun testCheckImageInfo() {
        // success
        checkImageInfo(
            imageInfo = ImageInfo(100, 200, "image/jpeg"),
            factory = TestRegionDecoder.Factory(ImageInfo(100, 200, "image/jpeg")),
            contentSize = IntSizeCompat(50, 100)
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

        // error: contentSize empty
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 200, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 200, "image/jpeg")),
                contentSize = IntSizeCompat(50, 0)
            )
        }
        assertFailsWith(Exception::class) {
            checkImageInfo(
                imageInfo = ImageInfo(100, 200, "image/jpeg"),
                factory = TestRegionDecoder.Factory(ImageInfo(100, 200, "image/jpeg")),
                contentSize = IntSizeCompat(0, 40)
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
        val imageSize = IntSizeCompat(29999, 325)
        generateSequence(1f) { it + 0.1f }.takeWhile { it <= 257 }.forEach { multiple ->
            val thumbnailSize = (imageSize.toSize() / multiple).roundToIntWithMode(RoundMode.CEIL)

            if (imageSize != thumbnailSize) {
                checkImageInfo(
                    imageInfo = ImageInfo(imageSize, "image/jpeg"),
                    factory = TestRegionDecoder.Factory(ImageInfo(imageSize, "image/jpeg")),
                    contentSize = thumbnailSize
                )
            } else {
                assertFailsWith(Exception::class) {
                    checkImageInfo(
                        imageInfo = ImageInfo(imageSize, "image/jpeg"),
                        factory = TestRegionDecoder.Factory(ImageInfo(imageSize, "image/jpeg")),
                        contentSize = thumbnailSize
                    )
                }
            }

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 3)
            assertFailsWith(Exception::class) {
                checkImageInfo(
                    imageInfo = ImageInfo(imageSize, "image/jpeg"),
                    factory = TestRegionDecoder.Factory(ImageInfo(imageSize, "image/jpeg")),
                    contentSize = thumbnailSize2
                )
            }
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

        var imageSize = IntSizeCompat(29999, 325)
        val maxMultiple = 257

        val nextFunction: (Float) -> Float = { it + 0.1f }
        val calculateThumbnailSize: (IntSizeCompat, Float) -> SizeCompat =
            { size, multiple -> size.toSize() / multiple }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.CEIL)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 3)
            assertFalse(
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.FLOOR)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 3)
            assertFalse(
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.ROUND)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 3)
            assertFalse(
                actual = canUseSubsamplingByAspectRatio(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        imageSize = IntSizeCompat(325, 29999)

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.CEIL)
            assertFalse(
                canUseSubsamplingByAspectRatio(thumbnailSize, imageSize),
                "imageSize=${imageSize.toShortString()}, thumbnailSize=${thumbnailSize.toShortString()}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.FLOOR)
            assertFalse(
                canUseSubsamplingByAspectRatio(thumbnailSize, imageSize),
                "imageSize=${imageSize.toShortString()}, thumbnailSize=${thumbnailSize.toShortString()}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.ROUND)
            assertFalse(
                canUseSubsamplingByAspectRatio(thumbnailSize, imageSize),
                "imageSize=${imageSize.toShortString()}, thumbnailSize=${thumbnailSize.toShortString()}"
            )
        }
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