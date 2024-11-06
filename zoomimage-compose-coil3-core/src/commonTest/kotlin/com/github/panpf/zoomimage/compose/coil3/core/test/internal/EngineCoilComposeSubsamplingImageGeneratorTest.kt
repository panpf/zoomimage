package com.github.panpf.zoomimage.compose.coil3.core.test.internal

import androidx.compose.ui.graphics.painter.ColorPainter
import coil3.Canvas
import coil3.Image
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.compose.coil.internal.EngineCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.test.coil.platformContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineCoilComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = platformContext
        val imageLoader = ImageLoader(context)
        val generator = EngineCoilComposeSubsamplingImageGenerator()

        assertEquals(
            expected = SubsamplingImageGenerateResult.Success(
                SubsamplingImage(
                    imageSource = CoilHttpImageSource.Factory(
                        context,
                        imageLoader,
                        "http://sample.com/sample.jpeg"
                    ),
                    imageInfo = null
                )
            ),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = SuccessResult(
                    image = TestImage(100, 200),
                    request = ImageRequest.Builder(context).data("http://sample.com/sample.jpeg")
                        .build(),
                    dataSource = DataSource.DISK,
                ),
                painter = ColorPainter(androidx.compose.ui.graphics.Color.Blue)
            )
        )

        assertEquals(
            expected = SubsamplingImageGenerateResult.Error("Unsupported data"),
            actual = generator.generateImage(
                context = context,
                imageLoader = imageLoader,
                result = SuccessResult(
                    image = TestImage(100, 200),
                    request = ImageRequest.Builder(context)
                        .data("fakehttp://sample.com/sample.jpeg").build(),
                    dataSource = DataSource.DISK,
                ),
                painter = ColorPainter(androidx.compose.ui.graphics.Color.Blue)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineCoilComposeSubsamplingImageGenerator()
        val element11 = EngineCoilComposeSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineCoilComposeSubsamplingImageGenerator",
            actual = EngineCoilComposeSubsamplingImageGenerator().toString()
        )
    }

    class TestImage(
        override val width: Int,
        override val height: Int,
        override val shareable: Boolean = true
    ) : Image {

        override val size: Long = width * height * 4L

        override fun draw(canvas: Canvas) {
        }
    }
}