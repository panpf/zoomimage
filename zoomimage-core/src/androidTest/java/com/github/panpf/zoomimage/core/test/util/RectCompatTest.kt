package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.reverseRotateInSpace
import com.github.panpf.zoomimage.util.rotateInSpace
import org.junit.Assert
import org.junit.Test

class RectCompatTest {

    @Test
    fun testRotateInSpaceAndReverseRotateInSpace() {
        val spaceSize = SizeCompat(1000f, 500f)
        val rect = RectCompat(600f, 200f, 800f, 400f)

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