package com.github.panpf.zoomimage.core.nonandroid.test.subsampling

import com.github.panpf.zoomimage.subsampling.SkiaBitmap
import com.github.panpf.zoomimage.subsampling.toLogString
import kotlin.test.Test
import kotlin.test.assertEquals

class SkiasTest {

    @Test
    fun testToLogString() {
        val bitmap = SkiaBitmap().apply {
            allocN32Pixels(100, 100, opaque = false)
        }
        assertEquals(
            expected = "SkiaBitmap@${
                bitmap.hashCode().toString(16)
            }(${bitmap.width.toFloat()}x${bitmap.height.toFloat()},${bitmap.colorType})",
            actual = bitmap.toLogString()
        )
        bitmap.close()
    }
}