package com.github.panpf.zoomimage.view.coil3.core.test.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.test.platform.app.InstrumentationRegistry
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.transition.CrossfadeDrawable
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.coil.internal.AnimatableCoilViewSubsamplingImageGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableCoilViewSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader(context)
        val result = SuccessResult(
            image = ColorDrawable(Color.CYAN).asImage(),
            request = ImageRequest.Builder(context).build(),
            dataSource = DataSource.DISK,
        )
        val generator = AnimatableCoilViewSubsamplingImageGenerator()

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = TestAnimatableDrawable()
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = CrossfadeDrawable(TestAnimatableDrawable(), null)
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = CrossfadeDrawable(null, TestAnimatableDrawable())
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = TransitionDrawable(
                    arrayOf(TestAnimatableDrawable(), ColorDrawable(Color.CYAN))
                )
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                drawable = TransitionDrawable(
                    arrayOf(ColorDrawable(Color.CYAN), TestAnimatableDrawable())
                )
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = AnimatableCoilViewSubsamplingImageGenerator()
        val element11 = AnimatableCoilViewSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "AnimatableCoilViewSubsamplingImageGenerator",
            actual = AnimatableCoilViewSubsamplingImageGenerator().toString()
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