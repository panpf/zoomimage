package com.github.panpf.zoomimage.core.test

import com.github.panpf.zoomimage.internal.calculateNextStepScale
import org.junit.Assert
import org.junit.Test

class ZoomUtilsTest {

    @Test
    fun testCalculateNextStepScale() {
        val stepScales = floatArrayOf(1f, 2f, 3f, 4f, 5f)
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.0f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.8f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 0.8f, rangeOfError = 0.2f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 0.9f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 0.9f, rangeOfError = 0f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 1.0f))
        Assert.assertEquals(2f, calculateNextStepScale(stepScales, 1.5f))
        Assert.assertEquals(3f, calculateNextStepScale(stepScales, 2.5f))
        Assert.assertEquals(4f, calculateNextStepScale(stepScales, 3.5f))
        Assert.assertEquals(5f, calculateNextStepScale(stepScales, 4.5f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 5.5f))
        Assert.assertEquals(1f, calculateNextStepScale(stepScales, 6.5f))

        Assert.assertEquals(0.0f, calculateNextStepScale(floatArrayOf(), 0.0f))
        Assert.assertEquals(0.8f, calculateNextStepScale(floatArrayOf(), 0.8f))
        Assert.assertEquals(0.9f, calculateNextStepScale(floatArrayOf(), 0.9f))
        Assert.assertEquals(1.0f, calculateNextStepScale(floatArrayOf(), 1.0f))
        Assert.assertEquals(2.5f, calculateNextStepScale(floatArrayOf(), 2.5f))
        Assert.assertEquals(3.5f, calculateNextStepScale(floatArrayOf(), 3.5f))
    }
}