package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import org.junit.Assert
import org.junit.Test

class IntOffsetCompatTest {
    // todo Implementation tests

    @Test
    fun testRotateInSpaceAndReverseRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)
        val offset = IntOffsetCompat(600, 200)

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