package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import org.junit.Assert
import org.junit.Test

class IntRectCompatTest {

    @Test
    fun testRotateInSpaceAndReverseRotateInSpace() {
        val spaceSize = IntSizeCompat(1000, 500)
        val rect = IntRectCompat(600, 200, 800, 400)

        var rotation = 90
        Assert.assertNotEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )

        rotation = 180
        Assert.assertNotEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )

        rotation = 270
        Assert.assertNotEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation)
        )
        Assert.assertEquals(
            rect,
            rect.rotateInSpace(spaceSize, rotation).reverseRotateInSpace(spaceSize, rotation)
        )
    }
}