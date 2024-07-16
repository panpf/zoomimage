package com.github.panpf.zoomimage.core.common.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import kotlin.test.Test
import kotlin.test.assertEquals

class TileAnimationSpecTest {

    @Test
    fun test() {
        assertEquals(200L, TileAnimationSpec.DEFAULT_DURATION)
        assertEquals(8L, TileAnimationSpec.DEFAULT_INTERVAL)

        TileAnimationSpec.Default.apply {
            assertEquals(TileAnimationSpec.DEFAULT_DURATION, duration)
            assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec.None.apply {
            assertEquals(0L, duration)
            assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec().apply {
            assertEquals(TileAnimationSpec.DEFAULT_DURATION, duration)
            assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec(duration = 400L, interval = 60L).apply {
            assertEquals(400L, duration)
            assertEquals(60L, interval)
        }
    }
}