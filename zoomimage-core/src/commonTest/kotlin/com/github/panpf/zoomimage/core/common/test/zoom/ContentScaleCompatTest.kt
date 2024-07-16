package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.SizeCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.name
import com.github.panpf.zoomimage.zoom.valueOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContentScaleCompatTest {

    @Test
    fun testComputeScaleFactor() {
        val dstSize = SizeCompat(1000f, 1000f)

        var srcSize = SizeCompat(100f, 50f)
        listOf(
            ContentScaleCompat.FillWidth to ScaleFactorCompat(10f, 10f),
            ContentScaleCompat.FillHeight to ScaleFactorCompat(20f, 20f),
            ContentScaleCompat.FillBounds to ScaleFactorCompat(10f, 20f),
            ContentScaleCompat.Fit to ScaleFactorCompat(10f, 10f),
            ContentScaleCompat.Crop to ScaleFactorCompat(20f, 20f),
            ContentScaleCompat.Inside to ScaleFactorCompat(1f, 1f),
            ContentScaleCompat.None to ScaleFactorCompat(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first}",
            )
        }

        srcSize = SizeCompat(50f, 100f)
        listOf(
            ContentScaleCompat.FillWidth to ScaleFactorCompat(20f, 20f),
            ContentScaleCompat.FillHeight to ScaleFactorCompat(10f, 10f),
            ContentScaleCompat.FillBounds to ScaleFactorCompat(20f, 10f),
            ContentScaleCompat.Fit to ScaleFactorCompat(10f, 10f),
            ContentScaleCompat.Crop to ScaleFactorCompat(20f, 20f),
            ContentScaleCompat.Inside to ScaleFactorCompat(1f, 1f),
            ContentScaleCompat.None to ScaleFactorCompat(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first.name}",
            )
        }

        srcSize = SizeCompat(2000f, 4000f)
        listOf(
            ContentScaleCompat.FillWidth to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillHeight to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.FillBounds to ScaleFactorCompat(0.5f, 0.25f),
            ContentScaleCompat.Fit to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.Crop to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.Inside to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.None to ScaleFactorCompat(1f, 1f),
        ).forEach {
            assertEquals(
                expected = it.second,
                actual = it.first.computeScaleFactor(srcSize, dstSize),
                message = "srcSize=$srcSize, dstSize=$dstSize, contentScale=${it.first.name}",
            )
        }

        srcSize = SizeCompat(4000f, 2000f)
        listOf(
            ContentScaleCompat.FillWidth to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.FillHeight to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.FillBounds to ScaleFactorCompat(0.25f, 0.5f),
            ContentScaleCompat.Fit to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.Crop to ScaleFactorCompat(0.5f, 0.5f),
            ContentScaleCompat.Inside to ScaleFactorCompat(0.25f, 0.25f),
            ContentScaleCompat.None to ScaleFactorCompat(1f, 1f),
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
            ContentScaleCompat.FillWidth to "FillWidth",
            ContentScaleCompat.FillHeight to "FillHeight",
            ContentScaleCompat.FillBounds to "FillBounds",
            ContentScaleCompat.Fit to "Fit",
            ContentScaleCompat.Crop to "Crop",
            ContentScaleCompat.Inside to "Inside",
            ContentScaleCompat.None to "None",
            MyContentScaleCompat.Default to "Unknown ContentScaleCompat: ${MyContentScaleCompat.Default}"
        ).forEach {
            assertEquals(it.first.name, it.second)
        }
    }

    @Test
    fun testValueOf() {
        listOf(
            ContentScaleCompat.FillWidth to "FillWidth",
            ContentScaleCompat.FillHeight to "FillHeight",
            ContentScaleCompat.FillBounds to "FillBounds",
            ContentScaleCompat.Fit to "Fit",
            ContentScaleCompat.Crop to "Crop",
            ContentScaleCompat.Inside to "Inside",
            ContentScaleCompat.None to "None",
        ).forEach {
            assertEquals(it.first, ContentScaleCompat.valueOf(it.second))
        }

        assertFailsWith(IllegalArgumentException::class) {
            ContentScaleCompat.valueOf(MyContentScaleCompat.Default.name)
        }
    }

    class MyContentScaleCompat : ContentScaleCompat {

        override fun computeScaleFactor(
            srcSize: SizeCompat,
            dstSize: SizeCompat
        ): ScaleFactorCompat {
            return ScaleFactorCompat(1f, 1f)
        }

        companion object {
            val Default = MyContentScaleCompat()
        }
    }
}