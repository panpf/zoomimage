package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileState
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
}