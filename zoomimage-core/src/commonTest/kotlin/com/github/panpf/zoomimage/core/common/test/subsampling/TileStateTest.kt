package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TILE_COLOR_GREEN
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_LIGHT_GRAY
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_RED
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_SKY_BLUE
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_YELLOW
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.tileColor
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
    fun testTileColor() {
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_NONE, withinLoadArea = false, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_NONE, withinLoadArea = false, fromCache = true)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(TileState.STATE_NONE, withinLoadArea = true, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(TileState.STATE_NONE, withinLoadArea = true, fromCache = true)
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_LOADING, withinLoadArea = false, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_LOADING, withinLoadArea = false, fromCache = true)
        )
        assertEquals(
            expected = TILE_COLOR_YELLOW,
            actual = tileColor(TileState.STATE_LOADING, withinLoadArea = true, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_YELLOW,
            actual = tileColor(TileState.STATE_LOADING, withinLoadArea = true, fromCache = true)
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_LOADED, withinLoadArea = false, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_LOADED, withinLoadArea = false, fromCache = true)
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileColor(TileState.STATE_LOADED, withinLoadArea = true, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_GREEN,
            actual = tileColor(TileState.STATE_LOADED, withinLoadArea = true, fromCache = true)
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_ERROR, withinLoadArea = false, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(TileState.STATE_ERROR, withinLoadArea = false, fromCache = true)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(TileState.STATE_ERROR, withinLoadArea = true, fromCache = false)
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(TileState.STATE_ERROR, withinLoadArea = true, fromCache = true)
        )
    }
}