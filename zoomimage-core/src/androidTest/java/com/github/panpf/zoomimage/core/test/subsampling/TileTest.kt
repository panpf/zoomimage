package com.github.panpf.zoomimage.core.test.subsampling

import android.graphics.Bitmap
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmap
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import org.junit.Assert
import org.junit.Test

class TileTest {

    @Test
    fun test() {
        val tile = Tile(IntOffsetCompat(0, 1), IntRectCompat(0, 0, 100, 100), 2)
        tile.apply {
            Assert.assertNull(bitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val tileBitmap = TestTileBitmap("key", bitmap)
        Assert.assertFalse(tileBitmap.displayed)
        tile.setTileBitmap(tileBitmap, false)
        Assert.assertTrue(tileBitmap.displayed)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertTrue(animationState.running)
            Assert.assertEquals(0, animationState.alpha)
        }
        val duration = 200L

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(67f, animationState.alpha.toFloat(), 10f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(127f, animationState.alpha.toFloat(), 10f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(194f, animationState.alpha.toFloat(), 10f)
        }

        Thread.sleep(100)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        tile.setTileBitmap(tileBitmap, false)
        Assert.assertTrue(tileBitmap.displayed)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val tileBitmap2 = TestTileBitmap("key", bitmap)
        Assert.assertTrue(tileBitmap.displayed)
        Assert.assertFalse(tileBitmap2.displayed)
        tile.setTileBitmap(tileBitmap2, true)
        Assert.assertFalse(tileBitmap.displayed)
        Assert.assertTrue(tileBitmap2.displayed)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val tileBitmap3 = TestTileBitmap("key", bitmap)
        tile.setTileBitmap(tileBitmap3, false)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertTrue(animationState.running)
            Assert.assertEquals(0, animationState.alpha)
        }

        val tileBitmap4 = TestTileBitmap("key", bitmap)
        tile.setTileBitmap(tileBitmap4, true)
        tile.apply {
            Assert.assertNotNull(bitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }
    }

    class TestTileBitmap(override val key: String, override val bitmap: Bitmap?) : TileBitmap {
        var displayed: Boolean = false
        override fun setIsDisplayed(displayed: Boolean) {
            this.displayed = displayed
        }
    }
}