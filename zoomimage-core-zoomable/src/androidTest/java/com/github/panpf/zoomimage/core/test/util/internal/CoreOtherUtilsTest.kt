package com.github.panpf.zoomimage.core.test.util.internal

import com.github.panpf.zoomimage.util.internal.format
import org.junit.Assert
import org.junit.Test

class CoreOtherUtilsTest {

    @Test
    fun testFormat() {
        Assert.assertEquals(1.2f, 1.234f.format(1), 0f)
        Assert.assertEquals(1.23f, 1.234f.format(2), 0f)
        Assert.assertEquals(1.24f, 1.235f.format(2), 0f)
    }
}