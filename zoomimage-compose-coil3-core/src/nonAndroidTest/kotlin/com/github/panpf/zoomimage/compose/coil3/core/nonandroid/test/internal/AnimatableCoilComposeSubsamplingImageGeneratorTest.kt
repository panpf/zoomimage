package com.github.panpf.zoomimage.compose.coil3.core.nonandroid.test.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.compose.coil.internal.AnimatableCoilComposeSubsamplingImageGenerator
import com.github.panpf.zoomimage.test.coil.platformContext
import com.github.panpf.zoomimage.test.createBitmap
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnimatableCoilComposeSubsamplingImageGeneratorTest {

    @Test
    fun testGenerateImage() = runTest {
        val context = platformContext
        val imageLoader = ImageLoader(context)
        val result = SuccessResult(
            image = createBitmap(100, 200).asImage(),
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
                painter = ColorPainter(Color.Blue)
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
}