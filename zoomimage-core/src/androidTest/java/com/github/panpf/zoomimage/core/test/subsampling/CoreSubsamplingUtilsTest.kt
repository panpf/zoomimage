package com.github.panpf.zoomimage.core.test.subsampling

import android.os.Build
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.ExifOrientationTestFileHelper
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.canUseSubsamplingByAspectRatio
import com.github.panpf.zoomimage.subsampling.checkUseSubsampling
import com.github.panpf.zoomimage.subsampling.readExifOrientation
import com.github.panpf.zoomimage.subsampling.readExifOrientationWithMimeType
import com.github.panpf.zoomimage.subsampling.readImageBounds
import com.github.panpf.zoomimage.subsampling.readImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import org.junit.Assert
import org.junit.Test

class CoreSubsamplingUtilsTest {

    @Test
    fun testReadImageBounds() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromAsset(context, "sample_dog.jpg").readImageBounds().getOrThrow().apply {
            Assert.assertEquals(575, outWidth)
            Assert.assertEquals(427, outHeight)
            Assert.assertEquals("image/jpeg", outMimeType)
        }

        ImageSource.fromAsset(context, "sample_cat.jpg").readImageBounds().getOrThrow().apply {
            Assert.assertEquals(551, outWidth)
            Assert.assertEquals(1038, outHeight)
            Assert.assertEquals("image/jpeg", outMimeType)
        }
    }

    @Test
    fun testReadExifOrientation() {
        val context = InstrumentationRegistry.getInstrumentation().context

        Assert.assertEquals(
            ExifInterface.ORIENTATION_NORMAL,
            ImageSource.fromAsset(context, "sample_dog.jpg").readExifOrientation().getOrThrow()
        )

        ExifOrientationTestFileHelper(context, "sample_dog.jpg").files().forEach {
            Assert.assertEquals(
                it.exifOrientation,
                ImageSource.fromFile(it.file).readExifOrientation().getOrThrow()
            )
        }
    }

    @Test
    fun testReadExifOrientationWithMimeType() {
        val context = InstrumentationRegistry.getInstrumentation().context

        Assert.assertEquals(
            ExifInterface.ORIENTATION_NORMAL,
            ImageSource.fromAsset(context, "sample_dog.jpg")
                .readExifOrientationWithMimeType("image/jpeg").getOrThrow()
        )

        Assert.assertEquals(
            ExifInterface.ORIENTATION_UNDEFINED,
            ImageSource.fromAsset(context, "sample_dog.jpg")
                .readExifOrientationWithMimeType("image/bmp").getOrThrow()
        )

        ExifOrientationTestFileHelper(context, "sample_dog.jpg").files().forEach {
            Assert.assertEquals(
                it.exifOrientation,
                ImageSource.fromFile(it.file)
                    .readExifOrientationWithMimeType("image/jpeg").getOrThrow()
            )
            Assert.assertEquals(
                ExifInterface.ORIENTATION_UNDEFINED,
                ImageSource.fromFile(it.file)
                    .readExifOrientationWithMimeType("image/bmp").getOrThrow()
            )
        }
    }

    @Test
    fun testReadImageInfo() {
        val context = InstrumentationRegistry.getInstrumentation().context

        ImageSource.fromAsset(context, "sample_dog.jpg").readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(IntSizeCompat(575, 427), size)
            Assert.assertEquals("image/jpeg", mimeType)
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, exifOrientation)
        }

        ImageSource.fromAsset(context, "sample_cat.jpg").readImageInfo(false).getOrThrow().apply {
            Assert.assertEquals(IntSizeCompat(551, 1038), size)
            Assert.assertEquals("image/jpeg", mimeType)
            Assert.assertEquals(ExifInterface.ORIENTATION_NORMAL, exifOrientation)
        }

        ExifOrientationTestFileHelper(context, "sample_dog.jpg")
            .files()
            .find { it.exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 }!!
            .also {
                ImageSource.fromFile(it.file).readImageInfo(false).getOrThrow().apply {
                    Assert.assertEquals(ExifInterface.ORIENTATION_ROTATE_90, exifOrientation)
                    Assert.assertEquals(IntSizeCompat(575, 427), size)
                    Assert.assertEquals("image/jpeg", mimeType)
                }

                ImageSource.fromFile(it.file).readImageInfo(true).getOrThrow().apply {
                    Assert.assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exifOrientation)
                    Assert.assertEquals(IntSizeCompat(427, 575), size)
                    Assert.assertEquals("image/jpeg", mimeType)
                }
            }
    }

    @Test
    fun testCheckUseSubsampling() {
        val imageInfo = ImageInfo(IntSizeCompat(1000, 2000), "image/jpeg", 0)

        Assert.assertEquals(
            -1,
            checkUseSubsampling(imageInfo, imageInfo.size)
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size - IntSizeCompat(1, 0))
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size - IntSizeCompat(0, 1))
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size - IntSizeCompat(1, 1))
        )
        Assert.assertEquals(
            -1,
            checkUseSubsampling(imageInfo, imageInfo.size + IntSizeCompat(1, 0))
        )
        Assert.assertEquals(
            -1,
            checkUseSubsampling(imageInfo, imageInfo.size + IntSizeCompat(0, 1))
        )
        Assert.assertEquals(
            -1,
            checkUseSubsampling(imageInfo, imageInfo.size + IntSizeCompat(1, 1))
        )

        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size / ScaleFactorCompat(17f, 17f))
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size / ScaleFactorCompat(17f, 16.5f))
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(imageInfo, imageInfo.size / ScaleFactorCompat(17.3f, 17f))
        )
        Assert.assertEquals(
            -2,
            checkUseSubsampling(imageInfo, imageInfo.size / ScaleFactorCompat(17f, 16.4f))
        )
        Assert.assertEquals(
            -2,
            checkUseSubsampling(imageInfo, imageInfo.size / ScaleFactorCompat(17.6f, 17f))
        )

        Assert.assertEquals(
            0,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/jpeg"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/png"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            0,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/webp"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            -3,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/bmp"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            -3,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/gif"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) 0 else -3,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/heic"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
        Assert.assertEquals(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) 0 else -3,
            checkUseSubsampling(
                imageInfo.copy(mimeType = "image/heif"),
                imageInfo.size / ScaleFactorCompat(17f, 17f)
            )
        )
    }

    @Test
    fun testCanUseSubsamplingByAspectRatio() {
        val imageSize = IntSizeCompat(1000, 2000)

        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.5f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.3f, 17f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17f, 16.4f))
        )
        Assert.assertFalse(
            canUseSubsamplingByAspectRatio(imageSize, imageSize / ScaleFactorCompat(17.6f, 17f))
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17f, 16.4f),
                minDifference = 0.8f
            )
        )
        Assert.assertTrue(
            canUseSubsamplingByAspectRatio(
                imageSize,
                imageSize / ScaleFactorCompat(17.6f, 17f),
                minDifference = 0.8f
            )
        )
    }

    private operator fun IntSizeCompat.minus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width - other.width, this.height - other.height)
    }

    private operator fun IntSizeCompat.plus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width + other.width, this.height + other.height)
    }
}