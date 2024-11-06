package com.github.panpf.zoomimage.compose.sketch4.core.test.internal

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.painter.AnimatablePainter
import com.github.panpf.sketch.painter.CrossfadePainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.zoomimage.compose.sketch.internal.AnimatableSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.sketch.platformContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableSketchComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = platformContext
        val sketch = Sketch.Builder(context).build()
        val result = ImageResult.Success(
            image = ColorPainter(Color.Cyan).asImage(),
            request = ImageRequest(context, "http://sample.com/sample.jpeg"),
            cacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg"),
            dataFrom = DataFrom.NETWORK,
            resize = Resize(100, 200),
            transformeds = null,
            extras = null,
        )
        val generator = AnimatableSketchComposeSubsamplingImageGenerator()

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                painter = ColorPainter(Color.Blue)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                painter = TestAnimatablePainter()
            )
        )

        assertEquals(
            expected = null,
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                painter = CrossfadePainter(TestAnimatablePainter(), null)
            )
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Animated images do not support subsampling"),
            actual = generator.generateImage(
                sketch = sketch,
                result = result,
                painter = CrossfadePainter(null, TestAnimatablePainter())
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = AnimatableSketchComposeSubsamplingImageGenerator()
        val element11 = AnimatableSketchComposeSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "AnimatableSketchComposeSubsamplingImageGenerator",
            actual = AnimatableSketchComposeSubsamplingImageGenerator().toString()
        )
    }

    class TestAnimatablePainter : Painter(), AnimatablePainter {

        override fun start() {
        }

        override fun stop() {
        }

        override fun isRunning(): Boolean {
            return false
        }

        override val intrinsicSize: Size = Size(100f, 200f)

        override fun DrawScope.onDraw() {

        }
    }
}