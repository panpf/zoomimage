package com.github.panpf.zoomimage.core.glide.test

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.EngineResourceWrapper
import com.bumptech.glide.load.engine.GlideEngine
import com.bumptech.glide.load.engine.createGlideEngine
import com.bumptech.glide.load.engine.newEngineKey
import com.github.panpf.zoomimage.glide.GlideTileBitmap
import com.github.panpf.zoomimage.glide.internal.toLogString
import com.github.panpf.zoomimage.subsampling.BitmapFrom
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

class GlideTileBitmapTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        GlideTileBitmap(resource1, "resource1", BitmapFrom.LOCAL).apply {
            assertSame(resource1.bitmap, bitmap1)
            assertEquals("resource1", key)
            assertEquals(bitmapFrom, BitmapFrom.LOCAL)
        }
        GlideTileBitmap(resource2, "resource2", BitmapFrom.MEMORY_CACHE).apply {
            assertSame(resource2.bitmap, bitmap2)
            assertEquals("resource2", key)
            assertEquals(bitmapFrom, BitmapFrom.MEMORY_CACHE)
        }
    }

    @Test
    fun testWidthHeightByteCount() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap12 = Bitmap.createBitmap(1101, 703, Bitmap.Config.RGB_565)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource12 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap12, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val androidTileBitmap1 = GlideTileBitmap(resource1, "resource1", BitmapFrom.LOCAL).apply {
            assertEquals(resource1.bitmap.width, width)
            assertEquals(resource1.bitmap.height, height)
            assertEquals(resource1.bitmap.byteCount.toLong(), byteCount)
        }
        val androidTileBitmap12 =
            GlideTileBitmap(resource12, "resource12", BitmapFrom.LOCAL).apply {
                assertEquals(resource12.bitmap.width, width)
                assertEquals(resource12.bitmap.height, height)
                assertEquals(resource12.bitmap.byteCount.toLong(), byteCount)
            }
        val androidTileBitmap2 = GlideTileBitmap(resource2, "resource2", BitmapFrom.LOCAL).apply {
            assertEquals(resource2.bitmap.width, width)
            assertEquals(resource2.bitmap.height, height)
            assertEquals(resource2.bitmap.byteCount.toLong(), byteCount)
        }

        assertEquals(androidTileBitmap1.width, androidTileBitmap12.width)
        assertEquals(androidTileBitmap1.height, androidTileBitmap12.height)
        assertNotEquals(androidTileBitmap1.byteCount, androidTileBitmap12.byteCount)

        assertNotEquals(androidTileBitmap1.width, androidTileBitmap2.width)
        assertNotEquals(androidTileBitmap1.height, androidTileBitmap2.height)
        assertNotEquals(androidTileBitmap1.byteCount, androidTileBitmap2.byteCount)

        assertNotEquals(androidTileBitmap2.width, androidTileBitmap12.width)
        assertNotEquals(androidTileBitmap2.height, androidTileBitmap12.height)
        assertNotEquals(androidTileBitmap2.byteCount, androidTileBitmap12.byteCount)
    }

    @Test
    fun testRecycle() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val resource =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val tileBitmap = GlideTileBitmap(resource, "resource1", BitmapFrom.LOCAL)
        assertEquals(false, tileBitmap.isRecycled)
        tileBitmap.recycle()
        assertEquals(true, tileBitmap.isRecycled)
    }

    @Test
    fun testEqualsAndHashCode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val tileBitmap1 = GlideTileBitmap(resource1, "resource1", BitmapFrom.LOCAL)
        val tileBitmap12 = GlideTileBitmap(resource1, "resource1", BitmapFrom.LOCAL)
        val tileBitmap2 = GlideTileBitmap(resource2, "resource2", BitmapFrom.LOCAL)
        val tileBitmap3 = GlideTileBitmap(resource1, "bitmap3", BitmapFrom.LOCAL)
        val tileBitmap4 = GlideTileBitmap(resource1, "resource1", BitmapFrom.MEMORY_CACHE)

        assertEquals(expected = tileBitmap1, actual = tileBitmap12)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap2)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap3)
        assertNotEquals(illegal = tileBitmap1, actual = tileBitmap4)
        assertNotEquals(illegal = tileBitmap2, actual = tileBitmap3)
        assertNotEquals(illegal = tileBitmap2, actual = tileBitmap4)
        assertNotEquals(illegal = tileBitmap3, actual = tileBitmap4)

        assertEquals(expected = tileBitmap1.hashCode(), actual = tileBitmap12.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap2.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap3.hashCode())
        assertNotEquals(illegal = tileBitmap1.hashCode(), actual = tileBitmap4.hashCode())
        assertNotEquals(illegal = tileBitmap2.hashCode(), actual = tileBitmap3.hashCode())
        assertNotEquals(illegal = tileBitmap2.hashCode(), actual = tileBitmap4.hashCode())
        assertNotEquals(illegal = tileBitmap3.hashCode(), actual = tileBitmap4.hashCode())
    }

    @Test
    fun testToString() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val glideEngine: GlideEngine by lazy {
            createGlideEngine(glide)!!
        }

        val bitmap1 = Bitmap.createBitmap(1101, 703, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(507, 1305, Bitmap.Config.ARGB_8888)
        val resource1 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap1, newEngineKey("key1")))
        val resource2 =
            EngineResourceWrapper(glideEngine.newEngineResource(bitmap2, newEngineKey("key1")))

        val tileBitmap1 = GlideTileBitmap(resource1, "resource1", BitmapFrom.LOCAL)
        val tileBitmap2 = GlideTileBitmap(resource2, "resource2", BitmapFrom.MEMORY_CACHE)

        assertEquals(
            expected = "GlideTileBitmap(key='resource1', bitmap=${resource1.bitmap.toLogString()}, bitmapFrom=LOCAL)",
            actual = tileBitmap1.toString()
        )
        assertEquals(
            expected = "GlideTileBitmap(key='resource2', bitmap=${resource2.bitmap.toLogString()}, bitmapFrom=MEMORY_CACHE)",
            actual = tileBitmap2.toString()
        )
    }
}