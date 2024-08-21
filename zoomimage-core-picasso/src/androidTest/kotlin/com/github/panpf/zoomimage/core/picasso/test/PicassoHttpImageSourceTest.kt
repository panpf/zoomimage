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

        val imageSource1 = PicassoHttpImageSource(picasso, imageUri1)
        val imageSource12 = PicassoHttpImageSource(picasso, imageUri1)
        val imageSource2 = PicassoHttpImageSource(picasso, imageUri2)
        val imageSource22 = PicassoHttpImageSource(picasso, imageUri2)

        assertEquals(expected = imageSource1, actual = imageSource1)
        assertEquals(expected = imageSource1, actual = imageSource12)
        assertEquals(expected = imageSource2, actual = imageSource22)
        assertNotEquals(illegal = imageSource1, actual = null as Any?)
        assertNotEquals(illegal = imageSource1, actual = Any())
        assertNotEquals(illegal = imageSource1, actual = imageSource2)
        assertNotEquals(illegal = imageSource12, actual = imageSource22)

        assertEquals(
            expected = imageSource1.hashCode(),
            actual = imageSource12.hashCode()
        )
        assertEquals(
            expected = imageSource2.hashCode(),
            actual = imageSource22.hashCode()
        )
        assertNotEquals(
            illegal = imageSource1.hashCode(),
            actual = imageSource2.hashCode()
        )
        assertNotEquals(
            illegal = imageSource12.hashCode(),
            actual = imageSource22.hashCode()
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