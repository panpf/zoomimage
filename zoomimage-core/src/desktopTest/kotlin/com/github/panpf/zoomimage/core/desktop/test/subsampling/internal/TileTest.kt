package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.Tile
import com.github.panpf.zoomimage.test.TestTileBitmap
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TileTest {

    @Test
    fun test() {
        val tile = Tile(
            coordinate = IntOffsetCompat(0, 1),
            srcRect = IntRectCompat(0, 0, 100, 100),
            sampleSize = 2
        )
        tile.apply {
            assertNull(tileBitmap)
            assertFalse(animationState.running)
            assertEquals(255, animationState.alpha)
        }

        val tileBitmap = TestTileBitmap("key")
        assertFalse(tileBitmap.displayed)
        tile.setTileBitmap(tileBitmap, allowAnimate = true)
        assertTrue(tileBitmap.displayed)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running)
            assertEquals(0, animationState.alpha)
        }
        val duration = 200L

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running, "alpha=${animationState.alpha}")
            assertEquals(67f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running, "alpha=${animationState.alpha}")
            assertEquals(127f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running, "alpha=${animationState.alpha}")
            assertEquals(194f, animationState.alpha.toFloat(), 20f)
        }

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(255, animationState.alpha)
        }

        tile.setTileBitmap(tileBitmap, allowAnimate = true)
        assertTrue(tileBitmap.displayed)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(255, animationState.alpha)
        }

        val tileBitmap2 = TestTileBitmap("key")
        assertTrue(tileBitmap.displayed)
        assertFalse(tileBitmap2.displayed)
        tile.setTileBitmap(tileBitmap2, allowAnimate = false)
        assertFalse(tileBitmap.displayed)
        assertTrue(tileBitmap2.displayed)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(255, animationState.alpha)
        }

        val tileBitmap3 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap3, allowAnimate = true)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running)
            assertEquals(0, animationState.alpha)
        }

        val tileBitmap4 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap4, allowAnimate = false)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(255, animationState.alpha)
        }
    }
}