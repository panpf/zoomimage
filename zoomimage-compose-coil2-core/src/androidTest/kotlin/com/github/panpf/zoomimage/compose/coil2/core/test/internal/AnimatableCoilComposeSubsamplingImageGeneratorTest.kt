package com.github.panpf.zoomimage.compose.coil2.core.test.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.drawable.CrossfadeDrawable
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.panpf.zoomimage.compose.coil.internal.AnimatableCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.google.accompanist.drawablepainter.DrawablePainter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableCoilComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader(context)
        val result = SuccessResult(
            drawable = ColorDrawable(Color.CYAN),
            request = ImageRequest.Builder(context).build(),
            dataSource = DataSource.DISK,
        )
        val generator = AnimatableCoilComposeSubsamplingImageGenerator()

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = ColorPainter(androidx.compose.ui.graphics.Color.Blue)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = DrawablePainter(TestAnimatableDrawable())
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = DrawablePainter(CrossfadeDrawable(TestAnimatableDrawable(), null))
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = DrawablePainter(CrossfadeDrawable(null, TestAnimatableDrawable()))
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = DrawablePainter(
                    TransitionDrawable(
                        arrayOf(
                            TestAnimatableDrawable(),
                            ColorDrawable(Color.CYAN)
                        )
                    )
                )
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = result,
                painter = DrawablePainter(
                    TransitionDrawable(
                        arrayOf(
                            ColorDrawable(Color.CYAN),
                            TestAnimatableDrawable()
                        )
                    )
                )
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = AnimatableCoilComposeSubsamplingImageGenerator()
        val element11 = AnimatableCoilComposeSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "AnimatableCoilComposeSubsamplingImageGenerator",
            actual = AnimatableCoilComposeSubsamplingImageGenerator().toString()
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