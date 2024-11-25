package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.Origin
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.isOrigin
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleFactorCompatTest {

    @Test
    fun testIsOrigin() {
        assertEquals(expected = true, actual = ScaleFactorCompat(1f).isOrigin())
        assertEquals(expected = true, actual = ScaleFactorCompat(0.995f).isOrigin())
        assertEquals(expected = false, actual = ScaleFactorCompat(0.994f).isOrigin())
    }

    @Test
    fun testToShortString() {
        assertEquals(
            "10.34x9.77",
            ScaleFactorCompat(10.342f, 9.765f).toShortString()
        )
        assertEquals(
            "9.77x10.34",
            ScaleFactorCompat(9.765f, 10.342f).toShortString()
        )
    }

    @Test
    fun testCreate() {
        assertEquals(
            "3.3x3.3",
            ScaleFactorCompat(3.3f).toShortString()
        )
        assertEquals(
            "5.3x5.3",
            ScaleFactorCompat(5.3f).toShortString()
        )
    }

    @Test
    fun testOrigin() {
        assertEquals(
            "1.0x1.0",
            ScaleFactorCompat.Origin.toShortString()
        )
        assertEquals(
            ScaleFactorCompat.Origin,
            ScaleFactorCompat.Origin
        )
    }

    @Test
    fun testTimes() {
        assertEquals(
            "35.97x53.35",
            (ScaleFactorCompat(10.9f, 9.7f) * ScaleFactorCompat(3.3f, 5.5f)).toShortString()
        )
        assertEquals(
            "59.95x32.01",
            (ScaleFactorCompat(10.9f, 9.7f) * ScaleFactorCompat(5.5f, 3.3f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "10.9x9.7",
            (ScaleFactorCompat(35.97f, 53.35f) / ScaleFactorCompat(3.3f, 5.5f)).toShortString()
        )
        assertEquals(
            "6.54x16.17",
            (ScaleFactorCompat(35.97f, 53.35f) / ScaleFactorCompat(5.5f, 3.3f)).toShortString()
        )
    }
}