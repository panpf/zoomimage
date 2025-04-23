package com.github.panpf.zoomimage.core.common.test.zoom.internal

import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.zoom.internal.InitialZoom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InitialZoomTest {

    @Test
    fun testConstructor() {
        InitialZoom(
            minScale = 1.1f,
            mediumScale = 2.2f,
            maxScale = 3.3f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin,
        )
    }

    @Test
    fun testOrigin() {
        InitialZoom.Origin.apply {
            assertEquals(expected = 1.0f, actual = minScale)
            assertEquals(expected = 1.0f, actual = mediumScale)
            assertEquals(expected = 1.0f, actual = maxScale)
            assertEquals(expected = TransformCompat.Origin, actual = baseTransform)
            assertEquals(expected = TransformCompat.Origin, actual = userTransform)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val initialZoom1 = InitialZoom(
            minScale = 1.1f,
            mediumScale = 2.2f,
            maxScale = 3.3f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin,
        )
        val initialZoom11 = initialZoom1.copy()
        val initialZoom2 = initialZoom1.copy(minScale = 1.2f)
        val initialZoom3 = initialZoom1.copy(mediumScale = 2.3f)
        val initialZoom4 = initialZoom1.copy(maxScale = 3.4f)
        val initialZoom5 = initialZoom1.copy(
            baseTransform = initialZoom1.baseTransform.copy(
                scale = ScaleFactorCompat(5f),
                offset = OffsetCompat(101f, 202f)
            )
        )
        val initialZoom6 = initialZoom1.copy(
            userTransform = initialZoom1.userTransform.copy(
                scale = ScaleFactorCompat(6f),
                offset = OffsetCompat(201f, 302f)
            )
        )
        assertEquals(expected = initialZoom1, actual = initialZoom11)
        assertNotEquals(illegal = initialZoom1, actual = initialZoom2)
        assertNotEquals(illegal = initialZoom1, actual = initialZoom3)
        assertNotEquals(illegal = initialZoom1, actual = initialZoom4)
        assertNotEquals(illegal = initialZoom1, actual = initialZoom5)
        assertNotEquals(illegal = initialZoom1, actual = initialZoom6)
        assertNotEquals(illegal = initialZoom2, actual = initialZoom3)
        assertNotEquals(illegal = initialZoom2, actual = initialZoom4)
        assertNotEquals(illegal = initialZoom2, actual = initialZoom5)
        assertNotEquals(illegal = initialZoom2, actual = initialZoom6)
        assertNotEquals(illegal = initialZoom3, actual = initialZoom4)
        assertNotEquals(illegal = initialZoom3, actual = initialZoom5)
        assertNotEquals(illegal = initialZoom3, actual = initialZoom6)
        assertNotEquals(illegal = initialZoom4, actual = initialZoom5)
        assertNotEquals(illegal = initialZoom4, actual = initialZoom6)
        assertNotEquals(illegal = initialZoom5, actual = initialZoom6)

        assertEquals(expected = initialZoom1.hashCode(), actual = initialZoom11.hashCode())
        assertNotEquals(illegal = initialZoom1.hashCode(), actual = initialZoom2.hashCode())
        assertNotEquals(illegal = initialZoom1.hashCode(), actual = initialZoom3.hashCode())
        assertNotEquals(illegal = initialZoom1.hashCode(), actual = initialZoom4.hashCode())
        assertNotEquals(illegal = initialZoom1.hashCode(), actual = initialZoom5.hashCode())
        assertNotEquals(illegal = initialZoom1.hashCode(), actual = initialZoom6.hashCode())
        assertNotEquals(illegal = initialZoom2.hashCode(), actual = initialZoom3.hashCode())
        assertNotEquals(illegal = initialZoom2.hashCode(), actual = initialZoom4.hashCode())
        assertNotEquals(illegal = initialZoom2.hashCode(), actual = initialZoom5.hashCode())
        assertNotEquals(illegal = initialZoom2.hashCode(), actual = initialZoom6.hashCode())
        assertNotEquals(illegal = initialZoom3.hashCode(), actual = initialZoom4.hashCode())
        assertNotEquals(illegal = initialZoom3.hashCode(), actual = initialZoom5.hashCode())
        assertNotEquals(illegal = initialZoom3.hashCode(), actual = initialZoom6.hashCode())
        assertNotEquals(illegal = initialZoom4.hashCode(), actual = initialZoom5.hashCode())
        assertNotEquals(illegal = initialZoom4.hashCode(), actual = initialZoom6.hashCode())
        assertNotEquals(illegal = initialZoom5.hashCode(), actual = initialZoom6.hashCode())
    }

    @Test
    fun testToString() {
        val initialZoom1 = InitialZoom(
            minScale = 1.1f,
            mediumScale = 2.2f,
            maxScale = 3.3f,
            baseTransform = TransformCompat.Origin,
            userTransform = TransformCompat.Origin,
        )
        assertEquals(
            expected = "InitialZoom(minScale=1.1, mediumScale=2.2, maxScale=3.3, baseTransform=TransformCompat(scale=1.0x1.0, offset=0.0x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.0x0.0), userTransform=TransformCompat(scale=1.0x1.0, offset=0.0x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.0x0.0))",
            actual = initialZoom1.toString()
        )
    }
}