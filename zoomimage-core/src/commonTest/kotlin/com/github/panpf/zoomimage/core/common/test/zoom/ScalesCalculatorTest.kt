package com.github.panpf.zoomimage.core.common.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Result
import com.github.panpf.zoomimage.zoom.name
import kotlin.test.Test
import kotlin.test.assertEquals

class ScalesCalculatorTest {

    @Test
    fun testMultiple() {
        assertEquals(3f, ScalesCalculator.MULTIPLE, 0f)
    }

    @Test
    fun testFixed() {
        ScalesCalculator.Fixed.also {
            assertEquals(3f, it.multiple)
            assertEquals("FixedScalesCalculator(multiple=3.0)", it.toString())
        }

        ScalesCalculator.fixed().also {
            assertEquals(3f, it.multiple)
        }

        ScalesCalculator.fixed(multiple = 4f).also {
            assertEquals(4f, it.multiple)
        }

        ScalesCalculator.fixed(multiple = 5f).also {
            assertEquals(5f, it.multiple)
        }

        val containerSize = IntSizeCompat(1000, 1000)
        listOf(
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                expectedResult = Result(0.5f, mediumScale = 1.5f, maxScale = 4.5f).toString(),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8.0f).toString(),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.5f, maxScale = 10f).toString(),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.FillBounds,
                minScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val minScale = item.minScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.fixed(item.multiple).calculate(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                minScale = minScale,
                initialScale = item.initialScale ?: 0f
            ).toString()
            assertEquals(
                expected = item.expectedResult,
                actual = result,
                message = "index: $index, item=$item",
            )
        }
    }

    @Test
    fun testDynamic() {
        ScalesCalculator.Dynamic.also {
            assertEquals(3f, it.multiple)
            assertEquals("DynamicScalesCalculator(multiple=3.0)", it.toString())
        }

        ScalesCalculator.dynamic().also {
            assertEquals(3f, it.multiple)
        }

        ScalesCalculator.dynamic(multiple = 4f).also {
            assertEquals(4f, it.multiple)
        }

        ScalesCalculator.dynamic(multiple = 5f).also {
            assertEquals(5f, it.multiple)
        }

        val containerSize = IntSizeCompat(1000, 1000)
        listOf(
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // contentScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.FillBounds,
                minScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            // minScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 20f,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(20f, mediumScale = 60f, maxScale = 180f).toString(),
            ),
            // initialScale initialScale < minScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = null,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // initialScale initialScale > minScale and initialScale * multiple < contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 2f,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 5f, maxScale = 40f).toString(),
            ),
            // initialScale initialScale > minScale and initialScale * multiple > contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 2f,
                initialScale = 15f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 15f, maxScale = 45f).toString(),
            ),
            // multiple
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = null,
                initialScale = null,
                multiple = 5f,
                expectedResult = Result(10f, mediumScale = 50f, maxScale = 250f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val minScale = item.minScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.dynamic(item.multiple).calculate(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                minScale = minScale,
                initialScale = item.initialScale ?: 0f
            ).toString()
            assertEquals(
                expected = item.expectedResult,
                actual = result,
                message = "index: $index, item=$item",
            )
        }
    }

    data class TestItem(
        val contentSize: IntSizeCompat,
        val contentOriginSize: IntSizeCompat,
        val contentScale: ContentScaleCompat,
        val minScale: Float?,
        val initialScale: Float?,
        val multiple: Float = ScalesCalculator.MULTIPLE,
        val expectedResult: String,
    ) {
        override fun toString(): String {
            return "TestItem(contentSize=$contentSize, contentOriginSize=$contentOriginSize, contentScale=${contentScale.name}, minScale=$minScale, initialScale=$initialScale, multiple=$multiple, expectedResult=$expectedResult)"
        }
    }
}