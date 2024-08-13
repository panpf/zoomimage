package com.github.panpf.zoomimage.compose.common.test.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.util.name
import com.github.panpf.zoomimage.compose.util.valueOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ComposePlatformUtilsContentScaleTest {

    @Test
    fun testComputeScaleFactor() {
        val dstSize = Size(1000f, 1000f)

        var srcSize = Size(100f, 50f)
        listOf(
            ContentScale.FillWidth to ScaleFactor(10f, 10f),
            ContentScale.FillHeight to ScaleFactor(20f, 20f),
            ContentScale.FillBounds to ScaleFactor(10f, 20f),
            ContentScale.Fit to ScaleFactor(10f, 10f),
            ContentScale.Crop to ScaleFactor(20f, 20f),
            ContentScale.Inside to ScaleFactor(1f, 1f),
            ContentScale.None to ScaleFactor(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first}",
            )
        }

        srcSize = Size(50f, 100f)
        listOf(
            ContentScale.FillWidth to ScaleFactor(20f, 20f),
            ContentScale.FillHeight to ScaleFactor(10f, 10f),
            ContentScale.FillBounds to ScaleFactor(20f, 10f),
            ContentScale.Fit to ScaleFactor(10f, 10f),
            ContentScale.Crop to ScaleFactor(20f, 20f),
            ContentScale.Inside to ScaleFactor(1f, 1f),
            ContentScale.None to ScaleFactor(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first.name}",
            )
        }

        srcSize = Size(2000f, 4000f)
        listOf(
            ContentScale.FillWidth to ScaleFactor(0.5f, 0.5f),
            ContentScale.FillHeight to ScaleFactor(0.25f, 0.25f),
            ContentScale.FillBounds to ScaleFactor(0.5f, 0.25f),
            ContentScale.Fit to ScaleFactor(0.25f, 0.25f),
            ContentScale.Crop to ScaleFactor(0.5f, 0.5f),
            ContentScale.Inside to ScaleFactor(0.25f, 0.25f),
            ContentScale.None to ScaleFactor(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first.name}",
            )
        }

        srcSize = Size(4000f, 2000f)
        listOf(
            ContentScale.FillWidth to ScaleFactor(0.25f, 0.25f),
            ContentScale.FillHeight to ScaleFactor(0.5f, 0.5f),
            ContentScale.FillBounds to ScaleFactor(0.25f, 0.5f),
            ContentScale.Fit to ScaleFactor(0.25f, 0.25f),
            ContentScale.Crop to ScaleFactor(0.5f, 0.5f),
            ContentScale.Inside to ScaleFactor(0.25f, 0.25f),
            ContentScale.None to ScaleFactor(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first.name}",
            )
        }
    }

    @Test
    fun testName() {
        listOf(
            ContentScale.FillWidth to "FillWidth",
            ContentScale.FillHeight to "FillHeight",
            ContentScale.FillBounds to "FillBounds",
            ContentScale.Fit to "Fit",
            ContentScale.Crop to "Crop",
            ContentScale.Inside to "Inside",
            ContentScale.None to "None",
            MyContentScale.Default to "Unknown ContentScale: ${MyContentScale.Default}"
        ).forEach {
            assertEquals(it.first.name, it.second)
        }
    }

    @Test
    fun testValueOf() {
        listOf(
            ContentScale.FillWidth to "FillWidth",
            ContentScale.FillHeight to "FillHeight",
            ContentScale.FillBounds to "FillBounds",
            ContentScale.Fit to "Fit",
            ContentScale.Crop to "Crop",
            ContentScale.Inside to "Inside",
            ContentScale.None to "None",
        ).forEach {
            assertEquals(it.first, ContentScale.valueOf(it.second))
        }

        assertFailsWith(IllegalArgumentException::class) {
            ContentScale.valueOf(MyContentScale.Default.name)
        }
    }

    class MyContentScale : ContentScale {

        override fun computeScaleFactor(
            srcSize: Size,
            dstSize: Size
        ): ScaleFactor {
            return ScaleFactor(1f, 1f)
        }

        companion object {
            val Default = MyContentScale()
        }
    }
}