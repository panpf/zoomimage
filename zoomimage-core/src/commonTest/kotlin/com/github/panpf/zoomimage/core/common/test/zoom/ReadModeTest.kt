package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ReadMode.Companion.SIZE_TYPE_VERTICAL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadModeTest {

    @Test
    fun test() {
        ReadMode().apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)
            assertEquals(ReadMode.Decider.Default, decider)
        }

        ReadMode.Default.apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)
            assertEquals(ReadMode.Decider.Default, decider)
        }
    }

    @Test
    fun testAccept() {
        ReadMode().apply {
            assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)

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
    fun testDecider() {
        ReadMode.LongImageDecider().apply {
            assertEquals(2.5f, sameDirectionMultiple)
            assertEquals(5.0f, notSameDirectionMultiple)
        }
        ReadMode.Decider.Default.apply {
            assertEquals(2.5f, sameDirectionMultiple)
            assertEquals(5.0f, notSameDirectionMultiple)
        }

        ReadMode.LongImageDecider().apply {
            assertEquals("LongImageDecider(2.5:5.0)", toString())
        }
        ReadMode.LongImageDecider(5.0f, 10.0f).apply {
            assertEquals("LongImageDecider(5.0:10.0)", toString())
        }

        ReadMode.LongImageDecider().apply {
            // same direction
            assertTrue(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )
            assertFalse(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 970),
                )
            )

            // not same direction
            assertTrue(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )
            assertFalse(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(490, 200),
                )
            )
        }
    }
}