package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.util.format
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreUtilsTest {

    @Test
    fun testFormat() {
        assertEquals(1.2f, 1.234f.format(1), 0f)
        assertEquals(1.23f, 1.234f.format(2), 0f)
        assertEquals(1.24f, 1.235f.format(2), 0f)
    }

    @Test
    fun testToHexString() {
        // TODO test
    }

    @Test
    fun testQuietClose() {
        // TODO test
    }
}