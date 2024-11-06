package com.github.panpf.zoomimage.compose.coil2.core.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.compose.coil.internal.EngineCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineCoilComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
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
                    drawable = ColorDrawable(Color.CYAN),
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
                    drawable = ColorDrawable(Color.CYAN),
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
}