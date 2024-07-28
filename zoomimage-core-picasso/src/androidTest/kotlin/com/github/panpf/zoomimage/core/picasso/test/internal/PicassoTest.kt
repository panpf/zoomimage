package com.github.panpf.zoomimage.core.picasso.test.internal

import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.checkMemoryCacheDisabled
import com.squareup.picasso.internalCache
import com.squareup.picasso.internalDownloader
import com.squareup.picasso.internalIndex
import com.squareup.picasso.internalMemoryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals

class PicassoTest {

    @Test
    fun testCheckMemoryCacheDisabled() {
        assertEquals(
            expected = false,
            actual = checkMemoryCacheDisabled(0)
        )
        assertEquals(
            expected = true,
            actual = checkMemoryCacheDisabled(MemoryPolicy.NO_CACHE.internalIndex)
        )
        assertEquals(
            expected = true,
            actual = checkMemoryCacheDisabled(MemoryPolicy.NO_STORE.internalIndex)
        )
        assertEquals(
            expected = true,
            actual = checkMemoryCacheDisabled(MemoryPolicy.NO_STORE.internalIndex or MemoryPolicy.NO_STORE.internalIndex)
        )
    }

    @Test
    fun testInternalMemoryPolicy() {
        val picasso = Picasso.get()
        assertEquals(
            expected = 0,
            actual = picasso.load("https://sample.com/sample.jpeg").internalMemoryPolicy
        )
        assertEquals(
            expected = MemoryPolicy.NO_CACHE.internalIndex,
            actual = picasso.load("https://sample.com/sample.jpeg")
                .memoryPolicy(MemoryPolicy.NO_CACHE).internalMemoryPolicy
        )
        assertEquals(
            expected = MemoryPolicy.NO_STORE.internalIndex,
            actual = picasso.load("https://sample.com/sample.jpeg")
                .memoryPolicy(MemoryPolicy.NO_STORE).internalMemoryPolicy
        )
        assertEquals(
            expected = MemoryPolicy.NO_CACHE.internalIndex or MemoryPolicy.NO_STORE.internalIndex,
            actual = picasso.load("https://sample.com/sample.jpeg")
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).internalMemoryPolicy
        )
    }

    @Test
    fun testInternalDownloader() {
        Picasso.get().internalDownloader
    }

    @Test
    fun testInternalCache() {
        Picasso.get().internalCache
    }

    @Test
    fun testInternalIndex() {
        MemoryPolicy.NO_CACHE.internalIndex
    }
}