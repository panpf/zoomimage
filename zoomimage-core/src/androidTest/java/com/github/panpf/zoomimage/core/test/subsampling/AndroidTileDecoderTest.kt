package com.github.panpf.zoomimage.core.test.subsampling

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.ExifOrientationTestFileHelper
import com.github.panpf.zoomimage.core.test.internal.ImageSimilarHelper
import com.github.panpf.zoomimage.subsampling.AndroidExifOrientation
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmap
import com.github.panpf.zoomimage.subsampling.AndroidTileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.AndroidTileDecoder
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.fromAsset
import com.github.panpf.zoomimage.subsampling.internal.readExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class AndroidTileDecoderTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val logger = Logger("Test")

        val imageSource = ImageSource.fromAsset(context, "sample_dog.jpg")
        val exifOrientation = imageSource.readExifOrientation().getOrThrow().apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, this)
        }.let { AndroidExifOrientation(it) }
        val imageInfo = imageSource.readImageInfo().getOrThrow().let {
            exifOrientation.applyToImageInfo(it)
        }
        val tileDecoder = AndroidTileDecoder(
            logger = logger,
            imageSource = imageSource,
            imageInfo = imageInfo,
            exifOrientation = exifOrientation,
            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
        )
        val bitmap: Bitmap
        try {
            bitmap = (tileDecoder.decode(
                IntRectCompat(100, 200, 300, 300),
                4
            )!! as AndroidTileBitmap).bitmap!!
            bitmap.apply {
                Assert.assertEquals(50, width)
                Assert.assertEquals(25, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder.destroy("test")
            }
        }

        val file = ExifOrientationTestFileHelper(context, "sample_dog.jpg").files()
            .find { it.exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 }!!.file
        val imageSource2 = ImageSource.fromFile(file)
        val exifOrientation2 = imageSource2.readExifOrientation().getOrThrow().apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_90, this)
        }.let { AndroidExifOrientation(it) }
        val imageInfo2 = imageSource2.readImageInfo().getOrThrow().let {
            exifOrientation2.applyToImageInfo(it)
        }
        val tileDecoder2 = AndroidTileDecoder(
            logger = logger,
            imageSource = imageSource2,
            imageInfo = imageInfo2,
            exifOrientation = exifOrientation2,
            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
        )
        val bitmap2: Bitmap
        try {
            bitmap2 = (tileDecoder2.decode(
                IntRectCompat(100, 200, 300, 300),
                4
            )!! as AndroidTileBitmap).bitmap!!
            bitmap2.apply {
                Assert.assertEquals(50, width)
                Assert.assertEquals(25, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder2.destroy("test")
            }
        }
        val bitmapFinger = ImageSimilarHelper.produceFingerPrint(bitmap)
        val bitmap2Finger = ImageSimilarHelper.produceFingerPrint(bitmap2)
        val hanming2 = ImageSimilarHelper.hammingDistance(bitmapFinger, bitmap2Finger)
        Assert.assertTrue(hanming2 <= 2)


        val exifOrientation3 = ExifInterface.ORIENTATION_UNDEFINED.apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, this)
        }.let { AndroidExifOrientation(it) }
        val imageInfo3 = imageSource2.readImageInfo().getOrThrow().let {
            exifOrientation3.applyToImageInfo(it)
        }
        val tileDecoder3 = AndroidTileDecoder(
            logger = logger,
            imageSource = imageSource2,
            imageInfo = imageInfo3,
            exifOrientation = exifOrientation3,
            tileBitmapReuseHelper = AndroidTileBitmapReuseHelper(logger, TileBitmapReuseSpec()),
        )
        val bitmap3: Bitmap
        try {
            bitmap3 = (tileDecoder3.decode(
                IntRectCompat(100, 200, 300, 300),
                4
            )!! as AndroidTileBitmap).bitmap!!
            bitmap3.apply {
                Assert.assertEquals(50, width)
                Assert.assertEquals(25, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder3.destroy("test")
            }
        }
        val bitmap3Finger = ImageSimilarHelper.produceFingerPrint(bitmap3)
        val hanming3 = ImageSimilarHelper.hammingDistance(bitmapFinger, bitmap3Finger)
        Assert.assertTrue(hanming3 > 2)
    }
}