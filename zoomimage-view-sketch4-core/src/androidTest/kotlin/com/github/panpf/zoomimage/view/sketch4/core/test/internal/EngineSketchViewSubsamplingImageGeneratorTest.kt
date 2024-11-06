package com.github.panpf.zoomimage.view.sketch4.core.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.asImage
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.Resize
import com.github.panpf.sketch.source.DataFrom
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.sketch.internal.EngineSketchViewSubsamplingImageGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineSketchViewSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val generator = EngineSketchViewSubsamplingImageGenerator()

        val result1 = ImageResult.Success(
            image = ColorDrawable(Color.CYAN).asImage(),
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
                drawable = ColorDrawable(Color.BLUE)
            )
        )

        val result2 = ImageResult.Success(
            image = ColorDrawable(Color.CYAN).asImage(),
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
                drawable = ColorDrawable(Color.BLUE)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineSketchViewSubsamplingImageGenerator()
        val element11 = EngineSketchViewSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineSketchViewSubsamplingImageGenerator",
            actual = EngineSketchViewSubsamplingImageGenerator().toString()
        )
    }
}