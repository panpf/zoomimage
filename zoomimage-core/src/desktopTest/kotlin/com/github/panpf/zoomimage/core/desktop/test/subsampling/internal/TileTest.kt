package com.github.panpf.zoomimage.core.desktop.test.subsampling.internal

import com.github.panpf.zoomimage.subsampling.internal.Tile
import com.github.panpf.zoomimage.test.TestTileBitmap
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
            assertEquals(expected = 255, actual = animationState.alpha)
        }

        val tileBitmap = TestTileBitmap("key")
        assertFalse(tileBitmap.displayed)
        tile.setTileBitmap(tileBitmap, allowAnimate = true)
        assertTrue(tileBitmap.displayed)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running)
            assertEquals(expected = 0, actual = animationState.alpha)
        }
        val duration = 200L

        val alphas = mutableListOf<Int>()

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileBitmap)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileBitmap)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileBitmap)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileBitmap)
        assertFalse(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        assertTrue(actual = alphas.first() > 0)
        assertEquals(expected = 255, actual = alphas.last())
        var result = true
        alphas.forEachIndexed { index, i ->
            if (index > 0) {
                result = index and alphas[index - 1] < i
            }
        }
        assertTrue(result, message = "alphas=$alphas")

        tile.setTileBitmap(tileBitmap, allowAnimate = true)
        assertTrue(tileBitmap.displayed)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(expected = 255, actual = animationState.alpha)
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
            assertEquals(expected = 255, actual = animationState.alpha)
        }

        val tileBitmap3 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap3, allowAnimate = true)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertTrue(animationState.running)
            assertEquals(expected = 0, actual = animationState.alpha)
        }

        val tileBitmap4 = TestTileBitmap("key")
        tile.setTileBitmap(tileBitmap4, allowAnimate = false)
        tile.apply {
            assertNotNull(this.tileBitmap)
            assertFalse(animationState.running)
            assertEquals(expected = 255, actual = animationState.alpha)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val tile1 = Tile(
            coordinate = IntOffsetCompat(0, 1),
            srcRect = IntRectCompat(0, 0, 100, 100),
            sampleSize = 2
        )
        val tile12 = Tile(
            coordinate = IntOffsetCompat(0, 1),
            srcRect = IntRectCompat(0, 0, 100, 100),
            sampleSize = 2
        )
        val tile2 = Tile(
            coordinate = IntOffsetCompat(1, 1),
            srcRect = IntRectCompat(0, 0, 100, 100),
            sampleSize = 2
        )
        val tile3 = Tile(
            coordinate = IntOffsetCompat(0, 1),
            srcRect = IntRectCompat(50, 50, 100, 100),
            sampleSize = 2
        )
        val tile4 = Tile(
            coordinate = IntOffsetCompat(0, 1),
            srcRect = IntRectCompat(0, 0, 100, 100),
            sampleSize = 4
        )

        assertEquals(expected = tile1, actual = tile1)
        assertEquals(expected = tile1, actual = tile12)
        assertNotEquals(illegal = tile1, actual = null as Any?)
        assertNotEquals(illegal = tile1, actual = Any())
        assertNotEquals(illegal = tile1, actual = tile2)
        assertNotEquals(illegal = tile1, actual = tile3)
        assertNotEquals(illegal = tile1, actual = tile4)
        assertNotEquals(illegal = tile2, actual = tile3)
        assertNotEquals(illegal = tile2, actual = tile4)
        assertNotEquals(illegal = tile3, actual = tile4)

        assertEquals(expected = tile1.hashCode(), actual = tile1.hashCode())
        assertEquals(expected = tile1.hashCode(), actual = tile12.hashCode())
        assertNotEquals(illegal = tile1.hashCode(), actual = tile2.hashCode())
        assertNotEquals(illegal = tile1.hashCode(), actual = tile3.hashCode())
        assertNotEquals(illegal = tile1.hashCode(), actual = tile4.hashCode())
        assertNotEquals(illegal = tile2.hashCode(), actual = tile3.hashCode())
        assertNotEquals(illegal = tile2.hashCode(), actual = tile4.hashCode())
        assertNotEquals(illegal = tile3.hashCode(), actual = tile4.hashCode())
    }
}