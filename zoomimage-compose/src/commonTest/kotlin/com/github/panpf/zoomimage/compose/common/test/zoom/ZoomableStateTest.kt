package com.github.panpf.zoomimage.compose.common.test.zoom

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.util.ScaleFactor
import com.github.panpf.zoomimage.compose.util.TopStart
import com.github.panpf.zoomimage.compose.util.format
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.test.waitMillis
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import com.github.panpf.zoomimage.zoom.Edge
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
    fun testConstructor() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger, LayoutDirection.Ltr)
        assertEquals(expected = logger, actual = zoomable.logger)
        @Suppress("USELESS_IS_CHECK")
        assertEquals(expected = true, actual = zoomable is RememberObserver)
    }

    @Test
    fun testContainerSize() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)

        zoomable.containerSize = IntSize(1000, 2000)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
    }

    @Test
    fun testContentSize() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)

        zoomable.containerSize = IntSize(1000, 2000)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
        assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)

        zoomable.contentSize = IntSize(500, 300)
        assertEquals(expected = IntSize(1000, 2000), actual = zoomable.containerSize)
        assertEquals(expected = IntSize(500, 300), actual = zoomable.contentSize)
    }

    @Test
    fun testContentOriginSize() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)

        zoomable.contentOriginSize = IntSize(4000, 2400)
        assertEquals(expected = IntSize(4000, 2400), actual = zoomable.contentOriginSize)
    }

    @Test
    fun testContentScale() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)

        zoomable.contentScale = ContentScale.Crop
        assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
    }

    @Test
    fun testAlignment() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = Alignment.Center, actual = zoomable.alignment)

        zoomable.alignment = Alignment.TopStart
        assertEquals(expected = Alignment.TopStart, actual = zoomable.alignment)

        // TODO test LayoutDirection
    }

    @Test
    fun testReadMode() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = null, actual = zoomable.readMode)

        zoomable.readMode = ReadMode.Default
        assertEquals(expected = ReadMode.Default, actual = zoomable.readMode)
    }

    @Test
    fun testScalesCalculator() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = ScalesCalculator.Dynamic, actual = zoomable.scalesCalculator)

        zoomable.scalesCalculator = ScalesCalculator.Fixed
        assertEquals(expected = ScalesCalculator.Fixed, actual = zoomable.scalesCalculator)
    }

    @Test
    fun testThreeStepScale() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = false, actual = zoomable.threeStepScale)

        zoomable.threeStepScale = true
        assertEquals(expected = true, actual = zoomable.threeStepScale)
    }

    @Test
    fun testRubberBandScale() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = true, actual = zoomable.rubberBandScale)

        zoomable.rubberBandScale = false
        assertEquals(expected = false, actual = zoomable.rubberBandScale)
    }

    @Test
    fun testOneFingerScaleSpec() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = OneFingerScaleSpec.Default, actual = zoomable.oneFingerScaleSpec)

        zoomable.oneFingerScaleSpec = OneFingerScaleSpec(DefaultPanToScaleTransformer(100))
        assertEquals(
            expected = OneFingerScaleSpec(DefaultPanToScaleTransformer(100)),
            actual = zoomable.oneFingerScaleSpec
        )
    }

    @Test
    fun testAnimationSpec() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = ZoomAnimationSpec.Default, actual = zoomable.animationSpec)

        zoomable.animationSpec = ZoomAnimationSpec(durationMillis = 4000)
        assertEquals(
            expected = ZoomAnimationSpec(durationMillis = 4000),
            actual = zoomable.animationSpec
        )
    }

    @Test
    fun testLimitOffsetWithinBaseVisibleRect() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)

        zoomable.limitOffsetWithinBaseVisibleRect = true
        assertEquals(expected = true, actual = zoomable.limitOffsetWithinBaseVisibleRect)
    }

    @Test
    fun testContainerWhitespace() {
        // TODO test
        // TODO test LayoutDirection
    }

    @Test
    fun testDisabledGestureTypes() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = 0, actual = zoomable.disabledGestureTypes)

        zoomable.disabledGestureTypes = GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE
        assertEquals(
            expected = GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE,
            actual = zoomable.disabledGestureTypes
        )
    }

    @Test
    fun testReverseMouseWheelScale() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(expected = false, actual = zoomable.reverseMouseWheelScale)

        zoomable.reverseMouseWheelScale = true
        assertEquals(expected = true, actual = zoomable.reverseMouseWheelScale)
    }

    @Test
    fun testMouseWheelScaleScrollDeltaConverter() {
        val zoomable = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(
            expected = 2.31f,
            actual = zoomable.mouseWheelScaleScrollDeltaConverter(7f).format(2)
        )
        assertEquals(
            expected = 1.65f,
            actual = zoomable.mouseWheelScaleScrollDeltaConverter(5f).format(2)
        )

        zoomable.mouseWheelScaleScrollDeltaConverter = { it * 0.5f }
        assertEquals(
            expected = 3.5f,
            actual = zoomable.mouseWheelScaleScrollDeltaConverter(7f).format(2)
        )
        assertEquals(
            expected = 2.5f,
            actual = zoomable.mouseWheelScaleScrollDeltaConverter(5f).format(2)
        )
    }

    @Test
    fun testScales() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            waitMillis(100)
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
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
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
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
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
            waitMillis(100)
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
    fun testContentBaseDisplayAndVisibleRect() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(244, 0, 273, 516).toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(244, 0, 273, 516).toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, -4308, 516, 4824).toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 718, 86, 804).toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
            )
        }

        // alignment
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.alignment = Alignment.BottomEnd
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.BottomEnd, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(487, 0, 516, 516).toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 244, 516, 273).toString(),
                actual = zoomable.contentBaseDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRect.toString()
            )
        }
    }

    @Test
    fun testContentDisplayAndVisibleRect() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(244, 0, 273, 516).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(244, 0, 273, 516).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(0, -4308, 516, 4824).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 718, 86, 804).toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }

        // alignment
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.alignment = Alignment.BottomEnd
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.BottomEnd, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(487, 0, 516, 516).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRect.toString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset.Zero.toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(0, 244, 516, 273).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }

        // scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 1.5f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.5f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset(-130.0f, -128.9f).toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(236, -129, 279, 645).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 254, 86, 1268).toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }

        // offset
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 1.5f, animated = false)
                    zoomable.offset(targetOffset = Offset(-180f, -172f), animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.5f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(
                expected = Offset(-130.0f, -172.0f).toString(),
                actual = zoomable.userTransform.offset.toString()
            )
            assertEquals(
                expected = IntRect(236, -172, 279, 602).toString(),
                actual = zoomable.contentDisplayRect.toString()
            )
            assertEquals(
                expected = IntRect(0, 338, 86, 1353).toString(),
                actual = zoomable.contentVisibleRect.toString()
            )
        }
    }

    @Test
    fun testUserOffsetBounds() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-1, 0, -1, 0).toString(),    // TODO why -1?, should be 0
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        // contentScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentScale = ContentScale.Crop
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(0, -4308, 0, 4308).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        // alignment
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.alignment = Alignment.BottomEnd
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.BottomEnd, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        // rotation
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(0, -1, 0, -1).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        // scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 1.5f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.5f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-130, -258, -130, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }

        // limitOffsetWithinBaseVisibleRect
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.contentScale = ContentScale.Crop
                    zoomable.limitOffsetWithinBaseVisibleRect = true
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = true, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect.Zero.toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
        }
    }

    @Test
    fun testScrollEdgeAndCanScroll() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH),
                actual = zoomable.scrollEdge
            )
            assertEquals(
                expected = listOf(false, false, false, false),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 20f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.NONE),
                actual = zoomable.scrollEdge,
            )
            assertEquals(
                expected = listOf(true, true, true, true),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.right + 1f
                    val addOffset = Offset(targetOffsetX - zoomable.userTransform.offsetX, 0f)
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 20f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.START, vertical = Edge.NONE),
                actual = zoomable.scrollEdge,
            )
            assertEquals(
                expected = listOf(true, false, true, true),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.left - 1f
                    val addOffset = Offset(targetOffsetX - zoomable.userTransform.offsetX, 0f)
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 20f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.END, vertical = Edge.NONE),
                actual = zoomable.scrollEdge,
            )
            assertEquals(
                expected = listOf(false, true, true, true),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                    val targetOffsetY = zoomable.userOffsetBounds.bottom + 1f
                    val addOffset = Offset(0f, targetOffsetY - zoomable.userTransform.offsetY)
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 20f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.START),
                actual = zoomable.scrollEdge,
            )
            assertEquals(
                expected = listOf(true, true, true, false),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                    val targetOffsetY = zoomable.userOffsetBounds.top - 1f
                    val addOffset = Offset(0f, targetOffsetY - zoomable.userTransform.offsetY)
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 20f, actual = zoomable.userTransform.scaleX.format(2))
            assertEquals(expected = false, actual = zoomable.limitOffsetWithinBaseVisibleRect)
            assertEquals(
                expected = IntRect(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.END),
                actual = zoomable.scrollEdge,
            )
            assertEquals(
                expected = listOf(true, true, false, true),
                actual = listOf(
                    zoomable.canScroll(horizontal = true, direction = 1),
                    zoomable.canScroll(horizontal = true, direction = -1),
                    zoomable.canScroll(horizontal = false, direction = 1),
                    zoomable.canScroll(horizontal = false, direction = -1),
                )
            )
        }
    }

    @Test
    fun testScale() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
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

        // targetScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 20f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(20f),
                    offset = Offset(-4912.99f, -4902.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6.78f),
                    offset = Offset(-32.99f, -4902.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // targetScale minScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.minScale * 0.9f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1f),
                    offset = Offset(-1f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(243f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // targetScale minScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.maxScale * 1.1f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-515.42f, -13440.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // targetScale centroidContentPoint top start
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(
                        targetScale = 20f,
                        centroidContentPoint = IntOffset.Zero,
                        animated = false,
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-0.3f, 0.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testSwitchScaleAndGetNexStepScale() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
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

        // switchScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(17.7f),
                    offset = Offset(-4318.0f, -4308.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6.0f),
                    offset = Offset(0.23f, -4308.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // switchScale, switchScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    zoomable.switchScale(animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1f),
                    offset = Offset(-1.0f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(243.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // switchScale, threeStepScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.threeStepScale = true
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 18.0f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(17.7f),
                    offset = Offset(-4318.0f, -4308.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6.0f),
                    offset = Offset(0.23f, -4308.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // switchScale, switchScale, threeStepScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.threeStepScale = true
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    zoomable.switchScale(animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-13470.46f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18f),
                    offset = Offset(-515.77f, -13440.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // switchScale, switchScale, switchScale, threeStepScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.threeStepScale = true
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false)
                    zoomable.switchScale(animated = false)
                    zoomable.switchScale(animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1f),
                    offset = Offset(-1.0f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(243.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // switchScale centroidContentPoint
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.switchScale(animated = false, centroidContentPoint = IntOffset.Zero)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(zoomable.minScale, zoomable.mediumScale, zoomable.maxScale)
                    .map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = zoomable.getNextStepScale().format(2)
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(17.7f),
                    offset = Offset(-4318.0f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(6.0f),
                    offset = Offset(0.23f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testOffset() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
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

        // scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = 20f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-515.42f, -13440.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale offset top start in bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.right - 1f
                    val targetOffsetY = zoomable.userOffsetBounds.bottom - 1f
                    val addOffset = Offset(
                        x = targetOffsetX - zoomable.userTransform.offsetX,
                        y = targetOffsetY - zoomable.userTransform.offsetY
                    )
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-12956.0f, -1.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-1.3f, -1.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale offset top start out bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.right + 1f
                    val targetOffsetY = zoomable.userOffsetBounds.bottom + 1f
                    val addOffset = Offset(
                        x = targetOffsetX - zoomable.userTransform.offsetX,
                        y = targetOffsetY - zoomable.userTransform.offsetY
                    )
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-0.3f, 0.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale offset bottom end in bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.left + 1f
                    val targetOffsetY = zoomable.userOffsetBounds.top + 1f
                    val addOffset = Offset(
                        x = targetOffsetX - zoomable.userTransform.offsetX,
                        y = targetOffsetY - zoomable.userTransform.offsetY
                    )
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-13986.0f, -26879.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-1031.3f, -26879.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale offset top bottom end bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = 20f, animated = false)
                    val targetOffsetX = zoomable.userOffsetBounds.left - 1f
                    val targetOffsetY = zoomable.userOffsetBounds.top - 1f
                    val addOffset = Offset(
                        x = targetOffsetX - zoomable.userTransform.offsetX,
                        y = targetOffsetY - zoomable.userTransform.offsetY
                    )
                    zoomable.offset(
                        targetOffset = zoomable.transform.offset + addOffset, animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.34f),
                    offset = Offset(244.0f, 0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(53.09f),
                    offset = Offset(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(18.0f),
                    offset = Offset(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOrigin(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testLocate() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // locate center, keep scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center,
                        targetScale = zoomable.transform.scaleX,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.0f),
                    offset = Offset(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // locate center, mediumScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center,
                        targetScale = zoomable.mediumScale,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-516.0f, -515.06f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-516.0f, -257.06f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // locate center, out minScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center,
                        targetScale = zoomable.minScale - 0.1f,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.0f),
                    offset = Offset(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // locate center, out maxScale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center,
                        targetScale = zoomable.maxScale + 0.1f,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-4128, -3353, 0, -774).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(9.0f),
                    offset = Offset(-2064.0f, -2061.19f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(4.22f),
                    offset = Offset(-2064.0f, -1287.19f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale, locate top start in bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center / 2f,
                        targetScale = zoomable.mediumScale,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-129.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-129.0f, 0.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale, locate top start out bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center / 2f * -1f,
                        targetScale = zoomable.mediumScale,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(0.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(0.0f, 0.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale, locate bottom end in bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center * 1.5f,
                        targetScale = zoomable.mediumScale,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-903.0f, -772.59f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-903.0f, -514.59f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale, locate bottom end out bounds
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.locate(
                        contentPoint = zoomable.contentSize.toIntRect().center * 2.5f,
                        targetScale = zoomable.mediumScale,
                        animated = false
                    )
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-1032.0f, -774.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-1032.0f, -516.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testRotate() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // rotate 90
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // rotate 180
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(180)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 180f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // rotate 270
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(270)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 270f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.mediumScale, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-516.0f, -515.37f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-516.0f, -257.37f),
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // rotate 90, scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                    zoomable.scale(zoomable.mediumScale, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-516.31f, -516.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-516.55f, -257.77f),
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // rotate 180, scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(180)
                    zoomable.scale(zoomable.mediumScale, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 180f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-516.0f, -516.31f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-516.0f, -258.31f),
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // rotate 270, scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.rotate(270)
                    zoomable.scale(zoomable.mediumScale, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 270f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(3.0f),
                    offset = Offset(-515.37f, -516.0f),
                ).toString(),
                actual = zoomable.userTransform.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(1.41f),
                    offset = Offset(-515.61f, -257.77f),
                    rotation = 270f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transform.toString()
            )
        }

        // scale, rotate 90
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.mediumScale, animated = false)
                    zoomable.rotate(90)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // scale, rotate 180
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.mediumScale, animated = false)
                    zoomable.rotate(180)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 180f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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

        // scale, rotate 270
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(1100, 733)
                LaunchedEffect(Unit) {
                    zoomable.scale(zoomable.mediumScale, animated = false)
                    zoomable.rotate(270)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = 270f, actual = zoomable.transform.rotation)
            assertEquals(
                expected = IntRect(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBounds.toString()
            )
            assertEquals(
                expected = Transform(
                    scale = ScaleFactor(0.47f),
                    offset = Offset(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOrigin(1.07f, 0.71f)
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
    fun testTouchPointToContentPoint() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                rememberZoomableState().apply { zoomableHolder = this }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize.Zero, actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (0, 0), (0, 0), (0, 0), (0, 0)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (0, 0), (0, 0), (0, 0), (0, 0)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }

        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize.Zero, actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (20, 381), (41, 761), (63, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (20, 381), (41, 761), (63, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Crop, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (22, 381), (43, 761), (65, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }

        // alignment
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                zoomable.alignment = Alignment.BottomEnd
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.BottomEnd, actual = zoomable.alignment)
            assertEquals(expected = 0.0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (20, 381), (41, 761), (63, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
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
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 90f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.0f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 1522), (20, 1142), (41, 761), (63, 381), (86, 0)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }

        // scale
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 1.5f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.5f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (20, 380), (41, 761), (62, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }

        // offset
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.scale(targetScale = zoomable.transform.scaleX * 1.5f, animated = false)
                    zoomable.offset(targetOffset = Offset(-180f, -172f), animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(expected = IntSize(516, 516), actual = zoomable.containerSize)
            assertEquals(expected = IntSize(86, 1522), actual = zoomable.contentSize)
            assertEquals(expected = IntSize(690, 12176), actual = zoomable.contentOriginSize)
            assertEquals(expected = ContentScale.Fit, actual = zoomable.contentScale)
            assertEquals(expected = Alignment.Center, actual = zoomable.alignment)
            assertEquals(expected = 0f, actual = zoomable.transform.rotation)
            assertEquals(expected = 1.5f, actual = zoomable.userTransform.scaleX.format(2))
            val contentDisplayCenter = zoomable.contentDisplayRect.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRect.size
            val add = (contentDisplaySize.toSize() / 4f).let { Offset(it.width, it.height) }
            assertEquals(
                expected = "(0, 0), (20, 381), (41, 761), (62, 1142), (86, 1522)",
                actual = listOf(
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add * 3f),
                    zoomable.touchPointToContentPoint(contentDisplayCenter - add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add),
                    zoomable.touchPointToContentPoint(contentDisplayCenter + add * 3f)
                ).joinToString()
            )
        }
    }

    @Test
    fun testCheckSupportGestureType() {
        val zoomableState = ZoomableState(Logger("Test"), LayoutDirection.Ltr)
        assertEquals(
            expected = "[ONE_FINGER_DRAG:true, TWO_FINGER_SCALE:true, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes = GestureType.ONE_FINGER_DRAG
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:true, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE or GestureType.KEYBOARD_SCALE
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:false, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.disabledGestureTypes =
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE or GestureType.KEYBOARD_SCALE or GestureType.KEYBOARD_DRAG
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:false, KEYBOARD_DRAG:false]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )
    }

    @Test
    fun testToString() {
        runComposeUiTest {
            var zoomableHolder: ZoomableState? = null
            setContent {
                val zoomable = rememberZoomableState().apply { zoomableHolder = this }
                zoomable.containerSize = IntSize(516, 516)
                zoomable.contentSize = IntSize(86, 1522)
                zoomable.contentOriginSize = IntSize(690, 12176)
                LaunchedEffect(Unit) {
                    zoomable.rotate(90)
                    zoomable.scale(targetScale = 20f, animated = false)
                }
            }
            waitMillis(100)
            val zoomable = zoomableHolder!!
            assertEquals(
                expected = "ZoomableState(" +
                        "containerSize=516x516, " +
                        "contentSize=86x1522, " +
                        "contentOriginSize=690x12176, " +
                        "contentScale=Fit, " +
                        "alignment=Center, " +
                        "minScale=0.339, " +
                        "mediumScale=8.0233, " +
                        "maxScale=24.0698, " +
                        "transform=(20.0x20.0,-602.0x-14961.42,90.0,0.0x0.0,0.08x1.47)" +
                        ")",
                actual = zoomable.toString()
            )
        }
    }

    // TODO test: rollbackScale, gestureTransform, fling, continuousTransformType
}