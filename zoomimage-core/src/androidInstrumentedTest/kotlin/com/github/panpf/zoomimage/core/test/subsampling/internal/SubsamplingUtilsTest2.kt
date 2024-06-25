package com.github.panpf.zoomimage.core.test.subsampling.internal

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.internal.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.internal.decodeAndCreateTileDecoder
import com.github.panpf.zoomimage.test.decodeExifOrientation
import com.github.panpf.zoomimage.test.decodeImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import kotlin.math.roundToInt

class SubsamplingUtilsTest2 {

    @Test
    fun testDecodeAndCreateTileDecoder() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val logger = Logger("MyTest")

        val imageSource = ImageSource.fromAsset(context, "sample_exif_girl_rotate_90.jpg")
        val exifOrientation =
            imageSource.decodeExifOrientation().getOrThrow()
        val imageInfo = imageSource.decodeImageInfo().getOrThrow()
        val correctOrientationImageInfo =
            AndroidExifOrientation(exifOrientation).applyToImageInfo(imageInfo)
        val thumbnailSize = correctOrientationImageInfo.size / 8
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = imageSource,
            thumbnailSize = thumbnailSize,
        ).getOrThrow().apply {
            Assert.assertEquals(correctOrientationImageInfo, this.getImageInfo())
        }

        val thumbnailSize2 = imageInfo.size / 8
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = imageSource,
            thumbnailSize = thumbnailSize2,
        ).getOrThrow().apply {
            Assert.assertEquals(imageInfo, this.getImageInfo())
        }

        val errorImageSource = ImageSource.fromAsset(context, "fake_image.jpg")
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = errorImageSource,
            thumbnailSize = thumbnailSize,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            Assert.assertEquals(-1, this.code)
            Assert.assertEquals(false, this.skipped)
            Assert.assertNotEquals("", this.message)
            Assert.assertNull(this.imageInfo)
        }

        val gifImageSource = ImageSource.fromAsset(context, "sample_anim.gif")
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = gifImageSource,
            thumbnailSize = thumbnailSize,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            Assert.assertEquals(-3, this.code)
            Assert.assertEquals(true, this.skipped)
            Assert.assertEquals(
                "Image type not support subsampling",
                this.message
            )
            Assert.assertEquals("image/gif", this.imageInfo!!.mimeType)
        }

        val errorThumbnailSize = correctOrientationImageInfo.size * 2
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = imageSource,
            thumbnailSize = errorThumbnailSize,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            Assert.assertEquals(-4, this.code)
            Assert.assertEquals(true, this.skipped)
            Assert.assertEquals(
                "The thumbnail size is greater than or equal to the original image",
                this.message
            )
            Assert.assertEquals(correctOrientationImageInfo, this.imageInfo)
        }

        val errorThumbnailSize2 = thumbnailSize.let {
            IntSizeCompat(
                width = it.width,
                height = (it.height * 1.2).roundToInt()
            )
        }
        decodeAndCreateTileDecoder(
            logger = logger,
            imageSource = imageSource,
            thumbnailSize = errorThumbnailSize2,
        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
            Assert.assertEquals(-5, this.code)
            Assert.assertEquals(false, this.skipped)
            Assert.assertEquals(
                "The thumbnail aspect ratio is different with the original image",
                this.message
            )
            Assert.assertEquals(correctOrientationImageInfo, this.imageInfo)
        }
    }
}