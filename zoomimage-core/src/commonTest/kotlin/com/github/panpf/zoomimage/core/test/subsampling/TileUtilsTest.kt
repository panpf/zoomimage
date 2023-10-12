//package com.github.panpf.zoomimage.core.test.subsampling
//
//import androidx.exifinterface.media.ExifInterface
//import androidx.test.platform.app.InstrumentationRegistry
//import com.github.panpf.zoomimage.Logger
//import com.github.panpf.zoomimage.core.test.internal.ExifOrientationTestFileHelper
//import com.github.panpf.zoomimage.subsampling.CreateTileDecoderException
//import com.github.panpf.zoomimage.subsampling.ImageSource
//import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper
//import com.github.panpf.zoomimage.subsampling.createTileDecoder
//import com.github.panpf.zoomimage.subsampling.internal.calculateTileGridMap
//import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
//import com.github.panpf.zoomimage.subsampling.toIntroString
//import com.github.panpf.zoomimage.util.IntSizeCompat
//import org.junit.Assert
//import org.junit.Test
//import kotlin.math.roundToInt
//
//// todo implement
//class TileUtilsTest {
//
//    @Test
//    fun testCreateTileDecoder() {
//        val context = InstrumentationRegistry.getInstrumentation().context
//        val logger = Logger("MyTest")
//        val tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
//
//        val imageFile = ExifOrientationTestFileHelper(context, "sample_dog.jpg").files()
//            .find { it.exifOrientation == ExifInterface.ORIENTATION_TRANSVERSE }!!.file
//        val imageSource = ImageSource.fromFile(imageFile)
//        val imageInfo = imageSource.readImageInfo(false).getOrThrow()
//        val thumbnailSize = imageInfo.size / 8
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = imageSource,
//            thumbnailSize = thumbnailSize,
//            ignoreExifOrientation = false
//        ).getOrThrow().apply {
//            Assert.assertEquals(imageInfo, this.imageInfo)
//            Assert.assertEquals(
//                ExifInterface.ORIENTATION_TRANSVERSE,
//                this.imageInfo.exifOrientation
//            )
//        }
//
//        val imageInfo2 = imageSource.readImageInfo(true).getOrThrow()
//        val thumbnailSize2 = imageInfo2.size / 8
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = imageSource,
//            thumbnailSize = thumbnailSize2,
//            ignoreExifOrientation = true
//        ).getOrThrow().apply {
//            Assert.assertEquals(imageInfo2, this.imageInfo)
//            Assert.assertEquals(
//                ExifInterface.ORIENTATION_UNDEFINED,
//                this.imageInfo.exifOrientation
//            )
//        }
//
//        val errorImageSource = ImageSource.fromAsset(context, "fake_image.jpg")
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = errorImageSource,
//            thumbnailSize = thumbnailSize,
//            ignoreExifOrientation = false
//        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
//            Assert.assertEquals(-1, this.code)
//            Assert.assertEquals(false, this.skipped)
//            Assert.assertNotEquals("", this.message)
//            Assert.assertNull(this.imageInfo)
//        }
//
//        val gifImageSource = ImageSource.fromAsset(context, "sample_anim.gif")
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = gifImageSource,
//            thumbnailSize = thumbnailSize,
//            ignoreExifOrientation = false
//        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
//            Assert.assertEquals(-2, this.code)
//            Assert.assertEquals(true, this.skipped)
//            Assert.assertEquals(
//                "Image type not support subsampling",
//                this.message
//            )
//            Assert.assertEquals("image/gif", this.imageInfo!!.mimeType)
//        }
//
//        val errorThumbnailSize = imageInfo.size * 2
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = imageSource,
//            thumbnailSize = errorThumbnailSize,
//            ignoreExifOrientation = false
//        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
//            Assert.assertEquals(-3, this.code)
//            Assert.assertEquals(true, this.skipped)
//            Assert.assertEquals(
//                "The thumbnail size is greater than or equal to the original image",
//                this.message
//            )
//            Assert.assertEquals(imageInfo, this.imageInfo)
//        }
//
//        val errorThumbnailSize2 = thumbnailSize.let {
//            IntSizeCompat(
//                width = it.width,
//                height = (it.height * 1.2).roundToInt()
//            )
//        }
//        createTileDecoder(
//            logger = logger,
//            tileBitmapPoolHelper = tileBitmapPoolHelper,
//            imageSource = imageSource,
//            thumbnailSize = errorThumbnailSize2,
//            ignoreExifOrientation = false
//        ).exceptionOrNull()!!.let { it as CreateTileDecoderException }.apply {
//            Assert.assertEquals(-4, this.code)
//            Assert.assertEquals(false, this.skipped)
//            Assert.assertEquals(
//                "The thumbnail aspect ratio is different with the original image",
//                this.message
//            )
//            Assert.assertEquals(imageInfo, this.imageInfo)
//        }
//    }
//
//    @Test
//    fun testToIntroString() {
//        val imageSize = IntSizeCompat(8000, 8000)
//        val containerSize = IntSizeCompat(1080, 1920)
//        val preferredTileSize = containerSize / 2
//        calculateTileGridMap(
//            imageSize = imageSize,
//            preferredTileSize = preferredTileSize,
//        ).apply {
//            Assert.assertEquals("[16:1:1x1,8:4:2x2,4:12:4x3,2:40:8x5,1:135:15x9]", toIntroString())
//        }
//    }
//}