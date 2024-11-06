package com.github.panpf.zoomimage.core.glide.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.glide.GlideHttpImageSource
import com.github.panpf.zoomimage.glide.internal.EngineGlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineGlideSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val generator = EngineGlideSubsamplingImageGenerator()

        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = GlideHttpImageSource.Factory(
                        glide,
                        "http://sample.com/sample.jpeg"
                    ),
                    imageInfo = null
                )
            ),
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = "http://sample.com/sample.jpeg",
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Unsupported model"),
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = "fakehttp://sample.com/sample.jpeg",
                drawable = ColorDrawable(Color.BLUE)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineGlideSubsamplingImageGenerator()
        val element11 = EngineGlideSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineGlideSubsamplingImageGenerator",
            actual = EngineGlideSubsamplingImageGenerator().toString()
        )
    }
}