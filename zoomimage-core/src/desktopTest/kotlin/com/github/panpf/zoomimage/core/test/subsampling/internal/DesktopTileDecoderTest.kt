package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.core.test.internal.fromResource
import com.github.panpf.zoomimage.core.test.internal.hammingDistance
import com.github.panpf.zoomimage.core.test.internal.produceFingerPrint
import com.github.panpf.zoomimage.subsampling.DesktopExifOrientation
import com.github.panpf.zoomimage.subsampling.DesktopTileBitmap
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.internal.DesktopTileDecoder
import com.github.panpf.zoomimage.subsampling.internal.decodeExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.awt.image.BufferedImage

class DesktopTileDecoderTest {

    @Test
    fun test() {
        val logger = Logger("Test")

        val imageSource = ImageSource.fromResource("sample_exif_girl_normal.jpg")
        val exifOrientation = imageSource.decodeExifOrientation()
            .getOrThrow().let { it as DesktopExifOrientation }.apply {
                Assert.assertEquals(ExifOrientation.ORIENTATION_NORMAL, this.exifOrientation)
            }
        val imageInfo = imageSource.decodeImageInfo().getOrThrow().let {
            exifOrientation.applyToImageInfo(it)
        }
        val tileDecoder = DesktopTileDecoder(
            logger = logger,
            imageSource = imageSource,
            imageInfo = imageInfo,
            exifOrientation = exifOrientation,
        )
        val bitmap: BufferedImage
        try {
            bitmap = tileDecoder
                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
                .let { it as DesktopTileBitmap }
                .bufferedImage
            bitmap.apply {
                Assert.assertEquals(200, width)
                Assert.assertEquals(100, height)
            }

            val bitmap1 = tileDecoder
                .decode(IntRectCompat(100, 200, 300, 300), 4)!!
                .let { it as DesktopTileBitmap }
                .bufferedImage
            bitmap1.apply {
                Assert.assertEquals(50, width)
                Assert.assertEquals(25, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder.destroy("test")
            }
        }

        val imageSource2 = ImageSource.fromResource("sample_exif_girl_rotate_90.jpg")
        val exifOrientation2 = imageSource2.decodeExifOrientation()
            .getOrThrow().let { it as DesktopExifOrientation }.apply {
                Assert.assertEquals(ExifOrientation.ORIENTATION_ROTATE_90, this.exifOrientation)
            }
        val imageInfo2 = imageSource2.decodeImageInfo().getOrThrow().let {
            exifOrientation2.applyToImageInfo(it)
        }
        val tileDecoder2 = DesktopTileDecoder(
            logger = logger,
            imageSource = imageSource2,
            imageInfo = imageInfo2,
            exifOrientation = exifOrientation2,
        )
        val bitmap2: BufferedImage
        try {
            bitmap2 = tileDecoder2
                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
                .let { it as DesktopTileBitmap }
                .bufferedImage
            bitmap2.apply {
                Assert.assertEquals(200, width)
                Assert.assertEquals(100, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder2.destroy("test")
            }
        }
        val bitmapFinger = produceFingerPrint(bitmap)
        val bitmap2Finger = produceFingerPrint(bitmap2)
        val hanming2 = hammingDistance(bitmapFinger, bitmap2Finger)
        Assert.assertTrue(hanming2 <= 2)

        val exifOrientation3 = ExifOrientation.ORIENTATION_UNDEFINED.apply {
            Assert.assertEquals(ExifOrientation.ORIENTATION_UNDEFINED, this)
        }.let { DesktopExifOrientation(it) }
        val imageInfo3 = imageSource2.decodeImageInfo().getOrThrow().let {
            exifOrientation3.applyToImageInfo(it)
        }
        val tileDecoder3 = DesktopTileDecoder(
            logger = logger,
            imageSource = imageSource2,
            imageInfo = imageInfo3,
            exifOrientation = exifOrientation3,
        )
        val bitmap3: BufferedImage
        try {
            bitmap3 = tileDecoder3
                .decode(IntRectCompat(100, 200, 300, 300), 1)!!
                .let { it as DesktopTileBitmap }
                .bufferedImage
            bitmap3.apply {
                Assert.assertEquals(200, width)
                Assert.assertEquals(100, height)
            }
        } finally {
            runBlocking(Dispatchers.Main) {
                tileDecoder3.destroy("test")
            }
        }
        val bitmap3Finger = produceFingerPrint(bitmap3)
        val hanming3 = hammingDistance(bitmapFinger, bitmap3Finger)
        Assert.assertTrue(hanming3 > 2)
    }
}