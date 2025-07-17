package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileImageFrom
import kotlin.test.Test
import kotlin.test.assertEquals

class TileImageFromTest {

    @Test
    fun testValue() {
        assertEquals(expected = 0, actual = TileImageFrom.UNKNOWN)
        assertEquals(expected = 1, actual = TileImageFrom.MEMORY_CACHE)
        assertEquals(expected = 2, actual = TileImageFrom.LOCAL)
    }

    @Test
    fun testName() {
        assertEquals(expected = "UNKNOWN", actual = TileImageFrom.name(TileImageFrom.UNKNOWN))
        assertEquals(
            expected = "MEMORY_CACHE",
            actual = TileImageFrom.name(TileImageFrom.MEMORY_CACHE)
        )
        assertEquals(expected = "LOCAL", actual = TileImageFrom.name(TileImageFrom.LOCAL))
        assertEquals(expected = "UNKNOWN", actual = TileImageFrom.name(TileImageFrom.UNKNOWN - 1))
    }
}