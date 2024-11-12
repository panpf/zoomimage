package com.github.panpf.zoomimage.core.common.test.util

import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.util.compareVersions
import com.github.panpf.zoomimage.util.format
import com.github.panpf.zoomimage.util.quietClose
import com.github.panpf.zoomimage.util.toHexString
import okio.Closeable
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

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
    fun testQuietClose() {
        if (Platform.current == Platform.iOS) {
            // TODO test: Will get stuck forever in iOS test environment.
            //  There are other places where this problem also occurs, search for it
            return
        }

        val myCloseable = MyCloseable()

        assertFailsWith(IOException::class) {
            myCloseable.close()
        }

        myCloseable.quietClose()
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

    private class MyCloseable : Closeable {

        override fun close() {
            throw IOException("Closed")
        }
    }
}