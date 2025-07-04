package com.github.panpf.zoomimage.view.sketch4.core.test.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.drawable.CrossfadeDrawable
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.sketch.internal.AnimatableSketchViewSubsamplingImageGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableSketchViewSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val result = ImageResult.Success(
            image = ColorDrawable(Color.CYAN).asImage(),
            request = ImageRequest(context, "http://sample.com/sample.jpeg"),
            cacheKey = "",
            memoryCacheKey = "",
            resultCacheKey = "",
            downloadCacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg"),
            dataFrom = DataFrom.NETWORK,
            resize = Resize(100, 200),
            transformeds = null,
            extras = null,
        )
        val generator = AnimatableSketchViewSubsamplingImageGenerator()

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = TestAnimatableDrawable()
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = CrossfadeDrawable(TestAnimatableDrawable(), null)
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = CrossfadeDrawable(null, TestAnimatableDrawable())
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = AnimatableSketchViewSubsamplingImageGenerator()
        val element11 = AnimatableSketchViewSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "AnimatableSketchViewSubsamplingImageGenerator",
            actual = AnimatableSketchViewSubsamplingImageGenerator().toString()
        )
    }

    class TestAnimatableDrawable : Drawable(), Animatable {

        override fun start() {
        }

        override fun stop() {
        }

        override fun isRunning(): Boolean {
            return false
        }

        override fun draw(canvas: Canvas) {

        }

        override fun setAlpha(alpha: Int) {

        }

        override fun setColorFilter(colorFilter: ColorFilter?) {

        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity(): Int {
            return 0
        }
    }
}