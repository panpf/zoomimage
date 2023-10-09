package com.github.panpf.zoomimage.core.test.subsampling

import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import org.junit.Assert
import org.junit.Test

class TileAnimationSpecTest {

    @Test
    fun test() {
        Assert.assertEquals(200L, TileAnimationSpec.DEFAULT_DURATION)
        Assert.assertEquals(8L, TileAnimationSpec.DEFAULT_INTERVAL)

        TileAnimationSpec.Default.apply {
            Assert.assertEquals(TileAnimationSpec.DEFAULT_DURATION, duration)
            Assert.assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec.None.apply {
            Assert.assertEquals(0L, duration)
            Assert.assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec().apply {
            Assert.assertEquals(TileAnimationSpec.DEFAULT_DURATION, duration)
            Assert.assertEquals(TileAnimationSpec.DEFAULT_INTERVAL, interval)
        }

        TileAnimationSpec(duration = 400L, interval = 60L).apply {
            Assert.assertEquals(400L, duration)
            Assert.assertEquals(60L, interval)
        }
    }
}