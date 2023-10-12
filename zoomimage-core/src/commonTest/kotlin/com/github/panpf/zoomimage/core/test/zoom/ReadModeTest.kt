package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ReadMode.Companion.SIZE_TYPE_VERTICAL
import org.junit.Assert
import org.junit.Test

class ReadModeTest {

    @Test
    fun test() {
        ReadMode().apply {
            Assert.assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)
            Assert.assertEquals(ReadMode.Decider.Default, decider)
        }

        ReadMode.Default.apply {
            Assert.assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)
            Assert.assertEquals(ReadMode.Decider.Default, decider)
        }
    }

    @Test
    fun testAccept() {
        ReadMode().apply {
            Assert.assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL or SIZE_TYPE_VERTICAL, sizeType)

            // HORIZONTAL
            Assert.assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )

            // VERTICAL
            Assert.assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )
        }

        ReadMode(sizeType = ReadMode.SIZE_TYPE_HORIZONTAL).apply {
            Assert.assertEquals(ReadMode.SIZE_TYPE_HORIZONTAL, sizeType)

            // HORIZONTAL
            Assert.assertTrue(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )

            // VERTICAL
            Assert.assertFalse(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )
        }

        ReadMode(sizeType = ReadMode.SIZE_TYPE_VERTICAL).apply {
            Assert.assertEquals(ReadMode.SIZE_TYPE_VERTICAL, sizeType)

            // HORIZONTAL
            Assert.assertFalse(
                accept(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )

            // VERTICAL
            Assert.assertTrue(
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
            Assert.assertEquals(2.5f, sameDirectionMultiple)
            Assert.assertEquals(5.0f, notSameDirectionMultiple)
        }
        ReadMode.Decider.Default.apply {
            Assert.assertEquals(2.5f, sameDirectionMultiple)
            Assert.assertEquals(5.0f, notSameDirectionMultiple)
        }

        ReadMode.LongImageDecider().apply {
            Assert.assertEquals("LongImageDecider(2.5:5.0)", toString())
        }
        ReadMode.LongImageDecider(5.0f, 10.0f).apply {
            Assert.assertEquals("LongImageDecider(5.0:10.0)", toString())
        }

        ReadMode.LongImageDecider().apply {
            // same direction
            Assert.assertTrue(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 980),
                )
            )
            Assert.assertFalse(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(200, 970),
                )
            )

            // not same direction
            Assert.assertTrue(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(500, 200),
                )
            )
            Assert.assertFalse(
                should(
                    containerSize = IntSizeCompat(1000, 2000),
                    contentSize = IntSizeCompat(490, 200),
                )
            )
        }
    }
}