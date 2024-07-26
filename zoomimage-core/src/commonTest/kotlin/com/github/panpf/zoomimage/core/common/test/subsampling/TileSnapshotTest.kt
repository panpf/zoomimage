package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.internal.Tile
import com.github.panpf.zoomimage.subsampling.toSnapshot
import com.github.panpf.zoomimage.test.TestTileBitmap
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class TileSnapshotTest {

    @Test
    fun testToSnapshot() {
        val tileBitmap1 = TestTileBitmap("111")
        val tileBitmap2 = TestTileBitmap("222")
        val tile1 = Tile(
            coordinate = IntOffsetCompat(3, 5),
            srcRect = IntRectCompat(1, 5, 99, 57),
            sampleSize = 4
        ).apply {
            setTileBitmap(tileBitmap1, allowAnimate = false)
        }
        val tile2 = Tile(
            coordinate = IntOffsetCompat(7, 1),
            srcRect = IntRectCompat(135, 589, 555, 779),
            sampleSize = 8
        ).apply {
            setTileBitmap(tileBitmap2, allowAnimate = false)
        }

        assertNotEquals(illegal = tile1.coordinate, actual = tile2.coordinate)
        assertNotEquals(illegal = tile1.srcRect, actual = tile2.srcRect)
        assertNotEquals(illegal = tile1.sampleSize, actual = tile2.sampleSize)
        assertNotEquals(illegal = tile1.tileBitmap, actual = tile2.tileBitmap)
        assertEquals(expected = tile1.state, actual = tile2.state)
        assertEquals(expected = tile1.animationState.alpha, actual = tile2.animationState.alpha)

        val tileSnapshot1 = tile1.toSnapshot().apply {
            assertEquals(expected = tile1.coordinate, actual = coordinate)
            assertEquals(expected = tile1.srcRect, actual = srcRect)
            assertEquals(expected = tile1.sampleSize, actual = sampleSize)
            assertSame(expected = tile1.tileBitmap, actual = tileBitmap)
            assertEquals(expected = tile1.state, actual = state)
            assertEquals(expected = tile1.animationState.alpha, actual = alpha)
        }
        val tileSnapshot12 = tileSnapshot1.copy()
        val tileSnapshot2 = tile2.toSnapshot().apply {
            assertEquals(expected = tile2.coordinate, actual = coordinate)
            assertEquals(expected = tile2.srcRect, actual = srcRect)
            assertEquals(expected = tile2.sampleSize, actual = sampleSize)
            assertSame(expected = tile2.tileBitmap, actual = tileBitmap)
            assertEquals(expected = tile2.state, actual = state)
            assertEquals(expected = tile2.animationState.alpha, actual = alpha)
        }

        assertEquals(expected = tileSnapshot1, actual = tileSnapshot12)
        assertNotSame(illegal = tileSnapshot1, actual = tileSnapshot12)
        assertNotEquals(illegal = tileSnapshot1, actual = tileSnapshot2)
    }
}