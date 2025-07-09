package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlin.test.Test
import kotlin.test.assertEquals

class ContinuousTransformTypeTest {

    @Test
    fun testValue() {
        assertEquals(1, ContinuousTransformType.SCALE)
        assertEquals(2, ContinuousTransformType.OFFSET)
        assertEquals(4, ContinuousTransformType.LOCATE)
        assertEquals(8, ContinuousTransformType.GESTURE)
        assertEquals(16, ContinuousTransformType.FLING)
    }

    @Test
    fun testValues() {
        assertEquals(listOf(1, 2, 4, 8, 16), ContinuousTransformType.values)
    }

    @Test
    fun testName() {
        assertEquals("SCALE", ContinuousTransformType.name(ContinuousTransformType.SCALE))
        assertEquals("OFFSET", ContinuousTransformType.name(ContinuousTransformType.OFFSET))
        assertEquals("LOCATE", ContinuousTransformType.name(ContinuousTransformType.LOCATE))
        assertEquals("GESTURE", ContinuousTransformType.name(ContinuousTransformType.GESTURE))
        assertEquals("FLING", ContinuousTransformType.name(ContinuousTransformType.FLING))
        assertEquals("UNKNOWN", ContinuousTransformType.name(ContinuousTransformType.NONE))
        assertEquals("UNKNOWN", ContinuousTransformType.name(-1))
        assertEquals("UNKNOWN", ContinuousTransformType.name(ContinuousTransformType.FLING * 2))
    }

    @Test
    fun testParse() {
        assertEquals(
            expected = listOf(),
            actual = ContinuousTransformType.parse(0)
                .map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE"),
            actual = ContinuousTransformType.parse(ContinuousTransformType.SCALE)
                .map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("OFFSET"),
            actual = ContinuousTransformType.parse(ContinuousTransformType.OFFSET)
                .map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("LOCATE"),
            actual = ContinuousTransformType.parse(ContinuousTransformType.LOCATE)
                .map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("GESTURE"),
            actual = ContinuousTransformType.parse(ContinuousTransformType.GESTURE)
                .map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("FLING"),
            actual = ContinuousTransformType.parse(ContinuousTransformType.FLING)
                .map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "LOCATE"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.LOCATE
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "GESTURE"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.GESTURE
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "FLING"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.FLING
            ).map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "GESTURE"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.GESTURE
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "FLING"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.FLING
            ).map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE
            ).map { ContinuousTransformType.name(it) }
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "FLING"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.FLING
            ).map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE", "FLING"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
            ).map { ContinuousTransformType.name(it) }
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE", "FLING"),
            actual = ContinuousTransformType.parse(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING or ContinuousTransformType.FLING * 2
            ).map { ContinuousTransformType.name(it) }
        )
    }

    @Test
    fun testNames() {
        assertEquals(
            expected = listOf(),
            actual = ContinuousTransformType.names(0)
        )

        assertEquals(
            expected = listOf("SCALE"),
            actual = ContinuousTransformType.names(ContinuousTransformType.SCALE)
        )
        assertEquals(
            expected = listOf("OFFSET"),
            actual = ContinuousTransformType.names(ContinuousTransformType.OFFSET)
        )
        assertEquals(
            expected = listOf("LOCATE"),
            actual = ContinuousTransformType.names(ContinuousTransformType.LOCATE)
        )
        assertEquals(
            expected = listOf("GESTURE"),
            actual = ContinuousTransformType.names(ContinuousTransformType.GESTURE)
        )
        assertEquals(
            expected = listOf("FLING"),
            actual = ContinuousTransformType.names(ContinuousTransformType.FLING)
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET
            )
        )
        assertEquals(
            expected = listOf("SCALE", "LOCATE"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.LOCATE
            )
        )
        assertEquals(
            expected = listOf("SCALE", "GESTURE"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.GESTURE
            )
        )
        assertEquals(
            expected = listOf("SCALE", "FLING"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.FLING
            )
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE
            )
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "GESTURE"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.GESTURE
            )
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "FLING"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.FLING
            )
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE
            )
        )
        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "FLING"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.FLING
            )
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE", "FLING"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
            )
        )

        assertEquals(
            expected = listOf("SCALE", "OFFSET", "LOCATE", "GESTURE", "FLING"),
            actual = ContinuousTransformType.names(
                ContinuousTransformType.SCALE or ContinuousTransformType.OFFSET or ContinuousTransformType.LOCATE or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING or ContinuousTransformType.FLING * 2
            )
        )
    }
}