package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.zoomimage.util.Origin
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class ScaleFactorCompatTest {

    @Test
    fun testToShortString() {
        Assert.assertEquals(
            "10.34x9.77",
            ScaleFactorCompat(10.342f, 9.765f).toShortString()
        )
        Assert.assertEquals(
            "9.77x10.34",
            ScaleFactorCompat(9.765f, 10.342f).toShortString()
        )
    }

    @Test
    fun testCreate() {
        Assert.assertEquals(
            "3.3x3.3",
            ScaleFactorCompat(3.3f).toShortString()
        )
        Assert.assertEquals(
            "5.3x5.3",
            ScaleFactorCompat(5.3f).toShortString()
        )
    }

    @Test
    fun testOrigin() {
        Assert.assertEquals(
            "1.0x1.0",
            ScaleFactorCompat.Origin.toShortString()
        )
        Assert.assertEquals(
            ScaleFactorCompat.Origin,
            ScaleFactorCompat.Origin
        )
    }

    @Test
    fun testTimes() {
        Assert.assertEquals(
            "35.97x53.35",
            (ScaleFactorCompat(10.9f, 9.7f) * ScaleFactorCompat(3.3f, 5.5f)).toShortString()
        )
        Assert.assertEquals(
            "59.95x32.01",
            (ScaleFactorCompat(10.9f, 9.7f) * ScaleFactorCompat(5.5f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        Assert.assertEquals(
            "10.9x9.7",
            (ScaleFactorCompat(35.97f, 53.35f) / ScaleFactorCompat(3.3f, 5.5f)).toShortString()
        )
        Assert.assertEquals(
            "6.54x16.17",
            (ScaleFactorCompat(35.97f, 53.35f) / ScaleFactorCompat(5.5f, 3.3f)).toShortString()
        )
    }
}