package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TILE_COLOR_GREEN
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_LIGHT_GRAY
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_RED
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_SKY_BLUE
import com.github.panpf.zoomimage.subsampling.TILE_COLOR_YELLOW
import com.github.panpf.zoomimage.subsampling.TileImageFrom
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.tileColor
import kotlin.test.Test
import kotlin.test.assertEquals

class TileColorTest {

    @Test
    fun testTileColor() {
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_NONE,
                from = TileImageFrom.UNKNOWN,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_NONE,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(
                state = TileState.STATE_NONE,
                from = TileImageFrom.UNKNOWN,
                withinLoadArea = true
            )
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(
                state = TileState.STATE_NONE,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = true
            )
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                TileState.STATE_LOADING,
                from = TileImageFrom.UNKNOWN,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                TileState.STATE_LOADING,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_YELLOW,
            actual = tileColor(
                state = TileState.STATE_LOADING,
                from = TileImageFrom.UNKNOWN,
                withinLoadArea = true
            )
        )
        assertEquals(
            expected = TILE_COLOR_YELLOW,
            actual = tileColor(
                TileState.STATE_LOADING,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = true
            )
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_LOADED,
                from = TileImageFrom.UNKNOWN,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_LOADED,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_SKY_BLUE,
            actual = tileColor(
                TileState.STATE_LOADED,
                from = TileImageFrom.LOCAL,
                withinLoadArea = true
            )
        )
        assertEquals(
            expected = TILE_COLOR_GREEN,
            actual = tileColor(
                state = TileState.STATE_LOADED,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = true
            )
        )

        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_ERROR,
                from = TileImageFrom.LOCAL,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_LIGHT_GRAY,
            actual = tileColor(
                state = TileState.STATE_ERROR,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = false
            )
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(
                state = TileState.STATE_ERROR,
                from = TileImageFrom.LOCAL,
                withinLoadArea = true
            )
        )
        assertEquals(
            expected = TILE_COLOR_RED,
            actual = tileColor(
                state = TileState.STATE_ERROR,
                from = TileImageFrom.MEMORY_CACHE,
                withinLoadArea = true
            )
        )
    }
}