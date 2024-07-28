package com.github.panpf.zoomimage.core.picasso.test

import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.github.panpf.zoomimage.picasso.PicassoHttpImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.squareup.picasso.Picasso
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PicassoHttpImageSourceTest {

    @Test
    fun testKey() {
        val picasso = Picasso.get()
        val imageUri = "https://www.example.com/image.jpg".toUri()
        PicassoHttpImageSource(picasso, imageUri).apply {
            assertEquals(imageUri.toString(), key)
        }
    }

    @Test
    fun testEqualsAndHashCode() {
        val picasso = Picasso.get()
        val imageUri1 = "https://www.example.com/image1.jpg".toUri()
        val imageUri2 = "https://www.example.com/image2.jpg".toUri()

        val imageSourceFactory1 = PicassoHttpImageSource(picasso, imageUri1)
        val imageSourceFactory12 = PicassoHttpImageSource(picasso, imageUri1)
        val imageSourceFactory2 = PicassoHttpImageSource(picasso, imageUri2)
        val imageSourceFactory22 = PicassoHttpImageSource(picasso, imageUri2)

        assertEquals(expected = imageSourceFactory1, actual = imageSourceFactory12)
        assertEquals(expected = imageSourceFactory2, actual = imageSourceFactory22)
        assertNotEquals(illegal = imageSourceFactory1, actual = imageSourceFactory2)
        assertNotEquals(illegal = imageSourceFactory12, actual = imageSourceFactory22)

        assertEquals(
            expected = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory12.hashCode()
        )
        assertEquals(
            expected = imageSourceFactory2.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory1.hashCode(),
            actual = imageSourceFactory2.hashCode()
        )
        assertNotEquals(
            illegal = imageSourceFactory12.hashCode(),
            actual = imageSourceFactory22.hashCode()
        )
    }

    @Test
    fun testToString() {
        val picasso = Picasso.get()
        val imageUri1 = "https://www.example.com/image1.jpg".toUri()
        val imageUri2 = "https://www.example.com/image2.jpg".toUri()

        assertEquals(
            "PicassoHttpImageSource('$imageUri1')",
            PicassoHttpImageSource(picasso, imageUri1).toString()
        )
        assertEquals(
            "PicassoHttpImageSource('$imageUri2')",
            PicassoHttpImageSource(picasso, imageUri2).toString()
        )
    }

    @Test
    fun testOpenSource() {
        val picasso = Picasso.get()
        val imageUri =
            "https://images.unsplash.com/photo-1721340143289-94be4f77cda4?q=80&w=2832&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D".toUri()

        val imageSource = PicassoHttpImageSource(picasso, imageUri)
        val bytes = imageSource.openSource().buffer().use { it.readByteArray() }
        val bitmap = BitmapFactory.decodeStream(bytes.inputStream())
        val imageSize = bitmap.let { IntSizeCompat(it.width, it.height) }
        assertEquals(expected = IntSizeCompat(2832, 4240), actual = imageSize)
    }
}