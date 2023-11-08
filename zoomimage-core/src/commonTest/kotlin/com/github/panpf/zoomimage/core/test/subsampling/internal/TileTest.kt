package com.github.panpf.zoomimage.core.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.CacheTileBitmap
import com.github.panpf.zoomimage.subsampling.internal.Tile
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import org.junit.Assert
import org.junit.Test

class TileTest {

    @Test
    fun test() {
        val tile = Tile(IntOffsetCompat(0, 1), IntRectCompat(0, 0, 100, 100), 2)
        tile.apply {
            Assert.assertNull(tileBitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val tileBitmap = TestTileBitmap("key")
        Assert.assertFalse(tileBitmap.displayed)
        tile.setTileBitmap(tileBitmap, false)
        Assert.assertTrue(tileBitmap.displayed)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertTrue(animationState.running)
            Assert.assertEquals(0, animationState.alpha)
        }
        val duration = 200L

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(67f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(127f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertTrue("alpha=${animationState.alpha}", animationState.running)
            Assert.assertEquals(194f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(100)
        tile.animationState.calculate(duration)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        tile.setTileBitmap(tileBitmap, false)
        Assert.assertTrue(tileBitmap.displayed)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val tileBitmap2 = TestTileBitmap("key")
        Assert.assertTrue(tileBitmap.displayed)
        Assert.assertFalse(tileBitmap2.displayed)
        tile.setTileBitmap(tileBitmap2, true)
        Assert.assertFalse(tileBitmap.displayed)
        Assert.assertTrue(tileBitmap2.displayed)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }

        val tileBitmap3 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap3, false)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertTrue(animationState.running)
            Assert.assertEquals(0, animationState.alpha)
        }

        val tileBitmap4 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap4, true)
        tile.apply {
            Assert.assertNotNull(this.tileBitmap)
            Assert.assertFalse(animationState.running)
            Assert.assertEquals(255, animationState.alpha)
        }
    }

    class TestTileBitmap(override val key: String) : CacheTileBitmap {
        var displayed: Boolean = false

        override val width: Int = 0

        override val height: Int = 0

        override val byteCount: Int = 0

        override val isRecycled: Boolean
            get() = true

        override fun recycle() {}

        override fun setIsDisplayed(displayed: Boolean) {
            this.displayed = displayed
        }
    }
}