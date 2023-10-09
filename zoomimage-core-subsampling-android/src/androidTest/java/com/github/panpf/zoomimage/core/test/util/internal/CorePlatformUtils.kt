package com.github.panpf.zoomimage.core.test.util.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.internal.isAndSupportHardware
import com.github.panpf.zoomimage.util.internal.requiredMainThread
import com.github.panpf.zoomimage.util.internal.requiredWorkThread
import com.github.panpf.zoomimage.util.internal.safeConfig
import com.github.panpf.zoomimage.util.internal.toHexShortString
import com.github.panpf.zoomimage.util.internal.toShortString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class CorePlatformUtils {

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
    fun testToHexString() {
        Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).apply {
            Assert.assertEquals(
                "(110x210,ARGB_8888,@${Integer.toHexString(this.hashCode())})",
                this.toHexShortString()
            )
        }

        Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).apply {
            Assert.assertEquals(
                "(210x110,RGB_565,@${Integer.toHexString(this.hashCode())})",
                this.toHexShortString()
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
            val context = InstrumentationRegistry.getInstrumentation().context
            context.assets.open("sample_dog.jpg").use {
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