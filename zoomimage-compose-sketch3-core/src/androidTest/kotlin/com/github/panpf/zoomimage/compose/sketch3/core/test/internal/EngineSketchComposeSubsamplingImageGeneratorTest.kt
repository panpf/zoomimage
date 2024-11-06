package com.github.panpf.zoomimage.compose.sketch3.core.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.datasource.DataFrom
import com.github.panpf.sketch.decode.ImageInfo
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.github.panpf.zoomimage.compose.sketch.internal.EngineSketchComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.sketch.SketchImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineSketchComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sketch = Sketch.Builder(context).build()
        val generator = EngineSketchComposeSubsamplingImageGenerator()

        val result1 = DisplayResult.Success(
            drawable = ColorDrawable(Color.CYAN),
            request = DisplayRequest(context, "http://sample.com/sample.jpeg"),
            requestKey = "",
            requestCacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg", 0),
            dataFrom = DataFrom.NETWORK,
            transformedList = null,
            extras = null,
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = SketchImageSource.Factory(sketch, result1.request.uriString),
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
                painter = ColorPainter(androidx.compose.ui.graphics.Color.Blue)
            )
        )

        val result2 = DisplayResult.Success(
            drawable = ColorDrawable(Color.CYAN),
            request = DisplayRequest(context, "fakehttp://sample.com/sample.jpeg"),
            requestKey = "",
            requestCacheKey = "",
            imageInfo = ImageInfo(100, 200, "image/jpeg", 0),
            dataFrom = DataFrom.NETWORK,
            transformedList = null,
            extras = null,
        )
        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = SketchImageSource.Factory(sketch, result2.request.uriString),
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
                painter = ColorPainter(androidx.compose.ui.graphics.Color.Blue)
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