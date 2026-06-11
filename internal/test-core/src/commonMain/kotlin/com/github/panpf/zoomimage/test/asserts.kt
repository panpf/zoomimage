package com.github.panpf.zoomimage.test

import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun assertSizeEquals(
    expected: IntSizeCompat,
    actual: IntSizeCompat,
    delta: IntSizeCompat? = null,
    message: String? = null
) {
    if (delta != null) {
        assertTrue(
            actual = expected.width == actual.width
                    || expected.width == actual.width + delta.width
                    || expected.width == actual.width - delta.width,
            message = "Expected <$expected>, actual <$actual>.${message?.let { " $it" } ?: ""}"
        )
        assertTrue(
            actual = expected.height == actual.height
                    || expected.height == actual.height + delta.height
                    || expected.height == actual.height - delta.height,
            message = "Expected <$expected>, actual <$actual>.${message?.let { " $it" } ?: ""}"
        )
    } else {
        assertEquals(expected, actual, message)
    }
}