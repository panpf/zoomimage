package com.github.panpf.zoomimage.core.test.zoom

import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import org.junit.Assert
import org.junit.Test

class ScalesCalculatorTest {

    @Test
    fun testMultiple() {
        Assert.assertEquals(3f, ScalesCalculator.Multiple, 0f)
    }

    @Test
    fun testFixed() {
        val containerSize = IntSizeCompat(1000, 1000)
        val contentSize = IntSizeCompat(100, 50)
        val contentOriginSize = IntSizeCompat(100, 50)
        val contentScale = ContentScaleCompat.FillWidth
        val minScale = 0.5f
        val initialScale = 0.5f
        ScalesCalculator.Fixed.calculate(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            minScale = minScale,
            initialScale = initialScale
        ).also {
            Assert.assertEquals(1.5f, it.mediumScale, 0f)
            Assert.assertEquals(4.5f, it.maxScale, 0f)
        }

        ScalesCalculator.fixed(4f).calculate(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            minScale = minScale,
            initialScale = initialScale
        ).also {
            Assert.assertEquals(2.0f, it.mediumScale, 0f)
            Assert.assertEquals(8.0f, it.maxScale, 0f)
        }

        ScalesCalculator.fixed(4f).calculate(
            containerSize = containerSize,
            contentSize = contentSize,
            contentOriginSize = contentOriginSize,
            contentScale = contentScale,
            minScale = minScale,
            initialScale = 2.5f
        ).also {
            Assert.assertEquals(2.5f, it.mediumScale, 0f)
            Assert.assertEquals(10f, it.maxScale, 0f)
        }
    }

//    @Test
//    fun testDynamic() {
//        val containerSize = IntSizeCompat(1000, 1000)
//        val contentSize = IntSizeCompat(100, 50)
//        val contentOriginSize = IntSizeCompat(100, 50)
//        val contentScale = ContentScaleCompat.FillWidth
//        val minScale = 0.5f
//        val initialScale = 0.5f
//        ScalesCalculator.Dynamic.calculate(
//            containerSize = containerSize,
//            contentSize = contentSize,
//            contentOriginSize = contentOriginSize,
//            contentScale = contentScale,
//            minScale = minScale,
//            initialScale = initialScale
//        ).also {
//            Assert.assertEquals(1.5f, it.mediumScale, 0f)
//            Assert.assertEquals(4.5f, it.maxScale, 0f)
//        }
//
//        ScalesCalculator.dynamic(4f).calculate(
//            containerSize = containerSize,
//            contentSize = contentSize,
//            contentOriginSize = contentOriginSize,
//            contentScale = contentScale,
//            minScale = minScale,
//            initialScale = initialScale
//        ).also {
//            Assert.assertEquals(2.0f, it.mediumScale, 0f)
//            Assert.assertEquals(8.0f, it.maxScale, 0f)
//        }
//
//        ScalesCalculator.dynamic(4f).calculate(
//            containerSize = containerSize,
//            contentSize = contentSize,
//            contentOriginSize = contentOriginSize,
//            contentScale = contentScale,
//            minScale = minScale,
//            initialScale = 2.5f
//        ).also {
//            Assert.assertEquals(2.5f, it.mediumScale, 0f)
//            Assert.assertEquals(10f, it.maxScale, 0f)
//        }
//    }
}