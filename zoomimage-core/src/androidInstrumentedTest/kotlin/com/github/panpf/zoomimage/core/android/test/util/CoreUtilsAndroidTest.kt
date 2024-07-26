package com.github.panpf.zoomimage.core.android.test.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.isAndSupportHardware
import com.github.panpf.zoomimage.util.requiredMainThread
import com.github.panpf.zoomimage.util.requiredWorkThread
import com.github.panpf.zoomimage.util.safeConfig
import com.github.panpf.zoomimage.util.toLogString
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okio.buffer
import org.junit.Assert
import org.junit.Test

class CoreUtilsAndroidTest {

    @Test
    fun testRequiredMainThread() {
        assertThrow(IllegalStateException::class) {
            requiredMainThread()
        }
        runBlocking(Dispatchers.Main) {
            requiredMainThread()
        }
    }

    @Test
    fun testRequiredWorkThread() {
        requiredWorkThread()

        assertThrow(IllegalStateException::class) {
            runBlocking(Dispatchers.Main) {
                requiredWorkThread()
            }
        }
    }

    @Test
    fun testSafeConfig() {
        Assert.assertEquals(
            Bitmap.Config.ARGB_8888,
            Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).safeConfig
        )

        Assert.assertEquals(
            Bitmap.Config.RGB_565,
            Bitmap.createBitmap(110, 210, Bitmap.Config.RGB_565).safeConfig
        )

        // Unable to create Bitmap with null config
    }

    @Test
    fun testToShortString() {
        Assert.assertEquals(
            "(110x210,ARGB_8888)",
            Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).toShortString()
        )

        Assert.assertEquals(
            "(210x110,RGB_565)",
            Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).toShortString()
        )
    }

    @Test
    fun testToLogString() {
        Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).apply {
            Assert.assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(110x210,ARGB_8888)",
                this.toLogString()
            )
        }

        Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).apply {
            Assert.assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(210x110,RGB_565)",
                this.toLogString()
            )
        }
    }

    @Test
    fun testIsAndSupportHardware() {
        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            Assert.assertEquals(Bitmap.Config.ARGB_8888, this.config)
            Assert.assertFalse(this.config.isAndSupportHardware())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ResourceImages.dog.toImageSource().openSource().buffer().inputStream().use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.HARDWARE
                })
            }!!.apply {
                Assert.assertEquals(Bitmap.Config.HARDWARE, this.config)
                Assert.assertTrue(this.config.isAndSupportHardware())
            }
        }
    }
}