package com.github.panpf.zoomimage.core.android.test.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.githb.panpf.zoomimage.images.ResourceImages
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoreUtilsAndroidTest {

    @Test
    fun testRequiredMainThread() {
        assertFailsWith(IllegalStateException::class) {
            requiredMainThread()
        }
        runBlocking(Dispatchers.Main) {
            requiredMainThread()
        }
    }

    @Test
    fun testRequiredWorkThread() {
        requiredWorkThread()

        assertFailsWith(IllegalStateException::class) {
            runBlocking(Dispatchers.Main) {
                requiredWorkThread()
            }
        }
    }

    @Test
    fun testSafeConfig() {
        assertEquals(
            Bitmap.Config.ARGB_8888,
            Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).safeConfig
        )

        assertEquals(
            Bitmap.Config.RGB_565,
            Bitmap.createBitmap(110, 210, Bitmap.Config.RGB_565).safeConfig
        )

        // Unable to create Bitmap with null config
    }

    @Test
    fun testToShortString() {
        assertEquals(
            "(110x210,ARGB_8888)",
            Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).toShortString()
        )

        assertEquals(
            "(210x110,RGB_565)",
            Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).toShortString()
        )
    }

    @Test
    fun testToLogString() {
        Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(110x210,ARGB_8888)",
                this.toLogString()
            )
        }

        Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(210x110,RGB_565)",
                this.toLogString()
            )
        }
    }

    @Test
    fun testIsAndSupportHardware() {
        Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            assertEquals(Bitmap.Config.ARGB_8888, this.config)
            assertFalse(this.config.isAndSupportHardware())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ResourceImages.dog.toImageSource().openSource().buffer().inputStream().use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.HARDWARE
                })
            }!!.apply {
                assertEquals(Bitmap.Config.HARDWARE, this.config)
                assertTrue(this.config.isAndSupportHardware())
            }
        }
    }
}