package com.github.panpf.zoomimage.view.coil2.core.test.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import com.github.panpf.zoomimage.view.coil.internal.EngineCoilViewSubsamplingImageGenerator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EngineCoilViewSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader(context)
        val generator = EngineCoilViewSubsamplingImageGenerator()

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
                drawable = ColorDrawable(Color.BLUE)
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
                drawable = ColorDrawable(Color.BLUE)
            )
        )
    }

    @Test
    fun testEqualsAndHashCode() {
        val element1 = EngineCoilViewSubsamplingImageGenerator()
        val element11 = EngineCoilViewSubsamplingImageGenerator()

        assertEquals(expected = element1, actual = element11)
        assertNotEquals(illegal = element1, actual = null as Any?)
        assertNotEquals(illegal = element1, actual = Any())

        assertEquals(expected = element1.hashCode(), actual = element11.hashCode())
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "EngineCoilViewSubsamplingImageGenerator",
            actual = EngineCoilViewSubsamplingImageGenerator().toString()
        )
    }
}