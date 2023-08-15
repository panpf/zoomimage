package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import org.junit.Assert
import org.junit.Test

class OffsetCompatTest {
    // todo Implementation tests

    @Test
    fun testRotateInSpaceAndReverseRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)
        val offset = OffsetCompat(600f, 200f)

        var rotation = 90
        Assert.assertNotEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )

        rotation = 180
        Assert.assertNotEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )

        rotation = 270
        Assert.assertNotEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            offset,
            offset.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )
    }
}