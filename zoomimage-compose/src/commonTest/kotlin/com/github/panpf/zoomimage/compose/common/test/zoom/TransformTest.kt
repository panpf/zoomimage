package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ScaleFactor
import com.github.panpf.zoomimage.compose.util.ScaleFactor
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.div
import com.github.panpf.zoomimage.compose.zoom.isEmpty
import com.github.panpf.zoomimage.compose.zoom.lerp
import com.github.panpf.zoomimage.compose.zoom.minus
import com.github.panpf.zoomimage.compose.zoom.plus
import com.github.panpf.zoomimage.compose.zoom.times
import com.github.panpf.zoomimage.compose.zoom.toShortString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransformTest {

    @Test
    fun testConstructor() {
        Transform(
            ScaleFactor(1.3f, 4.7f),
            Offset(156f, 97f),
        )
        assertFailsWith(IllegalArgumentException::class) {
            Transform(
                ScaleFactor.Unspecified,
                Offset(156f, 97f),
            )
        }
        assertFailsWith(IllegalArgumentException::class) {
            Transform(
                ScaleFactor(1.3f, 4.7f),
                Offset.Unspecified,
            )
        }
    }

    @Test
    fun testProperties() {
        Transform(
            ScaleFactor(1.3f, 4.7f),
            Offset(156f, 97f),
        ).apply {
            assertEquals(1.3f, scaleX)
            assertEquals(4.7f, scaleY)
            assertEquals(156f, offsetX)
            assertEquals(97f, offsetY)
            assertEquals(0f, rotation)
            assertEquals(0.0f, scaleOriginX)
            assertEquals(0.0f, scaleOriginY)
            assertEquals(0.0f, rotationOriginX)
            assertEquals(0.0f, rotationOriginY)
        }

        Transform(
            ScaleFactor(4.7f, 1.3f),
            Offset(97f, 156f),
            rotation = 90f,
            scaleOrigin = TransformOrigin(0.3f, 0.7f),
            rotationOrigin = TransformOrigin(0.6f, 0.2f),
        ).apply {
            assertEquals(4.7f, scaleX)
            assertEquals(1.3f, scaleY)
            assertEquals(97f, offsetX)
            assertEquals(156f, offsetY)
            assertEquals(90f, rotation)
            assertEquals(0.3f, scaleOriginX)
            assertEquals(0.7f, scaleOriginY)
            assertEquals(0.6f, rotationOriginX)
            assertEquals(0.2f, rotationOriginY)
        }

        Transform(
            ScaleFactor(4.7f, 1.3f),
            Offset(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOrigin(0.7f, 0.3f),
            rotationOrigin = TransformOrigin(0.2f, 0.6f),
        ).apply {
            assertEquals(4.7f, scaleX)
            assertEquals(1.3f, scaleY)
            assertEquals(97f, offsetX)
            assertEquals(156f, offsetY)
            assertEquals(270f, rotation)
            assertEquals(0.7f, scaleOriginX)
            assertEquals(0.3f, scaleOriginY)
            assertEquals(0.2f, rotationOriginX)
            assertEquals(0.6f, rotationOriginY)
        }
    }

    @Test
    fun testOrigin() {
        Transform.Origin.apply {
            assertEquals(1f, scaleX)
            assertEquals(1f, scaleY)
            assertEquals(0f, offsetX)
            assertEquals(0f, offsetY)
            assertEquals(0f, rotation)
            assertEquals(0.0f, scaleOriginX)
            assertEquals(0.0f, scaleOriginY)
            assertEquals(0.0f, rotationOriginX)
            assertEquals(0.0f, rotationOriginY)
        }

        assertEquals(Transform.Origin, Transform.Origin)
    }

    @Test
    fun testToString() {
        assertEquals(
            expected = "Transform(scale=1.0x1.0, offset=0.0x0.0, rotation=0.0, scaleOrigin=0.0x0.0, rotationOrigin=0.0x0.0)",
            actual = Transform.Origin.toString()
        )

        assertEquals(
            expected = "Transform(scale=4.7x1.3, offset=97.0x156.0, rotation=270.0, scaleOrigin=0.7x0.3, rotationOrigin=0.2x0.6)",
            actual = Transform(
                ScaleFactor(4.7f, 1.3f),
                Offset(97f, 156f),
                rotation = 270f,
                scaleOrigin = TransformOrigin(0.7f, 0.3f),
                rotationOrigin = TransformOrigin(0.2f, 0.6f),
            ).toString()
        )
    }

    @Test
    fun testIsEmptyAndIsNotEmpty() {
        assertEquals(
            expected = true,
            actual = Transform(
                scale = ScaleFactor(1f),
                offset = Offset(0f, 0f),
                rotation = 0f
            ).isEmpty()
        )
        assertEquals(
            expected = true,
            actual = Transform(
                scale = ScaleFactor(0.995f),
                offset = Offset(0.004f, 0.004f),
                rotation = 0.004f
            ).isEmpty()
        )
        assertEquals(
            expected = false,
            actual = Transform(
                scale = ScaleFactor(0.994f),
                offset = Offset(0.004f, 0.004f),
                rotation = 0.004f
            ).isEmpty()
        )
        assertEquals(
            expected = false,
            actual = Transform(
                scale = ScaleFactor(0.995f),
                offset = Offset(0.006f, 0.006f),
                rotation = 0.004f
            ).isEmpty()
        )
        assertEquals(
            expected = false,
            actual = Transform(
                scale = ScaleFactor(0.995f),
                offset = Offset(0.004f, 0.004f),
                rotation = 0.006f
            ).isEmpty()
        )
    }

    @Test
    fun testToShortString() {
        assertEquals(
            "(1.0x1.0,0.0x0.0,0.0,0.0x0.0,0.0x0.0)",
            Transform.Origin.toShortString()
        )

        assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            Transform(
                ScaleFactor(4.7f, 1.3f),
                Offset(97f, 156f),
                rotation = 270f,
                scaleOrigin = TransformOrigin(0.7f, 0.3f),
                rotationOrigin = TransformOrigin(0.2f, 0.6f),
            ).toShortString()
        )
    }

    @Test
    fun testTimes() {
        val transform = Transform(
            ScaleFactor(4.7f, 1.3f),
            Offset(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOrigin(0.7f, 0.3f),
            rotationOrigin = TransformOrigin(0.2f, 0.6f),
        )

        assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transform.toShortString()
        )

        assertEquals(
            "(9.4x1.95,194.0x234.0,270.0,0.7x0.3,0.2x0.6)",
            (transform * ScaleFactor(2.0f, 1.5f)).toShortString()
        )

        assertEquals(
            "(7.05x2.6,145.5x312.0,270.0,0.7x0.3,0.2x0.6)",
            (transform * ScaleFactor(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testDiv() {
        val transform = Transform(
            ScaleFactor(4.7f, 1.3f),
            Offset(97f, 156f),
            rotation = 270f,
            scaleOrigin = TransformOrigin(0.7f, 0.3f),
            rotationOrigin = TransformOrigin(0.2f, 0.6f),
        )

        assertEquals(
            "(4.7x1.3,97.0x156.0,270.0,0.7x0.3,0.2x0.6)",
            transform.toShortString()
        )

        assertEquals(
            "(2.35x0.87,48.5x104.0,270.0,0.7x0.3,0.2x0.6)",
            (transform / ScaleFactor(2.0f, 1.5f)).toShortString()
        )

        assertEquals(
            "(3.13x0.65,64.67x78.0,270.0,0.7x0.3,0.2x0.6)",
            (transform / ScaleFactor(1.5f, 2.0f)).toShortString()
        )
    }

    @Test
    fun testPlus() {
        assertFailsWith(IllegalArgumentException::class) {
            // scale origin is different
            Transform.Origin.copy(
                scale = ScaleFactor(2f),
                scaleOrigin = TransformOrigin(0.5f, 0.5f),
            ).plus(
                Transform.Origin.copy(
                    scale = ScaleFactor(3f),
                    scaleOrigin = TransformOrigin(0.2f, 0.2f),
                )
            )
        }

        // start scale default
        Transform.Origin.copy(
            scale = ScaleFactor(1f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                scale = ScaleFactor(3f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), scaleOrigin)
        }

        // end scale default
        Transform.Origin.copy(
            scale = ScaleFactor(2f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        // start and end scale default
        Transform.Origin.copy(
            scale = ScaleFactor(1f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        assertFailsWith(IllegalArgumentException::class) {
            // rotation origin is different
            Transform.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOrigin(0.5f, 0.5f),
            ).plus(
                Transform.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(0.2f, 0.2f),
                )
            )
        }

        // start rotation default
        Transform.Origin.copy(
            rotation = 0f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                rotation = 180f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), rotationOrigin)
        }

        // end rotation default
        Transform.Origin.copy(
            rotation = 90f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        // start and end rotation default
        Transform.Origin.copy(
            rotation = 0f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        val transform1 = Transform(
            scale = ScaleFactor(2f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = Transform(
            scale = ScaleFactor(3f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        val transform3 = transform1.plus(transform2)
        assertEquals(
            "(6.0x6.0,547.0x2695.0,270.0,0.5x0.5,0.2x0.2)",
            transform3.toShortString()
        )
    }

    @Test
    fun testMinus() {
        assertFailsWith(IllegalArgumentException::class) {
            // scale origin is different
            Transform.Origin.copy(
                scale = ScaleFactor(2f),
                scaleOrigin = TransformOrigin(0.5f, 0.5f),
            ).minus(
                Transform.Origin.copy(
                    scale = ScaleFactor(3f),
                    scaleOrigin = TransformOrigin(0.2f, 0.2f),
                )
            )
        }

        // start scale default
        Transform.Origin.copy(
            scale = ScaleFactor(1f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).minus(
            Transform.Origin.copy(
                scale = ScaleFactor(3f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), scaleOrigin)
        }

        // end scale default
        Transform.Origin.copy(
            scale = ScaleFactor(2f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).minus(
            Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        // start and end scale default
        Transform.Origin.copy(
            scale = ScaleFactor(1f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        assertFailsWith(IllegalArgumentException::class) {
            // rotation origin is different
            Transform.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOrigin(0.5f, 0.5f),
            ).minus(
                Transform.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(0.2f, 0.2f),
                )
            )
        }

        // start rotation default
        Transform.Origin.copy(
            rotation = 0f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).minus(
            Transform.Origin.copy(
                rotation = 180f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), rotationOrigin)
        }

        // end rotation default
        Transform.Origin.copy(
            rotation = 90f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).minus(
            Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        // start and end rotation default
        Transform.Origin.copy(
            rotation = 0f,
            rotationOrigin = TransformOrigin(0.5f, 0.5f),
        ).plus(
            Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            )
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        val transform1 = Transform(
            scale = ScaleFactor(2f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = Transform(
            scale = ScaleFactor(3f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        val transform3 = transform1.plus(transform2)
        assertEquals(
            "(6.0x6.0,547.0x2695.0,270.0,0.5x0.5,0.2x0.2)",
            transform3.toShortString()
        )

        val transform4 = transform3.minus(transform1)
        assertEquals(
            transform2.toShortString(),
            transform4.toShortString()
        )
    }

    @Test
    fun testLerp() {
        assertFailsWith(IllegalArgumentException::class) {
            // scale origin is different
            lerp(
                start = Transform.Origin.copy(
                    scale = ScaleFactor(2f),
                    scaleOrigin = TransformOrigin(0.5f, 0.5f),
                ),
                stop = Transform.Origin.copy(
                    scale = ScaleFactor(3f),
                    scaleOrigin = TransformOrigin(0.2f, 0.2f),
                ),
                fraction = 0.5f
            )
        }

        // start scale default
        lerp(
            start = Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                scale = ScaleFactor(3f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), scaleOrigin)
        }

        // end scale default
        lerp(
            start = Transform.Origin.copy(
                scale = ScaleFactor(2f),
                scaleOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        // start and end scale default
        lerp(
            start = Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                scale = ScaleFactor(1f),
                scaleOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), scaleOrigin)
        }

        assertFailsWith(IllegalArgumentException::class) {
            // rotation origin is different
            lerp(
                start = Transform.Origin.copy(
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(0.5f, 0.5f),
                ),
                stop = Transform.Origin.copy(
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(0.2f, 0.2f),
                ),
                fraction = 0.5f
            )
        }

        // start rotation default
        lerp(
            start = Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                rotation = 180f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.2f, 0.2f), rotationOrigin)
        }

        // end rotation default
        lerp(
            start = Transform.Origin.copy(
                rotation = 90f,
                rotationOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        // start and end rotation default
        lerp(
            start = Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.5f, 0.5f),
            ),
            stop = Transform.Origin.copy(
                rotation = 0f,
                rotationOrigin = TransformOrigin(0.2f, 0.2f),
            ),
            fraction = 0.5f
        ).apply {
            assertEquals(TransformOrigin(0.5f, 0.5f), rotationOrigin)
        }

        val transform1 = Transform(
            scale = ScaleFactor(2f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(199f, 563f),
            rotation = 90f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(2.0x2.0,199.0x563.0,90.0,0.5x0.5,0.2x0.2)",
            transform1.toShortString()
        )

        val transform2 = Transform(
            scale = ScaleFactor(3f),
            scaleOrigin = TransformOrigin(0.5f, 0.5f),
            offset = Offset(-50f, 1006f),
            rotation = 180f,
            rotationOrigin = TransformOrigin(0.2f, 0.2f),
        )
        assertEquals(
            "(3.0x3.0,-50.0x1006.0,180.0,0.5x0.5,0.2x0.2)",
            transform2.toShortString()
        )

        lerp(transform1, transform2, fraction = 0f).apply {
            assertEquals(
                transform1.toShortString(),
                this.toShortString()
            )
        }
        lerp(transform1, transform2, fraction = 0.5f).apply {
            assertEquals(
                "(2.5x2.5,74.5x784.5,135.0,0.5x0.5,0.2x0.2)",
                this.toShortString()
            )
        }
        lerp(transform1, transform2, fraction = 1f).apply {
            assertEquals(
                transform2.toShortString(),
                this.toShortString()
            )
        }
    }
}