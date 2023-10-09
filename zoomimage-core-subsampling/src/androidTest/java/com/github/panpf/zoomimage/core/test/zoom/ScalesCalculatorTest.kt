package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScalesCalculator.Result
import com.github.panpf.zoomimage.zoom.name
import org.junit.Assert
import org.junit.Test

class ScalesCalculatorTest {

    @Test
    fun testMultiple() {
        Assert.assertEquals(3f, ScalesCalculator.Multiple, 0f)
    }

    @Test
    fun testFixed() {
        ScalesCalculator.Fixed.also {
            Assert.assertEquals(3f, it.multiple)
        }

        ScalesCalculator.fixed().also {
            Assert.assertEquals(3f, it.multiple)
        }

        ScalesCalculator.fixed(multiple = 4f).also {
            Assert.assertEquals(4f, it.multiple)
        }

        ScalesCalculator.fixed(multiple = 5f).also {
            Assert.assertEquals(5f, it.multiple)
        }

        val containerSize = IntSizeCompat(1000, 1000)
        listOf(
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 1.5f, maxScale = 4.5f),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 4f,
                differencePercentage = 2f,
                expectedResult = Result(mediumScale = 2.0f, maxScale = 8.0f),
            ),
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 2.5f,
                multiple = 4f,
                differencePercentage = 2f,
                expectedResult = Result(mediumScale = 2.5f, maxScale = 10f),
            ),
        ).forEachIndexed { index, item ->
            val result = ScalesCalculator.fixed(item.multiple).calculate(
                containerSize = containerSize,
                contentSize = item.contentSize,
                contentOriginSize = item.contentOriginSize,
                contentScale = item.contentScale,
                minScale = item.minScale,
                initialScale = item.initialScale
            )
            Assert.assertEquals("index: $index, item=$item", item.expectedResult, result)
        }
    }

    @Test
    fun testDynamic() {
        ScalesCalculator.Dynamic.also {
            Assert.assertEquals(3f, it.multiple)
            Assert.assertEquals(0.3f, it.differencePercentage)
        }

        ScalesCalculator.dynamic().also {
            Assert.assertEquals(3f, it.multiple)
            Assert.assertEquals(0.3f, it.differencePercentage)
        }

        ScalesCalculator.dynamic(multiple = 4f, differencePercentage = 0.2f).also {
            Assert.assertEquals(4f, it.multiple)
            Assert.assertEquals(0.2f, it.differencePercentage)
        }

        ScalesCalculator.dynamic(multiple = 5f, differencePercentage = 0.1f).also {
            Assert.assertEquals(5f, it.multiple)
            Assert.assertEquals(0.1f, it.differencePercentage)
        }

        val containerSize = IntSizeCompat(1000, 1000)
        listOf(
            // minMediumScale win
            TestItem(
                contentSize = IntSizeCompat(800, 900),
                contentOriginSize = IntSizeCompat(800, 900),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 1.5f, maxScale = 4.5f),
            ),
            // fillContainerScale win
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 20f, maxScale = 60f),
            ),
            // minMediumScale win because multiple
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(100, 50),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 50f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 25f, maxScale = 1250f),
            ),
            // contentOriginScale win
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 40f, maxScale = 120f),
            ),
            // FillBounds win base contentOriginScale win
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.FillBounds,
                minScale = 0.5f,
                initialScale = 0.5f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 1.5f, maxScale = 4.5f),
            ),
            // initialScale win
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 30f,
                multiple = 3f,
                differencePercentage = 0.3f,
                expectedResult = Result(mediumScale = 30f, maxScale = 90f),
            ),
            // initialScale fail because difference
            TestItem(
                contentSize = IntSizeCompat(100, 50),
                contentOriginSize = IntSizeCompat(4000, 2000),
                contentScale = ContentScaleCompat.Fit,
                minScale = 0.5f,
                initialScale = 30f,
                multiple = 3f,
                differencePercentage = 0.1f,
                expectedResult = Result(mediumScale = 40f, maxScale = 120f),
            ),
        ).forEachIndexed { index, item ->
            val result =
                ScalesCalculator.dynamic(item.multiple, item.differencePercentage).calculate(
                    containerSize = containerSize,
                    contentSize = item.contentSize,
                    contentOriginSize = item.contentOriginSize,
                    contentScale = item.contentScale,
                    minScale = item.minScale,
                    initialScale = item.initialScale
                )
            Assert.assertEquals("index: $index, item=$item", item.expectedResult, result)
        }
    }

    data class TestItem(
        val contentSize: IntSizeCompat,
        val contentOriginSize: IntSizeCompat,
        val contentScale: ContentScaleCompat,
        val minScale: Float,
        val initialScale: Float,
        val multiple: Float = ScalesCalculator.Multiple,
        val differencePercentage: Float = multiple / 2,
        val expectedResult: Result,
    ) {
        override fun toString(): String {
            return "TestItem(contentSize=$contentSize, contentOriginSize=$contentOriginSize, contentScale=${contentScale.name}, minScale=$minScale, initialScale=$initialScale, multiple=$multiple, differencePercentage=$differencePercentage, expectedResult=$expectedResult)"
        }
    }
}