package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.TopStart
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ZoomableStateTest {

    @Test
    fun testRememberZoomableState() = runComposeUiTest {
        setContent {
            val zoomableState = rememberZoomableState()
            assertEquals(
                expected = rememberZoomImageLogger(),
                actual = zoomableState.logger
            )

            val logger = rememberZoomImageLogger(tag = "Test")
            val zoomableState2 = rememberZoomableState(logger)
            assertEquals(
                expected = logger,
                actual = zoomableState2.logger
            )
        }
    }

    @Test
    fun testLogger() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        assertEquals(expected = logger, actual = zoomable.logger)
    }

    @Test
    fun testContainerSize() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)

        zoomable.containerSize = IntSize(1000, 2000)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
    }

    @Test
    fun testContentSize() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)

        zoomable.containerSize = IntSize(1000, 2000)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.contentSize)

        zoomable.contentSize = IntSize(500, 300)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
        assertEquals(expected = IntSize(500, 300), actual = zoomable.contentSize)
    }

    @Test
    fun testContentOriginSize() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)

        zoomable.contentOriginSize = IntSize(4000, 2400)
        assertEquals(expected = IntSize(4000, 2400), actual = zoomable.contentOriginSize)
    }

    @Test
    fun testContentScale() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)

        zoomable.contentScale = ContentScale.Crop
        assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
    }

    @Test
    fun testAlignment() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = Alignment.Center, actual = zoomable.alignment)

        zoomable.alignment = Alignment.TopStart
        assertEquals(expected = Alignment.TopStart, actual = zoomable.alignment)
    }

    @Test
    fun testReadMode() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = null, actual = zoomable.readMode)

        zoomable.readMode = ReadMode.Default
        assertEquals(expected = ReadMode.Default, actual = zoomable.readMode)
    }

    @Test
    fun testScalesCalculator() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)

        zoomable.scalesCalculator = ScalesCalculator.Fixed
        assertEquals(expected = ScalesCalculator.Fixed, actual = zoomable.scalesCalculator)
    }

    @Test
    fun testThreeStepScale() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = false, actual = zoomable.threeStepScale)

        zoomable.threeStepScale = true
        assertEquals(expected = true, actual = zoomable.threeStepScale)
    }

    @Test
    fun testRubberBandScale() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = true, actual = zoomable.rubberBandScale)

        zoomable.rubberBandScale = false
        assertEquals(expected = false, actual = zoomable.rubberBandScale)
    }

    @Test
    fun testOneFingerScaleSpec() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = OneFingerScaleSpec.Default, actual = zoomable.oneFingerScaleSpec)

        zoomable.oneFingerScaleSpec = OneFingerScaleSpec(DefaultPanToScaleTransformer(100))
        assertEquals(
            expected = OneFingerScaleSpec(DefaultPanToScaleTransformer(100)),
            actual = zoomable.oneFingerScaleSpec
        )
    }

    @Test
    fun testAnimationSpec() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = ZoomAnimationSpec.Default, actual = zoomable.animationSpec)

        zoomable.animationSpec = ZoomAnimationSpec(durationMillis = 4000)
        assertEquals(
            expected = ZoomAnimationSpec(durationMillis = 4000),
            actual = zoomable.animationSpec
        )
    }

    @Test
    fun testLimitOffsetWithinBaseVisibleRect() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)

        zoomable.limitOffsetWithinBaseVisibleRect = true
        assertEquals(expected = true, actual = zoomable.limitOffsetWithinBaseVisibleRect)
    }

    @Test
    fun testDisabledGestureTypes() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = 0, actual = zoomable.disabledGestureTypes)

        zoomable.disabledGestureTypes = GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE
        assertEquals(
            expected = GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE,
            actual = zoomable.disabledGestureTypes
        )
    }

    @Test
    fun testReverseMouseWheelScale() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = false, actual = zoomable.reverseMouseWheelScale)

        zoomable.reverseMouseWheelScale = true
        assertEquals(expected = true, actual = zoomable.reverseMouseWheelScale)
    }

    @Test
    fun testScales() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(1.0f, 1.0f, 1.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(516, 516), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(1.0f, 3.0f, 9.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(0.34f, 8.02f, 24.07f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        // contentScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.contentScale = ContentScale.Crop
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(6.0f, 18.0f, 54.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        // rotation
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(0.34f, 8.02f, 24.07f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        // readMode
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.readMode = ReadMode.Default
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = ReadMode.Default, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }

        // scalesCalculator
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.scalesCalculator = ScalesCalculator.Fixed
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Fixed, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = listOf(0.34f, 1.02f, 3.05f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
        }
    }

    @Test
    fun testInitialTransform() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(516, 516), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.0f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(0f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin.Center
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        // contentScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.contentScale = ContentScale.Crop
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(0f, -4308f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        // rotation
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                }
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(243.42f, 0.58f),
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }

        // readMode
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.readMode = ReadMode.Default
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = ReadMode.Default, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(17.7f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(-4318.23f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6.0f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(0f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scalesCalculator
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.scalesCalculator = ScalesCalculator.Fixed
            }
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = null, actual = zoomable.readMode)
            assertEquals(expected = ScalesCalculator.Fixed, actual = zoomable.scalesCalculator)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    scaleOrigin = TransformOrigin.TopStart,
                    offset = Offset(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform.Origin.toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = zoomable.baseTransform.toString(),
                actual = zoomable.transform.toString()
            )
        }
    }

    @Test
    fun test() {
        val zoomable = ZoomableState(Logger("Test"))
        assertEquals(expected = Transform.Origin, actual = zoomable.baseTransform)
        assertEquals(expected = Transform.Origin, actual = zoomable.userTransform)
        assertEquals(expected = zoomable.baseTransform, actual = zoomable.transform)
        assertEquals(expected = 1f, actual = zoomable.minScale)
        assertEquals(expected = 1f, actual = zoomable.mediumScale)
        assertEquals(expected = 1f, actual = zoomable.maxScale)
        assertEquals(expected = 0, actual = zoomable.continuousTransformType)
        assertEquals(expected = IntRect.Zero, actual = zoomable.contentBaseDisplayRect)
        assertEquals(expected = IntRect.Zero, actual = zoomable.contentBaseVisibleRect)
        assertEquals(expected = IntRect.Zero, actual = zoomable.contentDisplayRect)
        assertEquals(expected = IntRect.Zero, actual = zoomable.contentVisibleRect)
        assertEquals(expected = ScrollEdge.Default, actual = zoomable.scrollEdge)
        assertEquals(expected = IntRect.Zero, actual = zoomable.userOffsetBounds)
    }

    @Test
    fun testUserOffsetBounds() {
        // TODO test
    }

    @Test
    fun testScrollEdge() {
        // TODO test
    }

    @Test
    fun testContentDisplayAndVisibleRect() {
        // TODO test
    }

    @Test
    fun testContinuousTransformType() {
        // TODO test
    }

    @Test
    fun testScale() {
        // TODO test
    }

    @Test
    fun testSwitchScale() {
        // TODO test
    }

    @Test
    fun testOffset() {
        // TODO test
    }

    @Test
    fun testLocate() {
        // TODO test
    }

    @Test
    fun testRotate() {
        // TODO test
    }

    @Test
    fun testGetNextStepScale() {
        // TODO test
    }

    @Test
    fun testTouchPointToContentPoint() {
        // TODO test
    }

    @Test
    fun testCanScroll() {
        // TODO test
    }

    @Test
    fun testRollbackScale() {
        // TODO test
    }

    @Test
    fun testGestureTransform() {
        // TODO test
    }

    @Test
    fun testFling() {
        // TODO test
    }

    @Test
    fun testCheckSupportGestureType() {
        // TODO test
    }

    @Test
    fun testToString() {
        // TODO test
    }
}