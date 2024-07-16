package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.TopStart
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.lerp
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals

class TransformOriginCompatTest {

    @Test
    fun testToShortString() {
        assertEquals("0.34x0.57", TransformOriginCompat(0.342f, 0.567f).toShortString())
        assertEquals("0.57x0.34", TransformOriginCompat(0.567f, 0.342f).toShortString())
    }

    @Test
    fun testTopStart() {
        assertEquals(
            "0.0x0.0",
            TransformOriginCompat.TopStart.toShortString()
        )
        assertEquals(
            TransformOriginCompat.TopStart,
            TransformOriginCompat.TopStart
        )
    }

    @Test
    fun testTimes() {
        assertEquals(
            "1.13x1.87",
            (TransformOriginCompat(0.342f, 0.567f) * 3.3f).toShortString()
        )
        assertEquals(
            "1.81x3.01",
            (TransformOriginCompat(0.342f, 0.567f) * 5.3f).toShortString()
        )

        assertEquals(
            "251x221",
            (IntSizeCompat(735, 389) * TransformOriginCompat(0.342f, 0.567f)).toShortString()
        )
        assertEquals(
            "417x133",
            (IntSizeCompat(735, 389) * TransformOriginCompat(0.567f, 0.342f)).toShortString()
        )

        assertEquals(
            "251.54x220.96",
            (SizeCompat(735.5f, 389.7f) * TransformOriginCompat(0.342f, 0.567f)).toShortString()
        )
        assertEquals(
            "417.03x133.28",
            (SizeCompat(735.5f, 389.7f) * TransformOriginCompat(0.567f, 0.342f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        assertEquals(
            "0.34x0.57",
            (TransformOriginCompat(1.13f, 1.87f) / 3.3f).toShortString()
        )
        assertEquals(
            "0.21x0.35",
            (TransformOriginCompat(1.13f, 1.87f) / 5.3f).toShortString()
        )
    }

    @Test
    fun testLerp() {
        val origin1 = TransformOriginCompat(0.13f, 0.77f)
        val origin2 = TransformOriginCompat(0.77f, 0.13f)
        assertEquals(origin1, lerp(origin1, origin2, 0f))
        assertEquals(TransformOriginCompat(0.45f, 0.45f), lerp(origin1, origin2, 0.5f))
        assertEquals(origin2, lerp(origin1, origin2, 1f))
    }
}