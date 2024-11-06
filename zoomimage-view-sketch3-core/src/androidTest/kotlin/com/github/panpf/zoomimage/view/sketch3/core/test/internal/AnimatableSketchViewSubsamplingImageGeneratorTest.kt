package com.github.panpf.zoomimage.view.sketch3.core.test.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.drawable.internal.CrossfadeDrawable
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
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
        val result = DisplayResult.Success(
            drawable = ColorDrawable(Color.CYAN),
            request = DisplayRequest(context, "http://sample.com/sample.jpeg"),
            requestKey = "",
            requestCacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg", 0),
            dataFrom = DataFrom.NETWORK,
            transformedList = null,
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

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = TransitionDrawable(
                    arrayOf(
                        TestAnimatableDrawable(),
                        ColorDrawable(Color.CYAN)
                    )
                )
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                drawable = TransitionDrawable(
                    arrayOf(
                        ColorDrawable(Color.CYAN),
                        TestAnimatableDrawable()
                    )
                )
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