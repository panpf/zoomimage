package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.internal.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.internal.decodeAndCreateTileDecoder
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.toIntSizeCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SubsamplingDesktopTest {

    @Test
    fun testDecodeAndCreateTileDecoder() = runTest {
        val logger = Logger("MyTest")

        val hugeLongQmshtImageFile = ResourceImages.hugeLongQmsht
        val hugeLongQmshtImageSource = hugeLongQmshtImageFile.toImageSource()
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            thumbnailSize = hugeLongQmshtImageFile.size.toIntSizeCompat() / 32f,
        ).getOrThrow().apply {
            assertEquals(
                expected = hugeLongQmshtImageFile.size.toIntSizeCompat(),
                actual = getImageInfo().size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = getImageInfo().mimeType
            )
        }

        val errorImageSource = ImageSource.fromFile("fake_image.jpg")
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = errorImageSource,
            thumbnailSize = IntSizeCompat(100, 100),
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-1, this.code)
            assertEquals(false, this.skipped)
            assertNotEquals("", this.message)
            assertNull(this.imageInfo)
        }

        val gifImageFile = ResourceImages.anim
        val gifImageSource = gifImageFile.toImageSource()
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = gifImageSource,
            thumbnailSize = gifImageFile.size.toIntSizeCompat() / 8f,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-3, this.code)
            assertEquals(true, this.skipped)
            assertEquals("Image type not support subsampling", this.message)
            assertEquals("image/gif", this.imageInfo!!.mimeType)
        }

        val errorThumbnailSize =
            hugeLongQmshtImageFile.size.toIntSizeCompat() / ScaleFactorCompat(1f, 8f)
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            thumbnailSize = errorThumbnailSize,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-4, this.code)
            assertEquals(true, this.skipped)
            assertEquals(
                "The thumbnail size is greater than or equal to the original image",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size.toIntSizeCompat(),
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }

        val errorThumbnailSize2 =
            hugeLongQmshtImageFile.size.toIntSizeCompat() / ScaleFactorCompat(8f, 1f)
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            thumbnailSize = errorThumbnailSize2,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-4, this.code)
            assertEquals(true, this.skipped)
            assertEquals(
                "The thumbnail size is greater than or equal to the original image",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size.toIntSizeCompat(),
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }

        val errorThumbnailSize3 =
            hugeLongQmshtImageFile.size.toIntSizeCompat() / ScaleFactorCompat(32f, 34f)
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            thumbnailSize = errorThumbnailSize3,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-5, this.code)
            assertEquals(false, this.skipped)
            assertEquals(
                "The aspect ratio of the thumbnail is too different from that of the original image. Please refer to the canUseSubsamplingByAspectRatio() function to correct the thumbnail size.",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size.toIntSizeCompat(),
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }
    }
}