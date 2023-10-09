package com.github.panpf.zoomimage.core.test.subsampling

import android.graphics.Bitmap
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.test.internal.ExifOrientationTestFileHelper
import com.github.panpf.zoomimage.core.test.internal.ImageSimilarHelper
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class TileDecoderTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val logger = Logger("Test")

        val imageSource = ImageSource.fromAsset(context, "sample_dog.jpg")
        val imageInfo = imageSource.readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, exifOrientation)
        }
        val tileDecoder = TileDecoder(
            logger = logger,
            imageSource = imageSource,
            tileBitmapPoolHelper = TileBitmapReuseHelper(logger),
            imageInfo = imageInfo
        )
        val bitmap: Bitmap
        try {
            bitmap = tileDecoder.decode(IntRectCompat(100, 200, 300, 300), 4)!!
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
        val imageInfo2 = imageSource2.readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exifOrientation)
        }
        val tileDecoder2 = TileDecoder(
            logger = logger,
            imageSource = imageSource2,
            tileBitmapPoolHelper = TileBitmapReuseHelper(logger),
            imageInfo = imageInfo2
        )
        val bitmap2: Bitmap
        try {
            bitmap2 = tileDecoder2.decode(IntRectCompat(100, 200, 300, 300), 4)!!
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

        val imageInfo3 = imageSource2.readImageInfo(true).getOrThrow().apply {
            Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifOrientation)
        }
        val tileDecoder3 = TileDecoder(
            logger = logger,
            imageSource = imageSource2,
            tileBitmapPoolHelper = TileBitmapReuseHelper(logger),
            imageInfo = imageInfo3
        )
        val bitmap3: Bitmap
        try {
            bitmap3 = tileDecoder3.decode(IntRectCompat(100, 200, 300, 300), 4)!!
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