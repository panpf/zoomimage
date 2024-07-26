package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.BitmapFrom
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_CYAN
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_GREEN
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_RED
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_SKY_BLUE
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_YELLOW_GREEN
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.tileStateColor
import kotlin.test.Test
import kotlin.test.assertEquals

class TileStateTest {

    @Test
    fun testValue() {
        assertEquals(expected = 0, actual = TileState.STATE_NONE)
        assertEquals(expected = 1, actual = TileState.STATE_LOADING)
        assertEquals(expected = 2, actual = TileState.STATE_LOADED)
        assertEquals(expected = 3, actual = TileState.STATE_ERROR)
    }

    @Test
    fun testName() {
        assertEquals(expected = "NONE", actual = TileState.name(TileState.STATE_NONE))
        assertEquals(expected = "LOADING", actual = TileState.name(TileState.STATE_LOADING))
        assertEquals(expected = "LOADED", actual = TileState.name(TileState.STATE_LOADED))
        assertEquals(expected = "ERROR", actual = TileState.name(TileState.STATE_ERROR))
        assertEquals(expected = "UNKNOWN", actual = TileState.name(TileState.STATE_NONE - 1))
        assertEquals(expected = "UNKNOWN", actual = TileState.name(TileState.STATE_ERROR + 1))
    }

    @Test
    fun testTileStateColor() {
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_NONE, withinLoadArea = false, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_NONE, withinLoadArea = false, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileStateColor(TileState.STATE_NONE, withinLoadArea = true, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileStateColor(TileState.STATE_NONE, withinLoadArea = true, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )

        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_LOADING, withinLoadArea = false, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_LOADING, withinLoadArea = false, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )
        assertEquals(
            expected = TILE_COLOR_CYAN,
            actual = tileStateColor(TileState.STATE_LOADING, withinLoadArea = true, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_CYAN,
            actual = tileStateColor(TileState.STATE_LOADING, withinLoadArea = true, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )

        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_LOADED, withinLoadArea = false, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_LOADED, withinLoadArea = false, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )
        assertEquals(
            expected = TILE_COLOR_YELLOW_GREEN,
            actual = tileStateColor(TileState.STATE_LOADED, withinLoadArea = true, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_GREEN,
            actual = tileStateColor(TileState.STATE_LOADED, withinLoadArea = true, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )

        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_ERROR, withinLoadArea = false, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileStateColor(TileState.STATE_ERROR, withinLoadArea = false, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileStateColor(TileState.STATE_ERROR, withinLoadArea = true, bitmapFrom = BitmapFrom.LOCAL)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileStateColor(TileState.STATE_ERROR, withinLoadArea = true, bitmapFrom = BitmapFrom.MEMORY_CACHE)
        )
    }
}