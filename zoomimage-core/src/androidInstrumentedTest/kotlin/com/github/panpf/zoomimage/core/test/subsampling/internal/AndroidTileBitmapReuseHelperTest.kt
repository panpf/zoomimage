package com.github.panpf.zoomimage.core.test.subsampling.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.core.test.internal.LruTileBitmapPool
import com.github.panpf.zoomimage.core.test.internal.formatFileSize
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.internal.AndroidTileBitmapReuseHelper
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import org.junit.Assert
import org.junit.Test

class AndroidTileBitmapReuseHelperTest {

    @Test
    fun testSetInBitmapForRegion() {
        val bitmapPool = LruTileBitmapPool(10L * 1024 * 1024)
        val tileBitmapReuseSpec = TileBitmapReuseSpec()
        tileBitmapReuseSpec.tileBitmapPool = bitmapPool
        val bitmapPoolHelper = AndroidTileBitmapReuseHelper(Logger("test"), tileBitmapReuseSpec)

        bitmapPool.clear()
        BitmapFactory.Options().apply {
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertFalse(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(0, 100),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
        }

        bitmapPool.clear()
        BitmapFactory.Options().apply {
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertFalse(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(100, 0),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bitmapPool.clear()
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.HARDWARE
                Assert.assertNull(inBitmap)
                Assert.assertFalse(inMutable)
                Assert.assertFalse(
                    bitmapPoolHelper.setInBitmapForRegion(
                        options = this,
                        regionSize = IntSizeCompat(100, 100),
                        imageMimeType = "image/jpeg",
                        imageSize = IntSizeCompat(500, 300),
                        caller = "test"
                    )
                )
                Assert.assertNull(inBitmap)
                Assert.assertFalse(inMutable)
            }
        }

        bitmapPool.clear()
        BitmapFactory.Options().apply {
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertTrue(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(100, 100),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNotNull(inBitmap)
            Assert.assertFalse(inMutable)
        }

        bitmapPool.clear()
        bitmapPool.put(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        BitmapFactory.Options().apply {
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertTrue(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(100, 100),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNotNull(inBitmap)
            Assert.assertFalse(inMutable)
        }

        bitmapPool.clear()
        bitmapPool.put(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        BitmapFactory.Options().apply {
            inSampleSize = 2
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertTrue(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(200, 200),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNotNull(inBitmap)
            Assert.assertFalse(inMutable)
        }

        bitmapPool.clear()
        bitmapPool.put(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        BitmapFactory.Options().apply {
            inSampleSize = 2
            Assert.assertNull(inBitmap)
            Assert.assertFalse(inMutable)
            Assert.assertTrue(
                bitmapPoolHelper.setInBitmapForRegion(
                    options = this,
                    regionSize = IntSizeCompat(300, 300),
                    imageMimeType = "image/jpeg",
                    imageSize = IntSizeCompat(500, 300),
                    caller = "test"
                )
            )
            Assert.assertNotNull(inBitmap)
            Assert.assertFalse(inMutable)
        }
    }

    @Test
    fun testGetOrCreate() {
        val bitmapPool = LruTileBitmapPool(10L * 1024 * 1024)
        val tileBitmapReuseSpec = TileBitmapReuseSpec()
        tileBitmapReuseSpec.tileBitmapPool = bitmapPool
        val bitmapPoolHelper = AndroidTileBitmapReuseHelper(Logger("test"), tileBitmapReuseSpec)

        Assert.assertEquals("0B", bitmapPool.size.formatFileSize())
        Assert.assertFalse(bitmapPool.exist(100, 100, Bitmap.Config.ARGB_8888))
        Assert.assertNotNull(
            bitmapPoolHelper.getOrCreate(
                100,
                100,
                Bitmap.Config.ARGB_8888,
                caller = "test"
            )
        )
        Assert.assertEquals("0B", bitmapPool.size.formatFileSize())

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmapPool.put(bitmap)
        Assert.assertTrue(bitmapPool.exist(100, 100, Bitmap.Config.ARGB_8888))
        Assert.assertEquals("39.06KB", bitmapPool.size.formatFileSize())
        Assert.assertSame(
            bitmap,
            bitmapPoolHelper.getOrCreate(100, 100, Bitmap.Config.ARGB_8888, caller = "test")
        )
        Assert.assertEquals("0B", bitmapPool.size.formatFileSize())

        bitmapPool.put(bitmap)
        Assert.assertTrue(bitmapPool.exist(100, 100, Bitmap.Config.ARGB_8888))
        Assert.assertEquals("39.06KB", bitmapPool.size.formatFileSize())
        tileBitmapReuseSpec.disabled = true
        Assert.assertNotSame(
            bitmap,
            bitmapPoolHelper.getOrCreate(100, 100, Bitmap.Config.ARGB_8888, caller = "test")
        )
        Assert.assertEquals("39.06KB", bitmapPool.size.formatFileSize())
        tileBitmapReuseSpec.disabled = false
    }

    @Test
    fun testFree() {
        val bitmapPool = LruTileBitmapPool(10L * 1024 * 1024)
        val tileBitmapReuseSpec = TileBitmapReuseSpec()
        tileBitmapReuseSpec.tileBitmapPool = bitmapPool
        val bitmapPoolHelper = AndroidTileBitmapReuseHelper(Logger("test"), tileBitmapReuseSpec)

        Assert.assertEquals(0, bitmapPool.size)

        bitmapPoolHelper.freeBitmap(null, caller = "test")
        Assert.assertEquals(0, bitmapPool.size)

        bitmapPoolHelper.freeBitmap(
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply { recycle() },
            caller = "test"
        )
        Assert.assertEquals(0, bitmapPool.size)

        val resources = InstrumentationRegistry.getInstrumentation().context.resources
        bitmapPoolHelper.freeBitmap(
            BitmapFactory.decodeResource(resources, android.R.drawable.bottom_bar), caller = "test"
        )
        Thread.sleep(100)
        Assert.assertEquals(0, bitmapPool.size)

        bitmapPoolHelper.freeBitmap(
            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
            caller = "test"
        )
        Thread.sleep(100)
        Assert.assertTrue(bitmapPool.size > 0)
    }
}