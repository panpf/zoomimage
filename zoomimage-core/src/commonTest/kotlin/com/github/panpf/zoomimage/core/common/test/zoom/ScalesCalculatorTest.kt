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
    fun testFixedWithBase() {
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
            // 0.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            // 1.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                expectedResult = Result(0.5f, mediumScale = 1.5f, maxScale = 4.5f).toString(),
            ),
            // 2.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 0.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8.0f).toString(),
            ),
            // 3.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.5f, maxScale = 10f).toString(),
            ),
            // 4.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.FillBounds,
                baseScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8f).toString(),
            ),
            // 5. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.4f,
                multiple = 3f,
                expectedResult = Result(2.8f, mediumScale = 8.4f, maxScale = 25.2f).toString(),
            ),
            // 6. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 3f, maxScale = 9.0f).toString(),
            ),
            // 7. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.4f,
                multiple = 3f,
                expectedResult = Result(6.4f, mediumScale = 19.2f, maxScale = 57.6f).toString(),
            ),
            // 8. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.5f,
                multiple = 3f,
                expectedResult = Result(5f, mediumScale = 6.5f, maxScale = 19.5f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val baseScale = item.baseScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.fixed(item.multiple).calculateWithBase(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                baseScale = baseScale,
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
            // 0.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            // 1.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                expectedResult = Result(0.5f, mediumScale = 1.5f, maxScale = 4.5f).toString(),
            ),
            // 2.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 0.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8.0f).toString(),
            ),
            // 3.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.5f, maxScale = 10f).toString(),
            ),
            // 4.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.FillBounds,
                baseScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                expectedResult = Result(0.5f, mediumScale = 2.0f, maxScale = 8f).toString(),
            ),
            // 5. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.4f,
                multiple = 3f,
                expectedResult = Result(2.0f, mediumScale = 2.8f, maxScale = 8.4f).toString(),
            ),
            // 6. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 3f, maxScale = 9.0f).toString(),
            ),
            // 7. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.4f,
                multiple = 3f,
                expectedResult = Result(5.0f, mediumScale = 6.4f, maxScale = 19.2f).toString(),
            ),
            // 8. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.5f,
                multiple = 3f,
                expectedResult = Result(5f, mediumScale = 6.5f, maxScale = 19.5f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val baseScale = item.baseScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.fixed(item.multiple).calculate(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                minScale = baseScale,
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
    fun testDynamicWithBase() {
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
            // 0.
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 1. contentScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.FillBounds,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            // 2. baseScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 20f,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(20f, mediumScale = 60f, maxScale = 180f).toString(),
            ),
            // 3. initialScale initialScale < baseScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(5f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 4. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.4f,
                multiple = 3f,
                expectedResult = Result(2.8f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 5. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 3f, maxScale = 40f).toString(),
            ),
            // 6. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.4f,
                multiple = 3f,
                expectedResult = Result(6.4f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 7. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.5f,
                multiple = 3f,
                expectedResult = Result(5f, mediumScale = 6.5f, maxScale = 40f).toString(),
            ),
            // 8. initialScale initialScale > baseScale and initialScale * multiple < contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 5f, maxScale = 40f).toString(),
            ),
            // 9. initialScale initialScale > baseScale and initialScale * multiple > contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 15f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 15f, maxScale = 45f).toString(),
            ),
            // 10. multiple
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 5f,
                expectedResult = Result(10f, mediumScale = 50f, maxScale = 250f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val baseScale = item.baseScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.dynamic(item.multiple).calculateWithBase(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                baseScale = baseScale,
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
            // 0
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 1. contentScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.FillBounds,
                baseScale = null,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 30f, maxScale = 90f).toString(),
            ),
            // 2. baseScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 20f,
                initialScale = null,
                multiple = 3f,
                expectedResult = Result(20f, mediumScale = 60f, maxScale = 180f).toString(),
            ),
            // 3. initialScale initialScale < baseScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(10f, mediumScale = 40f, maxScale = 120f).toString(),
            ),
            // 4. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.4f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 2.8f, maxScale = 40f).toString(),
            ),
            // 5. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 2f * 1.5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 3f, maxScale = 40f).toString(),
            ),
            // 6. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.4f,
                multiple = 3f,
                expectedResult = Result(5.0f, mediumScale = 6.4f, maxScale = 40f).toString(),
            ),
            // 7. initialScale initialScale > baseScale and initialScale / baseScale < 1.5f and abs(initialScale - baseScale) < 1.5f
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 5f,
                initialScale = 5f + 1.5f,
                multiple = 3f,
                expectedResult = Result(5f, mediumScale = 6.5f, maxScale = 40f).toString(),
            ),
            // 8. initialScale initialScale > baseScale and initialScale * multiple < contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 5f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 5f, maxScale = 40f).toString(),
            ),
            // 9. initialScale initialScale > baseScale and initialScale * multiple > contentOriginScale
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = 2f,
                initialScale = 15f,
                multiple = 3f,
                expectedResult = Result(2f, mediumScale = 15f, maxScale = 45f).toString(),
            ),
            // 10. multiple
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                baseScale = null,
                initialScale = null,
                multiple = 5f,
                expectedResult = Result(10f, mediumScale = 50f, maxScale = 250f).toString(),
            ),
        ).forEachIndexed { index, item ->
            val baseScale = item.baseScale ?: item.contentScale.computeScaleFactor(
                srcSize = item.contentSize.toSize(),
                dstSize = containerSize.toSize()
            ).scaleX
            val result = ScalesCalculator.dynamic(item.multiple).calculate(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                minScale = baseScale,
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
        val baseScale: Float?,
        val initialScale: Float?,
        val multiple: Float = ScalesCalculator.MULTIPLE,
        val expectedResult: String,
    ) {
        override fun toString(): String {
            return "TestItem(contentSize=$contentSize, contentOriginSize=$contentOriginSize, contentScale=${contentScale.name}, baseScale=$baseScale, initialScale=$initialScale, multiple=$multiple, expectedResult=$expectedResult)"
        }
    }
}