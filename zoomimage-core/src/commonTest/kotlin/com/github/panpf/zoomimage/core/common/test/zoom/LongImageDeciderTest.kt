package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.zoom.ReadMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LongImageDeciderTest {

    @Test
    fun testConstructor() {
        ReadMode.LongImageDecider().apply {
            assertEquals(expected = "2.5", actual = sameDirectionMultiple.format(2).toString())
            assertEquals(expected = "5.0", actual = notSameDirectionMultiple.format(2).toString())
        }
        ReadMode.LongImageDecider(5.0f, 10.0f).apply {
            assertEquals(expected = "5.0", actual = sameDirectionMultiple.format(2).toString())
            assertEquals(expected = "10.0", actual = notSameDirectionMultiple.format(2).toString())
        }
    }

    @Test
    fun testShould() {
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

    @Test
    fun testEqualsAndHashCode() {
        val decider1 = ReadMode.LongImageDecider()
        val decider12 = ReadMode.LongImageDecider()
        val decider2 = ReadMode.LongImageDecider(sameDirectionMultiple = 5.0f)
        val decider3 = ReadMode.LongImageDecider(notSameDirectionMultiple = 10.0f)
        val decider4 =
            ReadMode.LongImageDecider(sameDirectionMultiple = 20f, notSameDirectionMultiple = 50.0f)
        assertEquals(expected = decider1, actual = decider1)
        assertEquals(expected = decider1, actual = decider12)
        assertNotEquals(illegal = decider1, actual = null as Any?)
        assertNotEquals(illegal = decider1, actual = Any())
        assertNotEquals(illegal = decider1, actual = decider2)
        assertNotEquals(illegal = decider1, actual = decider3)
        assertNotEquals(illegal = decider1, actual = decider4)
        assertNotEquals(illegal = decider2, actual = decider3)
        assertNotEquals(illegal = decider2, actual = decider4)
        assertNotEquals(illegal = decider3, actual = decider4)

        assertEquals(expected = decider1.hashCode(), actual = decider1.hashCode())
        assertEquals(expected = decider1.hashCode(), actual = decider12.hashCode())
        assertNotEquals(illegal = decider1.hashCode(), actual = decider2.hashCode())
        assertNotEquals(illegal = decider1.hashCode(), actual = decider3.hashCode())
        assertNotEquals(illegal = decider1.hashCode(), actual = decider4.hashCode())
        assertNotEquals(illegal = decider2.hashCode(), actual = decider3.hashCode())
        assertNotEquals(illegal = decider2.hashCode(), actual = decider4.hashCode())
        assertNotEquals(illegal = decider3.hashCode(), actual = decider4.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "LongImageDecider(2.5:5.0)",
            actual = ReadMode.LongImageDecider().toString()
        )
        assertEquals(
            expected = "LongImageDecider(5.0:10.0)",
            actual = ReadMode.LongImageDecider(5.0f, 10.0f).toString()
        )
    }
}