package com.github.panpf.zoomimage.core.nonandroid.test.util

import com.github.panpf.zoomimage.subsampling.toLogString
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toSkiaRect
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoreUtilsNonAndroidTest {

    @Test
    fun testToSkiaRect() {
        val rect1 = IntRectCompat(111, 223, 467, 953)
        val rect2 = IntRectCompat(213, 125, 763, 555)

        val skiRect1 = rect1.toSkiaRect().apply {
            assertEquals(rect1.left.toFloat(), left)
            assertEquals(rect1.top.toFloat(), top)
            assertEquals(rect1.right.toFloat(), right)
            assertEquals(rect1.bottom.toFloat(), bottom)
        }
        val skiRect2 = rect2.toSkiaRect().apply {
            assertEquals(rect2.left.toFloat(), left)
            assertEquals(rect2.top.toFloat(), top)
            assertEquals(rect2.right.toFloat(), right)
            assertEquals(rect2.bottom.toFloat(), bottom)
        }

        assertNotEquals(illegal = rect1, actual = rect2)
        assertNotEquals(illegal = skiRect1, actual = skiRect2)
    }

    @Test
    fun testToLogString() {
        val bitmap = Bitmap().apply {
            allocN32Pixels(100, 100, opaque = false)
        }
        assertEquals(
            expected = "Bitmap@${
                bitmap.hashCode().toString(16)
            }(${bitmap.width.toFloat()}x${bitmap.height.toFloat()},${bitmap.colorType},null)",
            actual = bitmap.toLogString()
        )
        bitmap.close()
    }
}