package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.util.closeQuietly
import com.github.panpf.zoomimage.util.compareVersions
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.isThumbnailWithSize
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.toHexString
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.util.toSize
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CoreUtilsTest {

    @Test
    fun testFormat() {
        assertEquals(Float.NaN, Float.NaN.format(1), 0f)
        assertEquals(1.2f, 1.234f.format(1), 0f)
        assertEquals(1.23f, 1.234f.format(2), 0f)
        assertEquals(1.24f, 1.235f.format(2), 0f)
    }

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
    fun testCloseQuietly() {
        if (Platform.current == Platform.iOS) {
            // TODO test: Will get stuck forever in iOS test environment.
            //  There are other places where this problem also occurs, search for it
            return
        }

        val myCloseable = MyCloseable()

        assertFailsWith(IOException::class) {
            myCloseable.close()
        }

        myCloseable.closeQuietly()

        val myAutoCloseable = MyAutoCloseable()

        assertFailsWith(IOException::class) {
            myAutoCloseable.close()
        }

        myAutoCloseable.closeQuietly()
    }

    @Test
    fun testCompareVersions() {
        assertEquals(-1, compareVersions("0.8", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8"))
        assertEquals(-1, compareVersions("0.8.10", "0.8.10.1"))
        assertEquals(1, compareVersions("0.8.10.1", "0.8.10"))
        assertEquals(-1, compareVersions("0.8.15", "0.8.16"))
        assertEquals(1, compareVersions("0.8.16", "0.8.15"))
        assertEquals(-1, compareVersions("0.7.99", "0.8.0"))
        assertEquals(1, compareVersions("0.8.0", "0.7.99"))
        assertEquals(-1, compareVersions("0.6.99", "0.7.99"))
        assertEquals(1, compareVersions("0.7.99", "0.6.99"))

        assertEquals(0, compareVersions("1.0.0", "1.0.0"))
        assertEquals(0, compareVersions("0.8.1", "0.8.1"))

        assertEquals(-1, compareVersions("0.8.0", "0.8.1-SNAPSHOT01"))
        assertEquals(1, compareVersions("0.8.1-SNAPSHOT01", "0.8.0"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT01", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8.1-SNAPSHOT01"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT01", "0.8.2"))
        assertEquals(1, compareVersions("0.8.2", "0.8.1-SNAPSHOT01"))
        assertEquals(0, compareVersions("0.8.1-SNAPSHOT01", "0.8.1-SNAPSHOT1"))
        assertEquals(0, compareVersions("0.8.1-SNAPSHOT09", "0.8.1-SNAPSHOT9"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT1", "0.8.1-SNAPSHOT2"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT01", "0.8.1-SNAPSHOT2"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT01", "0.8.1-SNAPSHOT02"))
        assertEquals(1, compareVersions("0.8.1-SNAPSHOT2", "0.8.1-SNAPSHOT1"))
        assertEquals(1, compareVersions("0.8.1-SNAPSHOT2", "0.8.1-SNAPSHOT01"))
        assertEquals(1, compareVersions("0.8.1-SNAPSHOT02", "0.8.1-SNAPSHOT01"))

        assertEquals(-1, compareVersions("0.8.0", "0.8.1-alpha01"))
        assertEquals(1, compareVersions("0.8.1-alpha01", "0.8.0"))
        assertEquals(-1, compareVersions("0.8.1-alpha01", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8.1-alpha01"))
        assertEquals(-1, compareVersions("0.8.1-alpha01", "0.8.2"))
        assertEquals(1, compareVersions("0.8.2", "0.8.1-alpha01"))
        assertEquals(0, compareVersions("0.8.1-alpha01", "0.8.1-alpha1"))
        assertEquals(0, compareVersions("0.8.1-alpha09", "0.8.1-alpha9"))
        assertEquals(-1, compareVersions("0.8.1-alpha1", "0.8.1-alpha2"))
        assertEquals(-1, compareVersions("0.8.1-alpha01", "0.8.1-alpha2"))
        assertEquals(-1, compareVersions("0.8.1-alpha01", "0.8.1-alpha02"))
        assertEquals(1, compareVersions("0.8.1-alpha2", "0.8.1-alpha1"))
        assertEquals(1, compareVersions("0.8.1-alpha2", "0.8.1-alpha01"))
        assertEquals(1, compareVersions("0.8.1-alpha02", "0.8.1-alpha01"))

        assertEquals(-1, compareVersions("0.8.0", "0.8.1-beta01"))
        assertEquals(1, compareVersions("0.8.1-beta01", "0.8.0"))
        assertEquals(-1, compareVersions("0.8.1-beta01", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8.1-beta01"))
        assertEquals(-1, compareVersions("0.8.1-beta01", "0.8.2"))
        assertEquals(1, compareVersions("0.8.2", "0.8.1-beta01"))
        assertEquals(0, compareVersions("0.8.1-beta01", "0.8.1-beta1"))
        assertEquals(0, compareVersions("0.8.1-beta09", "0.8.1-beta9"))
        assertEquals(-1, compareVersions("0.8.1-beta1", "0.8.1-beta2"))
        assertEquals(-1, compareVersions("0.8.1-beta01", "0.8.1-beta2"))
        assertEquals(-1, compareVersions("0.8.1-beta01", "0.8.1-beta02"))
        assertEquals(1, compareVersions("0.8.1-beta2", "0.8.1-beta1"))
        assertEquals(1, compareVersions("0.8.1-beta2", "0.8.1-beta01"))
        assertEquals(1, compareVersions("0.8.1-beta02", "0.8.1-beta01"))

        assertEquals(-1, compareVersions("0.8.0", "0.8.1-rc01"))
        assertEquals(1, compareVersions("0.8.1-rc01", "0.8.0"))
        assertEquals(-1, compareVersions("0.8.1-rc01", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8.1-rc01"))
        assertEquals(-1, compareVersions("0.8.1-rc01", "0.8.2"))
        assertEquals(1, compareVersions("0.8.2", "0.8.1-rc01"))
        assertEquals(0, compareVersions("0.8.1-rc01", "0.8.1-rc1"))
        assertEquals(0, compareVersions("0.8.1-rc09", "0.8.1-rc9"))
        assertEquals(-1, compareVersions("0.8.1-rc1", "0.8.1-rc2"))
        assertEquals(-1, compareVersions("0.8.1-rc01", "0.8.1-rc2"))
        assertEquals(-1, compareVersions("0.8.1-rc01", "0.8.1-rc02"))
        assertEquals(1, compareVersions("0.8.1-rc2", "0.8.1-rc1"))
        assertEquals(1, compareVersions("0.8.1-rc2", "0.8.1-rc01"))
        assertEquals(1, compareVersions("0.8.1-rc02", "0.8.1-rc01"))

        assertEquals(-1, compareVersions("0.8.0", "0.8.1-SNAPSHOT1"))
        assertEquals(-1, compareVersions("0.8.1-SNAPSHOT1", "0.8.1-alpha01"))
        assertEquals(-1, compareVersions("0.8.1-alpha01", "0.8.1-beta1"))
        assertEquals(-1, compareVersions("0.8.1-beta1", "0.8.1-rc02"))
        assertEquals(-1, compareVersions("0.8.1-rc02", "0.8.1"))
        assertEquals(-1, compareVersions("0.8.1", "0.8.2"))

        assertEquals(1, compareVersions("0.8.2", "0.8.1"))
        assertEquals(1, compareVersions("0.8.1", "0.8.1-rc.02"))
        assertEquals(1, compareVersions("0.8.1-rc.02", "0.8.1-beta.1"))
        assertEquals(1, compareVersions("0.8.1-beta.1", "0.8.1-alpha.01"))
        assertEquals(1, compareVersions("0.8.1-alpha.01", "0.8.1-SNAPSHOT.1"))
        assertEquals(1, compareVersions("0.8.1-SNAPSHOT.1", "0.8.0"))
    }

    @Test
    fun testIsThumbnailWithSize() {
        assertFalse(isThumbnailWithSize(IntSizeCompat(0, 2000), IntSizeCompat(500, 1000)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(1000, 0), IntSizeCompat(500, 1000)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(1000, 2000), IntSizeCompat(0, 1000)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(1000, 2000), IntSizeCompat(500, 0)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(1000, 2000), IntSizeCompat(1001, 200)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(1000, 2000), IntSizeCompat(100, 2001)))
        assertFalse(isThumbnailWithSize(IntSizeCompat(100, 200), IntSizeCompat(1000, 100)))

        assertFalse(
            isThumbnailWithSize(
                size = IntSizeCompat(6799, 4882),
                otherSize = IntSizeCompat(696, 501),
                epsilonPixels = 1f
            )
        )
        assertTrue(
            isThumbnailWithSize(
                size = IntSizeCompat(6799, 4882),
                otherSize = IntSizeCompat(696, 501),
                epsilonPixels = 2f
            )
        )

        var imageSize = IntSizeCompat(29999, 325)
        val maxMultiple = 257

        val nextFunction: (Float) -> Float = { it + 0.1f }
        val calculateThumbnailSize: (IntSizeCompat, Float) -> SizeCompat =
            { size, multiple -> size.toSize() / multiple }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.CEIL)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 2)
            assertFalse(
                actual = isThumbnailWithSize(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.FLOOR)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 2)
            assertFalse(
                actual = isThumbnailWithSize(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.ROUND)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(imageSize, thumbnailSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(0, 2)
            assertFalse(
                actual = isThumbnailWithSize(imageSize, thumbnailSize2),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        imageSize = IntSizeCompat(325, 29999)

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.CEIL)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(thumbnailSize, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(2, 0)
            assertFalse(
                actual = isThumbnailWithSize(thumbnailSize2, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.FLOOR)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(thumbnailSize, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(2, 0)
            assertFalse(
                actual = isThumbnailWithSize(thumbnailSize2, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }

        generateSequence(1f, nextFunction).takeWhile { it <= maxMultiple }.forEach { multiple ->
            val thumbnailSize =
                calculateThumbnailSize(imageSize, multiple).roundToIntWithMode(RoundMode.ROUND)
            assertEquals(
                expected = imageSize != thumbnailSize,
                actual = isThumbnailWithSize(thumbnailSize, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )

            val thumbnailSize2 = thumbnailSize + IntSizeCompat(2, 0)
            assertFalse(
                actual = isThumbnailWithSize(thumbnailSize2, imageSize),
                message = "imageSize=${imageSize.toShortString()}, " +
                        "thumbnailSize=${thumbnailSize2.toShortString()}, " +
                        "multiple=${multiple.format(2)}"
            )
        }
    }

    // TODO test isInRange

    private class MyCloseable : okio.Closeable {

        override fun close() {
            throw IOException("Closed")
        }
    }

    private class MyAutoCloseable : AutoCloseable {

        override fun close() {
            throw IOException("Closed")
        }
    }
}