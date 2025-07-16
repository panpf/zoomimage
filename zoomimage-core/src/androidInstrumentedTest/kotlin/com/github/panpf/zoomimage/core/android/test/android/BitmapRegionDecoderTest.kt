package com.github.panpf.zoomimage.core.android.test.android

import android.graphics.BitmapRegionDecoder
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import androidx.core.graphics.get
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.closeQuietly
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BitmapRegionDecoderTest {

    @Test
    fun testDecodeRegion() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val inputStream =
            imageFile.toImageSource().openSource().buffer().inputStream()
        var decoder: BitmapRegionDecoder? = null
        try {
            decoder = BitmapRegionDecoder.newInstance(inputStream, false)!!
            val srcRect = Rect(
                /* left = */ imageSize.width - tileSize.width,
                /* top = */ imageSize.height - tileSize.height,
                /* right = */ imageSize.width,
                /* bottom = */ imageSize.height
            )
            val bitmap = decoder.decodeRegion(srcRect, null)!!
            val color = bitmap[bitmap.width - 1, bitmap.height - 1]
            assertEquals(tileSize.width, bitmap.width)
            assertEquals(tileSize.height, bitmap.height)
            assertNotEquals(0, Color.red(color))
            assertNotEquals(0, Color.green(color))
            assertNotEquals(0, Color.blue(color))
        } finally {
            decoder?.recycle()
            inputStream.closeQuietly()
        }
    }

    @Test
    fun testDecodeRegionOutOfRange1() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val inputStream =
            imageFile.toImageSource().openSource().buffer().inputStream()
        var decoder: BitmapRegionDecoder? = null
        try {
            decoder = BitmapRegionDecoder.newInstance(inputStream, false)!!
            val srcRect = Rect(
                imageSize.width - tileSize.width,
                imageSize.height - tileSize.height,
                imageSize.width + 1,
                imageSize.height + 1
            )
            val bitmap = decoder.decodeRegion(srcRect, null)!!
            val color = bitmap[bitmap.width - 1, bitmap.height - 1]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   // API 24
                assertEquals(tileSize.width, bitmap.width)
                assertEquals(tileSize.height, bitmap.height)
                assertNotEquals(0, Color.red(color))
                assertNotEquals(0, Color.green(color))
                assertNotEquals(0, Color.blue(color))
            } else {
                assertEquals(tileSize.width + 1, bitmap.width)
                assertEquals(tileSize.height + 1, bitmap.height)
                assertEquals(0, Color.red(color))
                assertEquals(0, Color.green(color))
                assertEquals(0, Color.blue(color))
            }
        } finally {
            decoder?.recycle()
            inputStream.closeQuietly()
        }
    }

    @Test
    fun testDecodeRegionOutOfRange2() {
        val imageFile = ResourceImages.hugeChina
        val imageSize = imageFile.size  // 6799x4882
        val tileSize = IntSizeCompat(679, 1219)
        val inputStream =
            imageFile.toImageSource().openSource().buffer().inputStream()
        var decoder: BitmapRegionDecoder? = null
        try {
            decoder = BitmapRegionDecoder.newInstance(inputStream, false)!!
            val srcRect = Rect(
                imageSize.width - tileSize.width,
                imageSize.height - tileSize.height,
                imageSize.width + 2,
                imageSize.height + 2
            )
            val bitmap = decoder.decodeRegion(srcRect, null)!!
            val color1 = bitmap[bitmap.width - 1, bitmap.height - 1]
            val color2 = bitmap[bitmap.width - 2, bitmap.height - 2]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   // API 24
                assertEquals(tileSize.width, bitmap.width)
                assertEquals(tileSize.height, bitmap.height)
                assertNotEquals(0, Color.red(color1))
                assertNotEquals(0, Color.green(color1))
                assertNotEquals(0, Color.blue(color1))
                assertNotEquals(0, Color.red(color2))
                assertNotEquals(0, Color.green(color2))
                assertNotEquals(0, Color.blue(color2))
            } else {
                assertEquals(tileSize.width + 2, bitmap.width)
                assertEquals(tileSize.height + 2, bitmap.height)
                assertEquals(0, Color.red(color1))
                assertEquals(0, Color.green(color1))
                assertEquals(0, Color.blue(color1))
                assertEquals(0, Color.red(color2))
                assertEquals(0, Color.green(color2))
                assertEquals(0, Color.blue(color2))
            }
        } finally {
            decoder?.recycle()
            inputStream.closeQuietly()
        }
    }
}