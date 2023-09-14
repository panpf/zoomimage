package com.github.panpf.zoomimage.core.test.subsampling

import androidx.exifinterface.media.ExifInterface
import com.github.panpf.zoomimage.subsampling.ImageInfo
import org.junit.Assert
import org.junit.Test

class ImageInfoTest {

    @Test
    fun testToString(){
        Assert.assertEquals(
            "ImageInfo(size=100x1200, mimeType='image/jpeg', exifOrientation=UNDEFINED)",
            ImageInfo(100, 1200, "image/jpeg", 0).toString()
        )

        Assert.assertEquals(
            "ImageInfo(size=1200x100, mimeType='image/png', exifOrientation=ROTATE_270)",
            ImageInfo(1200, 100, "image/png", ExifInterface.ORIENTATION_ROTATE_270).toString()
        )
    }

    @Test
    fun testToShortString(){
        Assert.assertEquals(
            "(100x1200,'image/jpeg',UNDEFINED)",
            ImageInfo(100, 1200, "image/jpeg", 0).toShortString()
        )

        Assert.assertEquals(
            "(1200x100,'image/png',ROTATE_270)",
            ImageInfo(1200, 100, "image/png", ExifInterface.ORIENTATION_ROTATE_270).toShortString()
        )
    }
}