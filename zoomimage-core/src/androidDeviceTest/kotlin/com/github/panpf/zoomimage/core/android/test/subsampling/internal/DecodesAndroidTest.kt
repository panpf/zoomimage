package com.github.panpf.zoomimage.core.android.test.subsampling.internal

import com.github.panpf.zoomimage.images.ComposeResImageFiles
import com.github.panpf.zoomimage.subsampling.internal.AndroidRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.ExifOrientationHelper
import com.github.panpf.zoomimage.subsampling.internal.applyToImageInfo
import com.github.panpf.zoomimage.subsampling.internal.decodeExifOrientation
import com.github.panpf.zoomimage.subsampling.internal.decodeImageInfo
import com.github.panpf.zoomimage.subsampling.internal.defaultRegionDecoder
import com.github.panpf.zoomimage.subsampling.internal.defaultRegionDecoders
import com.github.panpf.zoomimage.util.IntSizeCompat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecodesAndroidTest {

    @Test
    fun testDefaultRegionDecoder() {
        val factory = defaultRegionDecoder()
        assertTrue(factory is AndroidRegionDecoder.Factory)
    }

    @Test
    fun testDefaultRegionDecoders() {
        assertEquals(
            expected = listOf(AndroidRegionDecoder.Factory()),
            actual = defaultRegionDecoders()
        )
    }

    @Test
    fun testDecodeExifOrientation() = runTest {
        ComposeResImageFiles.exifs.forEach { imageFile ->
            assertExifOrientationEquals(
                expected = imageFile.exifOrientation,
                actual = imageFile.toImageSource().decodeExifOrientation(),
                message = imageFile.toString(),
            )
        }
    }

    private fun assertExifOrientationEquals(expected: Int, actual: Int, message: String? = null) {
        val correctedExpected = if (expected == 1) 0 else expected
        val correctedActual = if (actual == 1) 0 else actual
        assertEquals(
            expected = correctedExpected,
            actual = correctedActual,
            message = message,
        )
    }

    @Test
    fun testDecodeImageInfo() = runTest {
        ComposeResImageFiles.exifs.forEach { imageFile ->
            val imageInfo = runCatching {
                imageFile.toImageSource().decodeImageInfo()
            }.getOrNull()
            if (imageInfo != null) {
                assertEquals(
                    expected = imageFile.imageInfo,
                    actual = ExifOrientationHelper(imageFile.exifOrientation)
                        .applyToImageInfo(imageInfo),
                    message = imageFile.toString(),
                )
            }
        }
    }

    private operator fun IntSizeCompat.minus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width - other.width, this.height - other.height)
    }

    private operator fun IntSizeCompat.plus(other: IntSizeCompat): IntSizeCompat {
        return IntSizeCompat(this.width + other.width, this.height + other.height)
    }

    private fun getExtensionFromUrl(url: String): String? {
        if (url.isBlank()) return null
        return url
            .substringBeforeLast('#') // Strip the fragment.
            .substringBeforeLast('?') // Strip the query.
            .substringAfterLast('/') // Get the last path segment.
            .substringAfterLast('.', missingDelimiterValue = "") // Get the file extension.
            .trim()
            .takeIf { it.isNotEmpty() }
    }
}