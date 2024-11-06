package com.github.panpf.zoomimage.core.picasso.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.picasso.PicassoHttpImageSource
import com.github.panpf.zoomimage.picasso.internal.EnginePicassoSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.subsampling.toFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EnginePicassoSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val picasso = Picasso.Builder(context).build()
        val generator = EnginePicassoSubsamplingImageGenerator()

        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = PicassoHttpImageSource(
                        picasso,
                        "http://sample.com/sample.jpeg".toUri()
                    ).toFactory(),
                    imageInfo = null
                )
            ),
            actual = generator.generateImage(
                context = context,
                picasso = picasso,
                data = "http://sample.com/sample.jpeg",
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Unsupported data"),
            actual = generator.generateImage(
                context = context,
                picasso = picasso,
                data = "fakehttp://sample.com/sample.jpeg",
                drawable = ColorDrawable(Color.BLUE)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EnginePicassoSubsamplingImageGenerator()
        val element11 = EnginePicassoSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EnginePicassoSubsamplingImageGenerator",
            actual = EnginePicassoSubsamplingImageGenerator().toString()
        )
    }
}