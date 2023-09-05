package com.github.panpf.zoomimage.core.test.util

import com.github.panpf.tools4j.test.ktx.assertNoThrow
import com.github.panpf.tools4j.test.ktx.assertThrow
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.div
import com.github.panpf.zoomimage.util.plus
import com.github.panpf.zoomimage.util.times
import com.github.panpf.zoomimage.util.toShortString
import org.junit.Assert
import org.junit.Test

class TransformCompatTest {

    @Test
    fun testConstructor() {
        assertNoThrow {
            TransformCompat(
                ScaleFactorCompat(1.3f, 4.7f),
                OffsetCompat(156f, 97f),
            )
        }
        assertThrow(IllegalArgumentException::class) {
            TransformCompat(
                ScaleFactorCompat.Unspecified,
                OffsetCompat(156f, 97f),
            )
        }
        assertThrow(IllegalArgumentException::class) {
            TransformCompat(
                ScaleFactorCompat(1.3f, 4.7f),
                OffsetCompat.Unspecified,
            )
        }
    }

    @Test
    fun testProperties() {
        TransformCompat(
            ScaleFactorCompat(1.3f, 4.7f),
            OffsetCompat(156f, 97f),
        ).apply {
            Assert.assertEquals(1.3f, scaleX)
            Assert.assertEquals(4.7f, scaleY)
            Assert.assertEquals(156f, offsetX)
            Assert.assertEquals(97f, offsetY)
            Assert.assertEquals(0f, rotation)
            Assert.assertEquals(0.0f, scaleOriginX)
            Assert.assertEquals(0.0f, scaleOriginY)
            Assert.assertEquals(0.0f, rotationOriginX)
            Assert.assertEquals(0.0f, rotationOriginY)
        }

        TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 90f,
            scaleOrigin = TransformOriginCompat(0.3f, 0.7f),
            rotationOrigin = TransformOriginCompat(0.6f, 0.2f),
        ).apply {
            Assert.assertEquals(4.7f, scaleX)
            Assert.assertEquals(1.3f, scaleY)
            Assert.assertEquals(97f, offsetX)
            Assert.assertEquals(156f, offsetY)
            Assert.assertEquals(90f, rotation)
            Assert.assertEquals(0.3f, scaleOriginX)
            Assert.assertEquals(0.7f, scaleOriginY)
            Assert.assertEquals(0.6f, rotationOriginX)
            Assert.assertEquals(0.2f, rotationOriginY)
        }

        TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        ).apply {
            Assert.assertEquals(4.7f, scaleX)
            Assert.assertEquals(1.3f, scaleY)
            Assert.assertEquals(97f, offsetX)
            Assert.assertEquals(156f, offsetY)
            Assert.assertEquals(270f, rotation)
            Assert.assertEquals(0.7f, scaleOriginX)
            Assert.assertEquals(0.3f, scaleOriginY)
            Assert.assertEquals(0.2f, rotationOriginX)
            Assert.assertEquals(0.6f, rotationOriginY)
        }
    }

    @Test
    fun testOrigin() {
        TransformCompat.Origin.apply {
            Assert.assertEquals(1f, scaleX)
            Assert.assertEquals(1f, scaleY)
            Assert.assertEquals(0f, offsetX)
            Assert.assertEquals(0f, offsetY)
            Assert.assertEquals(0f, rotation)
            Assert.assertEquals(0.0f, scaleOriginX)
            Assert.assertEquals(0.0f, scaleOriginY)
            Assert.assertEquals(0.0f, rotationOriginX)
            Assert.assertEquals(0.0f, rotationOriginY)
        }

        Assert.assertEquals(TransformCompat.Origin, TransformCompat.Origin)
    }

    @Test
    fun testToString() {
        Assert.assertEquals(
            "(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)",
            TransformCompat.Origin.toShortString()
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            TransformCompat(
                ScaleFactorCompat(4.7f, 1.3f),
                OffsetCompat(97f, 156f),
                rotation = 270f,
                scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
                rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
            ).toShortString()
        )
    }

    @Test
    fun testTimes() {
        val transformCompat = TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transformCompat.toShortString()
        )

        Assert.assertEquals(
            "(9.4x1.95,194.0x234.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat * ScaleFactorCompat(2.0f, 1.5f)).toShortString()
        )

        Assert.assertEquals(
            "(7.05x2.6,145.5x312.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat * ScaleFactorCompat(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        val transformCompat = TransformCompat(
            ScaleFactorCompat(4.7f, 1.3f),
            OffsetCompat(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOriginCompat(0.7f, 0.3f),
            rotationOrigin = TransformOriginCompat(0.2f, 0.6f),
        )

        Assert.assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transformCompat.toShortString()
        )

        Assert.assertEquals(
            "(2.35x0.87,48.5x104.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat / ScaleFactorCompat(2.0f, 1.5f)).toShortString()
        )

        Assert.assertEquals(
            "(3.13x0.65,64.67x78.0,270.0,0.7x0.3,0.2x0.6)",
            (transformCompat / ScaleFactorCompat(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testPlus() {
        assertThrow(IllegalArgumentException::class) {
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }
        assertNoThrow {
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(1f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(3f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
            TransformCompat.Origin.copy(
                scale = ScaleFactorCompat(2f),
                scaleOrigin = TransformOriginCompat(0.5f, 0.5f),
            ).plus(
                TransformCompat.Origin.copy(
                    scale = ScaleFactorCompat(1f),
                    scaleOrigin = TransformOriginCompat(0.2f, 0.2f),
                )
            )
        }

        // todo Unit tests
    }
}