package com.github.panpf.zoomimage.core.desktop.test.subsampling

import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.test.TestTileImage
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
            assertNull(tileImage)
            assertFalse(animationState.running)
            assertEquals(expected = 255, actual = animationState.alpha)
        }

        val tileImage = TestTileImage("key")
        assertFalse(tileImage.displayed)
        tile.setTileImage(tileImage, allowAnimate = true)
        assertTrue(tileImage.displayed)
        tile.apply {
            assertNotNull(this.tileImage)
            assertTrue(animationState.running)
            assertEquals(expected = 0, actual = animationState.alpha)
        }
        val duration = 200L

        val alphas = mutableListOf<Int>()

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileImage)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileImage)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileImage)
        assertTrue(tile.animationState.running, "alpha=${tile.animationState.alpha}")
        alphas.add(tile.animationState.alpha)

        Thread.sleep(50)
        tile.animationState.calculate(duration)
        assertNotNull(tile.tileImage)
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

        tile.setTileImage(tileImage, allowAnimate = true)
        assertTrue(tileImage.displayed)
        tile.apply {
            assertNotNull(this.tileImage)
            assertFalse(animationState.running)
            assertEquals(expected = 255, actual = animationState.alpha)
        }

        val tileImage2 = TestTileImage("key")
        assertTrue(tileImage.displayed)
        assertFalse(tileImage2.displayed)
        tile.setTileImage(tileImage2, allowAnimate = false)
        assertFalse(tileImage.displayed)
        assertTrue(tileImage2.displayed)
        tile.apply {
            assertNotNull(this.tileImage)
            assertFalse(animationState.running)
            assertEquals(expected = 255, actual = animationState.alpha)
        }

        val tileImage3 = TestTileImage("key")
        tile.setTileImage(tileImage3, allowAnimate = true)
        tile.apply {
            assertNotNull(this.tileImage)
            assertTrue(animationState.running)
            assertEquals(expected = 0, actual = animationState.alpha)
        }

        val tileImage4 = TestTileImage("key")
        tile.setTileImage(tileImage4, allowAnimate = false)
        tile.apply {
            assertNotNull(this.tileImage)
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