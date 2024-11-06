package com.github.panpf.zoomimage.compose.sketch4.core.test.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.zoomimage.compose.sketch.internal.EngineSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.sketch.platformContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineSketchComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = platformContext
        val sketch = Sketch.Builder(context).build()
        val generator = EngineSketchComposeSubsamplingImageGenerator()

        val result1 = ImageResult.Success(
            image = ColorPainter(Color.Cyan).asImage(),
            request = ImageRequest(context, "http://sample.com/sample.jpeg"),
            cacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg"),
            dataFrom = DataFrom.NETWORK,
            transformeds = null,
            extras = null,
            resize = Resize(100, 200)
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = SketchImageSource.Factory(sketch, result1.request.uri.toString()),
                    imageInfo = com.github.panpf.zoomimage.subsampling.ImageInfo(
                        width = result1.imageInfo.width,
                        height = result1.imageInfo.height,
                        mimeType = result1.imageInfo.mimeType
                    )
                )
            ),
            actual = generator.generateImage(
                sketch = sketch,
                result = result1,
                painter = ColorPainter(Color.Blue)
            )
        )

        val result2 = ImageResult.Success(
            image = ColorPainter(Color.Cyan).asImage(),
            request = ImageRequest(context, "fakehttp://sample.com/sample.jpeg"),
            cacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg"),
            dataFrom = DataFrom.NETWORK,
            transformeds = null,
            extras = null,
            resize = Resize(100, 200)
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = SketchImageSource.Factory(sketch, result2.request.uri.toString()),
                    imageInfo = com.github.panpf.zoomimage.subsampling.ImageInfo(
                        width = result2.imageInfo.width,
                        height = result2.imageInfo.height,
                        mimeType = result2.imageInfo.mimeType
                    )
                )
            ),
            actual = generator.generateImage(
                sketch = sketch,
                result = result2,
                painter = ColorPainter(Color.Blue)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineSketchComposeSubsamplingImageGenerator()
        val element11 = EngineSketchComposeSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineSketchComposeSubsamplingImageGenerator",
            actual = EngineSketchComposeSubsamplingImageGenerator().toString()
        )
    }
}