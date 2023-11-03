package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.core.test.internal.fromResource
import com.github.panpf.zoomimage.subsampling.DesktopExifOrientation
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.internal.checkSupportSubsamplingByMimeType
import com.github.panpf.zoomimage.subsampling.internal.decodeExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
import org.junit.Assert
import org.junit.Test

class DesktopTileDecodeUtilsTest {

    @Test
    fun testReadExifOrientation() {
        listOf(
            "sample_dog.jpg" to ExifOrientation.ORIENTATION_NORMAL,
            "sample_exif_girl_rotate_90.jpg" to ExifOrientation.ORIENTATION_ROTATE_90,
        ).forEach { (resourcePath, excepted) ->
            Assert.assertEquals(
                "resourcePath=$resourcePath, excepted=$excepted",
                excepted,
                ImageSource.fromResource(resourcePath).decodeExifOrientation()
                    .getOrThrow().let { it as DesktopExifOrientation }.exifOrientation
            )
        }
    }

    @Test
    fun testReadImageInfo() {
        listOf(
            "sample_dog.jpg" to ImageInfo(575, 427, "image/jpeg"),
            "sample_exif_girl_rotate_90.jpg" to ImageInfo(6400, 1080, "image/jpeg"),
        ).forEach { (resourcePath, excepted) ->
            Assert.assertEquals(
                "resourcePath=$resourcePath, excepted=$excepted",
                excepted,
                ImageSource.fromResource(resourcePath).decodeImageInfo().getOrThrow()
            )
        }
    }

    @Test
    fun testIsSupportSourceRegion() {
        listOf(
            "image/jpeg" to true,
            "image/png" to true,
            "image/gif" to false,
            "image/bmp" to true,
        ).forEach { (mimeType, excepted) ->
            Assert.assertEquals(
                "mimeType=$mimeType, excepted=$excepted",
                excepted,
                checkSupportSubsamplingByMimeType(mimeType)
            )
        }
    }
}