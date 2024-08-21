package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadModeTest {

    @Test
    fun test() {
        ReadMode().apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL, sizeType)
            assertEquals(ReadMode.Decider.Default, decider)
        }

        ReadMode.Default.apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL, sizeType)
            assertEquals(ReadMode.Decider.Default, decider)
        }
    }

    @Test
    fun testAccept() {
        ReadMode().apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL, sizeType)

            // HORIZONTAL
            assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )

            // VERTICAL
            assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )
        }

        ReadMode(sizeType = ReadMode.SIZE_TYPE_HORIZONTAL).apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL, sizeType)

            // HORIZONTAL
            assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )

            // VERTICAL
            assertFalse(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )
        }

        ReadMode(sizeType = ReadMode.SIZE_TYPE_VERTICAL).apply {
            assertEquals(ReadMode.SIZE_TYPE_VERTICAL, sizeType)

            // HORIZONTAL
            assertFalse(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )

            // VERTICAL
            assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )
        }
    }

    @Test
    fun testDeciderDefault() {
        assertEquals(
            expected = ReadMode.LongImageDecider(),
            actual = ReadMode.Decider.Default
        )
    }
}