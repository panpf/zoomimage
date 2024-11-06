package com.github.panpf.zoomimage.core.glide.test.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.github.panpf.zoomimage.glide.internal.AnimatableGlideSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableGlideSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Glide.get(context)
        val generator = AnimatableGlideSubsamplingImageGenerator()

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = Any(),
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = Any(),
                drawable = TestAnimatableDrawable()
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = Any(),
                drawable = TransitionDrawable(
                    arrayOf(TestAnimatableDrawable(), ColorDrawable(Color.CYAN))
                )
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                glide = glide,
                model = Any(),
                drawable = TransitionDrawable(
                    arrayOf(ColorDrawable(Color.CYAN), TestAnimatableDrawable())
                )
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = AnimatableGlideSubsamplingImageGenerator()
        val element11 = AnimatableGlideSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "AnimatableGlideSubsamplingImageGenerator",
            actual = AnimatableGlideSubsamplingImageGenerator().toString()
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