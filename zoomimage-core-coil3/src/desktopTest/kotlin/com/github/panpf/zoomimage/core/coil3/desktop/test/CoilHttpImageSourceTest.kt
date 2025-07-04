package com.github.panpf.zoomimage.core.coil3.desktop.test

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.request.ImageRequest
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.runBlocking
import okio.buffer
import org.jetbrains.skia.Image
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CoilHttpImageSourceTest {

    @Test
    fun testConstructor() {
        val imageUri = "https://www.example.com/image.jpg"
        CoilHttpImageSource(imageUri) {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun testKey() {
        val imageUri = "https://www.example.com/image.jpg"
        CoilHttpImageSource(imageUri) {
            throw UnsupportedOperationException()
        }.apply {
            assertEquals(imageUri, key)
        }
    }

    @Test
    fun testOpenSource() {
        val context = PlatformContext.INSTANCE
        val imageLoader = ImageLoader.Builder(context).build()
        val imageUri =
            "https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=640&auto=jpeg&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
        val request = ImageRequest.Builder(context).data(imageUri).build()
        val diskCache = imageLoader.diskCache!!

        diskCache.clear()
        assertEquals(null, diskCache.openSnapshot(imageUri))

        CoilHttpImageSource.Factory(context, imageLoader, request)
            .let { runBlocking { it.create() } }.apply {
                val imageSize = this.openSource()
                    .buffer().use { it.readByteArray() }
                    .let { Image.makeFromEncoded(it) }
                    .let { IntSizeCompat(it.width, it.height) }
                assertEquals(expected = IntSizeCompat(640, 958), actual = imageSize)
            }
        assertNotEquals(
            illegal = null,
            actual = diskCache.openSnapshot(imageUri)?.apply { close() }
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val imageUri1 = "https://www.example.com/image1.jpg"
        val imageUri2 = "https://www.example.com/image2.jpg"

        val element1 = CoilHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val element12 = CoilHttpImageSource(imageUri1) {
            throw UnsupportedOperationException()
        }
        val element2 = CoilHttpImageSource(imageUri2) {
            throw UnsupportedOperationException()
        }

        assertEquals(expected = element1, actual = element1)
        assertEquals(expected = element1, actual = element12)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())
        assertNotEquals(illegal = element1, actual = element2)

        assertEquals(expected = element1.hashCode(), actual = element12.hashCode())
        assertNotEquals(illegal = element1.hashCode(), actual = element2.hashCode())
    }

    @Test
    fun testToString() {
        val imageUri = "https://www.example.com/image1.jpg"
        val coilHttpImageSource = CoilHttpImageSource(imageUri) {
            throw UnsupportedOperationException()
        }
        assertEquals(
            expected = "CoilHttpImageSource('$imageUri')",
            actual = coilHttpImageSource.toString()
        )
    }
}