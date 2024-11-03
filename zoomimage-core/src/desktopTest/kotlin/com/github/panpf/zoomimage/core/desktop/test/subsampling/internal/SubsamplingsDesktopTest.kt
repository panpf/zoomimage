package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.fromFile
import com.github.panpf.zoomimage.subsampling.internal.createTileDecoder
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubsamplingDesktopTest {

    @Test
    fun testCreateTileDecoder() = runTest {
        val logger = Logger("MyTest")

        // success
        val hugeLongQmshtImageFile = ResourceImages.hugeLongQmsht
        val hugeLongQmshtImageSource = hugeLongQmshtImageFile.toImageSource()
        createTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            contentSize = hugeLongQmshtImageFile.size / 32f,
        ).getOrThrow().apply {
            assertEquals(
                expected = hugeLongQmshtImageFile.size,
                actual = imageInfo.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo.mimeType
            )
        }

        // error -1
        val errorImageSource = ImageSource.fromFile("fake_image.jpg")
        createTileDecoder(
            logger = logger,
            imageSource = errorImageSource,
            contentSize = IntSizeCompat(100, 100),
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-1, this.code)
            assertEquals(false, this.skipped)
            assertEquals(
                "Create DecodeHelper failed: fake_image.jpg (No such file or directory)",
                this.message
            )
            assertNull(this.imageInfo)
        }

//        // error -3
//        val gifImageFile = ResourceImages.anim
//        val gifImageSource = gifImageFile.toImageSource()
//        createTileDecoder(
//            logger = logger,
//            imageSource = gifImageSource,
//            contentSize = gifImageFile.size / 8f,
//        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
//            assertEquals(-3, this.code)
//            assertEquals(true, this.skipped)
//            assertEquals("Image type not support subsampling", this.message)
//            assertEquals("image/gif", this.imageInfo!!.mimeType)
//        }

        // error -4, width
        val errorThumbnailSize =
            hugeLongQmshtImageFile.size / ScaleFactorCompat(1f, 8f)
        createTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            contentSize = errorThumbnailSize,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-4, this.code)
            assertEquals(true, this.skipped)
            assertEquals(
                "The thumbnail size is greater than or equal to the original image",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size,
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }

        // error -4, height
        val errorThumbnailSize2 =
            hugeLongQmshtImageFile.size / ScaleFactorCompat(8f, 1f)
        createTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            contentSize = errorThumbnailSize2,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-4, this.code)
            assertEquals(true, this.skipped)
            assertEquals(
                "The thumbnail size is greater than or equal to the original image",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size,
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }

        // error -5
        val errorThumbnailSize3 =
            hugeLongQmshtImageFile.size / ScaleFactorCompat(32f, 34f)
        createTileDecoder(
            logger = logger,
            imageSource = hugeLongQmshtImageSource,
            contentSize = errorThumbnailSize3,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            assertEquals(-5, this.code)
            assertEquals(false, this.skipped)
            assertEquals(
                "The aspect ratio of the thumbnail is too different from that of the original image. Please refer to the canUseSubsamplingByAspectRatio() function to correct the thumbnail size.",
                this.message
            )
            assertEquals(
                expected = hugeLongQmshtImageFile.size,
                actual = this.imageInfo!!.size
            )
            assertEquals(
                expected = "image/jpeg",
                actual = imageInfo!!.mimeType
            )
        }
    }
}