package com.github.panpf.zoomimage.core.sketch3.test.internal

import android.graphics.Bitmap
import com.github.panpf.zoomimage.sketch.internal.toHexString
import com.github.panpf.zoomimage.sketch.internal.toLogString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class SketchOtherUtilsTest {

    @Test
    fun testToHexString() {
        val any1 = Any()
        val any2 = Any()
        assertEquals(
            expected = any1.hashCode().toString(16),
            actual = any1.toHexString()
        )
        assertEquals(
            expected = any2.hashCode().toString(16),
            actual = any2.toHexString()
        )
        assertNotEquals(
            illegal = any1.toHexString(),
            actual = any2.toHexString()
        )
    }

    @Test
    fun testToLogString() {
        Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(110x210,ARGB_8888)",
                this.toLogString()
            )
        }

        Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(210x110,RGB_565)",
                this.toLogString()
            )
        }
    }
}