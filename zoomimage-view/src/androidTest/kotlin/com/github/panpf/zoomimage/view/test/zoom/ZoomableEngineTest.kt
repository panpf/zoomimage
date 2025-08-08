package com.github.panpf.zoomimage.view.test.zoom

import android.view.ViewGroup
import android.widget.ImageView
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.OffsetCompat
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.ScaleFactorCompat
import com.github.panpf.zoomimage.util.TopStart
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.util.TransformOriginCompat
import com.github.panpf.zoomimage.util.toIntRect
import com.github.panpf.zoomimage.util.toOffset
import com.github.panpf.zoomimage.util.toRect
import com.github.panpf.zoomimage.util.toSize
import com.github.panpf.zoomimage.view.util.format
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.DefaultPanToScaleTransformer
import com.github.panpf.zoomimage.zoom.Edge
import com.github.panpf.zoomimage.zoom.GestureType
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.ScrollEdge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

class ZoomableEngineTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val logger = Logger("Test")
        val zoomable = ZoomableEngine(logger, imageView)
        assertEquals(expected = logger, actual = zoomable.logger)
    }

    @Test
    fun testContainerSize() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)

        withContext(Dispatchers.Main) {
            zoomable.setContainerSize(IntSizeCompat(1000, 2000))
        }
        assertEquals(
            expected = IntSizeCompat(1000, 2000),
            actual = zoomable.containerSizeState.value
        )
    }

    @Test
    fun testContentSize() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = IntSizeCompat.Zero,
            actual = zoomable.contentSizeState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setContainerSize(IntSizeCompat(1000, 2000))
        }
        assertEquals(
            expected = IntSizeCompat(1000, 2000),
            actual = zoomable.containerSizeState.value
        )
        assertEquals(
            expected = IntSizeCompat.Zero,
            actual = zoomable.contentSizeState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setContentSize(IntSizeCompat(500, 300))
        }
        assertEquals(
            expected = IntSizeCompat(1000, 2000),
            actual = zoomable.containerSizeState.value
        )
        assertEquals(
            expected = IntSizeCompat(500, 300),
            actual = zoomable.contentSizeState.value
        )
    }

    @Test
    fun testContentOriginSize() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentOriginSizeState.value)

        withContext(Dispatchers.Main) {
            zoomable.setContentOriginSize(IntSizeCompat(4000, 2400))
        }
        assertEquals(
            expected = IntSizeCompat(4000, 2400),
            actual = zoomable.contentOriginSizeState.value
        )
    }

    @Test
    fun testContentScale() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = ContentScaleCompat.Fit, actual = zoomable.contentScaleState.value)

        withContext(Dispatchers.Main) {
            zoomable.setContentScale(ContentScaleCompat.Crop)
        }
        assertEquals(expected = ContentScaleCompat.Crop, actual = zoomable.contentScaleState.value)
    }

    @Test
    fun testAlignment() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)

        withContext(Dispatchers.Main) {
            zoomable.setAlignment(AlignmentCompat.TopStart)
        }
        assertEquals(expected = AlignmentCompat.TopStart, actual = zoomable.alignmentState.value)
    }

    // TODO test rtlLayoutDirection

    @Test
    fun testReadMode() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = null, actual = zoomable.readModeState.value)

        withContext(Dispatchers.Main) {
            zoomable.setReadMode(ReadMode.Default)
        }
        assertEquals(expected = ReadMode.Default, actual = zoomable.readModeState.value)
    }

    @Test
    fun testScalesCalculator() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = ScalesCalculator.Dynamic,
            actual = zoomable.scalesCalculatorState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setScalesCalculator(ScalesCalculator.Fixed)
        }
        assertEquals(
            expected = ScalesCalculator.Fixed,
            actual = zoomable.scalesCalculatorState.value
        )
    }

    @Test
    fun testThreeStepScale() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = false, actual = zoomable.threeStepScaleState.value)

        withContext(Dispatchers.Main) {
            zoomable.setThreeStepScale(true)
        }
        assertEquals(expected = true, actual = zoomable.threeStepScaleState.value)
    }

    @Test
    fun testRubberBandScale() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = true, actual = zoomable.rubberBandScaleState.value)

        withContext(Dispatchers.Main) {
            zoomable.setRubberBandScale(false)
        }
        assertEquals(expected = false, actual = zoomable.rubberBandScaleState.value)
    }

    @Test
    fun testOneFingerScaleSpec() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = OneFingerScaleSpec.Default,
            actual = zoomable.oneFingerScaleSpecState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setOneFingerScaleSpec(
                OneFingerScaleSpec(DefaultPanToScaleTransformer(100))
            )
        }
        assertEquals(
            expected = OneFingerScaleSpec(DefaultPanToScaleTransformer(100)),
            actual = zoomable.oneFingerScaleSpecState.value
        )
    }

    @Test
    fun testAnimationSpec() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = ZoomAnimationSpec.Default,
            actual = zoomable.animationSpecState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setAnimationSpec(ZoomAnimationSpec(durationMillis = 4000))
        }
        assertEquals(
            expected = ZoomAnimationSpec(durationMillis = 4000),
            actual = zoomable.animationSpecState.value
        )
    }

    @Test
    fun testLimitOffsetWithinBaseVisibleRect() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = false,
            actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
        )

        withContext(Dispatchers.Main) {
            zoomable.setLimitOffsetWithinBaseVisibleRect(true)
        }
        assertEquals(expected = true, actual = zoomable.limitOffsetWithinBaseVisibleRectState.value)
    }

    @Test
    fun testContainerWhitespace() {
        // TODO test ContainerWhitespace, LayoutDirection
    }

    @Test
    fun testDisabledGestureTypes() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(expected = 0, actual = zoomable.disabledGestureTypesState.value)

        zoomable.setDisabledGestureTypes(
            GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE
        )
        assertEquals(
            expected = GestureType.ONE_FINGER_SCALE or GestureType.TWO_FINGER_SCALE,
            actual = zoomable.disabledGestureTypesState.value
        )
    }

    @Test
    fun testScales() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            Thread.sleep(100)

            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(1.0f, 1.0f, 1.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(1.0f, 1.0f, 1.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(0.34f, 8.02f, 24.07f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(6.0f, 18.0f, 54.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            withContext(Dispatchers.Main) {
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(0.34f, 8.02f, 24.07f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        // readModeState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setReadMode(ReadMode.Default)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = ReadMode.Default, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }

        // scalesCalculatorState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setScalesCalculator(ScalesCalculator.Fixed)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Fixed,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = listOf(0.34f, 1.02f, 3.05f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
        }
    }

    @Test
    fun testInitialTransform() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(0f, -4308f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(243.42f, 0.58f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // readModeState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setReadMode(ReadMode.Default)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = ReadMode.Default, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Dynamic,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(17.7f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(-4318.23f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.0f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(0f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scalesCalculatorState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setScalesCalculator(ScalesCalculator.Fixed)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(expected = null, actual = zoomable.readModeState.value)
            assertEquals(
                expected = ScalesCalculator.Fixed,
                actual = zoomable.scalesCalculatorState.value
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    scaleOrigin = TransformOriginCompat.TopStart,
                    offset = OffsetCompat(244f, 0f),
                    rotation = 0f,
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }
    }

    @Test
    fun testContentBaseDisplayAndVisibleRect() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(244, 0, 273, 516).toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(244f, 0f, 273.2f, 516f).toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(244, 0, 273, 516).toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(244f, 0f, 273.2f, 516f).toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, -4308, 516, 4824).toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 718, 86, 804).toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, -4308f, 516f, 4824f).toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 718f, 86f, 804f).toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        // alignmentState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setAlignment(AlignmentCompat.BottomEnd)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = zoomable.alignmentState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(487, 0, 516, 516).toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(487f, 0f, 516.2f, 516f).toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            withContext(Dispatchers.Main) {
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 244, 516, 273).toString(),
                actual = zoomable.contentBaseDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentBaseVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 244f, 516f, 273.2f).toString(),
                actual = zoomable.contentBaseDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentBaseVisibleRectFState.value.toString()
            )
        }
    }

    @Test
    fun testContentDisplayAndVisibleRect() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(244, 0, 273, 516).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(244f, 0f, 273f, 516f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(244, 0, 273, 516).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(244f, 0f, 273f, 516f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, -4308, 516, 4824).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 718, 86, 804).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, -4308f, 516f, 4824f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 718f, 86f, 804f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        // alignmentState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setAlignment(AlignmentCompat.BottomEnd)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = zoomable.alignmentState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(487, 0, 516, 516).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(487f, 0f, 516f, 516f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 85.5f, 1522f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat.Zero.toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 244, 516, 273).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 0, 86, 1522).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 244f, 516f, 273f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 0f, 86f, 1522f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 1.5f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.5f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat(-130.0f, -128.9f).toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(236, -129, 279, 645).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 254, 86, 1268).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(236f, -128.9f, 279.5f, 645f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 253.7f, 86.0f, 1268.3f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }

        // offset
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 1.5f,
                    animated = false
                )
                zoomable.offset(targetOffset = OffsetCompat(-180f, -172f), animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.5f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = OffsetCompat(-130.0f, -172.0f).toString(),
                actual = zoomable.userTransformState.value.offset.toString()
            )
            assertEquals(
                expected = IntRectCompat(236, -172, 279, 602).toString(),
                actual = zoomable.contentDisplayRectState.value.toString()
            )
            assertEquals(
                expected = IntRectCompat(0, 338, 86, 1353).toString(),
                actual = zoomable.contentVisibleRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(236f, -172f, 279.5f, 602f).toString(),
                actual = zoomable.contentDisplayRectFState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, 338.2f, 86.0f, 1352.9f).toString(),
                actual = zoomable.contentVisibleRectFState.value.toString()
            )
        }
    }

    @Test
    fun testUserOffsetBounds() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),    // TODO why -1?, should be 0
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(-0.5f, 0f, -0.5f, 0f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(-0.5f, 0f, -0.5f, 0f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(0, -4308, 0, 4308).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, -4308f, 0f, 4308f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        // alignmentState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setAlignment(AlignmentCompat.BottomEnd)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = zoomable.alignmentState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(-0.1f, 0.0f, -0.1f, 0.0f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(0, -1, 0, -1).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(0f, -0.5f, 0f, -0.5f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 1.5f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.5f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-130, -258, -130, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat(-129.8f, -257.9f, -129.8f, 0f).toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }

        // limitOffsetWithinBaseVisibleRectState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentScale(ContentScaleCompat.Crop)
                zoomable.setLimitOffsetWithinBaseVisibleRect(true)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = true,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = RectCompat.Zero.toString(),
                actual = zoomable.userOffsetBoundsRectFState.value.toString()
            )
        }
    }

    @Test
    fun testScrollEdgeAndCanScroll() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.BOTH, vertical = Edge.BOTH),
                actual = zoomable.scrollEdgeState.value
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

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            withContext(Dispatchers.Main) {
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
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

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.right + 1f
                val addOffset =
                    OffsetCompat(targetOffsetX - zoomable.userTransformState.value.offsetX, 0f)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.START, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
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

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.left - 1f
                val addOffset =
                    OffsetCompat(targetOffsetX - zoomable.userTransformState.value.offsetX, 0f)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.END, vertical = Edge.NONE),
                actual = zoomable.scrollEdgeState.value,
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

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.bottom + 1f
                val addOffset =
                    OffsetCompat(0f, targetOffsetY - zoomable.userTransformState.value.offsetY)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.START),
                actual = zoomable.scrollEdgeState.value,
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

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.top - 1f
                val addOffset =
                    OffsetCompat(0f, targetOffsetY - zoomable.userTransformState.value.offsetY)
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 20f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            assertEquals(
                expected = false,
                actual = zoomable.limitOffsetWithinBaseVisibleRectState.value
            )
            assertEquals(
                expected = IntRectCompat(-4947, -9804, -4880, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = ScrollEdge(horizontal = Edge.NONE, vertical = Edge.END),
                actual = zoomable.scrollEdgeState.value,
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
    fun testScale() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // targetScale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 20f,
                    animated = false
                )
            }

            Thread.sleep(100)
            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(20f),
                    offset = OffsetCompat(-4912.99f, -4902.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.78f),
                    offset = OffsetCompat(-32.99f, -4902.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // targetScale minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = zoomable.minScaleState.value * 0.9f, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1f),
                    offset = OffsetCompat(-1f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(243f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // targetScale minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = zoomable.maxScaleState.value * 1.1f, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-515.42f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // targetScale centroidContentPoint top start
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = 20f,
                    centroidContentPoint = IntOffsetCompat.Zero,
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-0.3f, 0.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // targetScale centroidContentPointF bottom end
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(
                    targetScale = 20f,
                    centroidContentPointF = OffsetCompat(
                        x = zoomable.contentSizeState.value.width.toFloat(),
                        y = zoomable.contentSizeState.value.height.toFloat()
                    ),
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testScaleBy() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleBy
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleBy(
                    addScale = 20f,
                    animated = false
                )
            }

            Thread.sleep(100)
            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(20f),
                    offset = OffsetCompat(-4912.99f, -4902.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.78f),
                    offset = OffsetCompat(-32.99f, -4902.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleBy minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleBy(
                    addScale = (zoomable.minScaleState.value * 0.9f) / zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1f),
                    offset = OffsetCompat(-1f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(243f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleBy minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleBy(
                    addScale = (zoomable.maxScaleState.value * 1.1f) / zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-515.42f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleBy centroidContentPoint top start
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleBy(
                    addScale = 20f / zoomable.transformState.value.scaleX,
                    centroidContentPoint = IntOffsetCompat.Zero,
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-0.3f, 0.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleBy centroidContentPointF bottom end
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleBy(
                    addScale = 20f / zoomable.transformState.value.scaleX,
                    centroidContentPointF = OffsetCompat(
                        x = zoomable.contentSizeState.value.width.toFloat(),
                        y = zoomable.contentSizeState.value.height.toFloat()
                    ),
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testScaleByPlus() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleByPlus
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleByPlus(
                    addScale = (zoomable.transformState.value.scaleX * 20f) - zoomable.transformState.value.scaleX,
                    animated = false
                )
            }

            Thread.sleep(100)
            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(20f),
                    offset = OffsetCompat(-4912.99f, -4902.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.78f),
                    offset = OffsetCompat(-32.99f, -4902.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleByPlus minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleByPlus(
                    addScale = (zoomable.minScaleState.value * 0.9f) - zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1f),
                    offset = OffsetCompat(-1f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(243f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleByPlus minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleByPlus(
                    addScale = (zoomable.maxScaleState.value * 1.1f) - zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-515.42f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleByPlus centroidContentPoint top start
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleByPlus(
                    addScale = 20f - zoomable.transformState.value.scaleX,
                    centroidContentPoint = IntOffsetCompat.Zero,
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-0.3f, 0.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scaleByPlus centroidContentPointF bottom end
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scaleByPlus(
                    addScale = 20f - zoomable.transformState.value.scaleX,
                    centroidContentPointF = OffsetCompat(
                        x = zoomable.contentSizeState.value.width.toFloat(),
                        y = zoomable.contentSizeState.value.height.toFloat()
                    ),
                    animated = false,
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testSwitchScaleAndGetNexStepScale() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.switchScale(animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(17.7f),
                    offset = OffsetCompat(-4318.0f, -4308.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.0f),
                    offset = OffsetCompat(0.23f, -4308.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale, switchScale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.switchScale(animated = false)
                zoomable.switchScale(animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1f),
                    offset = OffsetCompat(-1.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(243.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale, threeStepScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setThreeStepScale(true)
                zoomable.switchScale(animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 18.0f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(17.7f),
                    offset = OffsetCompat(-4318.0f, -4308.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.0f),
                    offset = OffsetCompat(0.23f, -4308.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale, switchScale, threeStepScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setThreeStepScale(true)
                zoomable.switchScale(animated = false)
                zoomable.switchScale(animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.23f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18f),
                    offset = OffsetCompat(-515.53f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale, switchScale, switchScale, threeStepScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setThreeStepScale(true)
                zoomable.switchScale(animated = false)
                zoomable.switchScale(animated = false)
                zoomable.switchScale(animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 6.0f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1f),
                    offset = OffsetCompat(-1.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(243.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale centroidContentPoint
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.switchScale(animated = false, centroidContentPoint = IntOffsetCompat.Zero)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(17.7f),
                    offset = OffsetCompat(-4318.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.0f),
                    offset = OffsetCompat(0.23f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // switchScale centroidContentPointF
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.switchScale(
                    animated = false,
                    centroidContentPointF = OffsetCompat(
                        x = zoomable.contentSizeState.value.width.toFloat(),
                        y = zoomable.contentSizeState.value.height.toFloat()
                    )
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = listOf(0.34f, 6.0f, 18.0f),
                actual = listOf(
                    zoomable.minScaleState.value,
                    zoomable.mediumScaleState.value,
                    zoomable.maxScaleState.value
                ).map { it.format(2) }
            )
            assertEquals(
                expected = 0.34f,
                actual = withContext(Dispatchers.Main) { zoomable.getNextStepScale() }.format(2)
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(17.7f),
                    offset = OffsetCompat(-4318.0f, -8616.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(6.0f),
                    offset = OffsetCompat(0.23f, -8616.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testOffset() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-515.42f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offset top start in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.right - 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.bottom - 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12956.0f, -1.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1.3f, -1.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offset top start out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.right + 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.bottom + 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-0.3f, 0.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offset bottom end in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.left + 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.top + 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13986.0f, -26879.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1031.3f, -26879.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offset top bottom end bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.left - 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.top - 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offset(
                    targetOffset = zoomable.transformState.value.offset + addOffset,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testOffsetBy() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1, 0, -1, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13470.12f, -13440.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-515.42f, -13440.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offsetBy top start in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.right - 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.bottom - 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offsetBy(addOffset = addOffset, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12956.0f, -1.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1.3f, -1.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offsetBy top start out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.right + 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.bottom + 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offsetBy(addOffset = addOffset, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-12955.0f, 0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-0.3f, 0.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offsetBy bottom end in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.left + 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.top + 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offsetBy(addOffset = addOffset, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13986.0f, -26879.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1031.3f, -26879.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale offsetBy top bottom end bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.scale(targetScale = 20f, animated = false)
                val targetOffsetX = zoomable.userOffsetBoundsRectState.value.left - 1f
                val targetOffsetY = zoomable.userOffsetBoundsRectState.value.top - 1f
                val addOffset = OffsetCompat(
                    x = targetOffsetX - zoomable.userTransformState.value.offsetX,
                    y = targetOffsetY - zoomable.userTransformState.value.offsetY
                )
                zoomable.offsetBy(addOffset = addOffset, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-13987, -26880, -12955, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.34f),
                    offset = OffsetCompat(244.0f, 0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(53.09f),
                    offset = OffsetCompat(-13987.0f, -26880.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(18.0f),
                    offset = OffsetCompat(-1032.3f, -26880.0f),
                    rotationOrigin = TransformOriginCompat(0.08f, 1.47f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testLocate() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, keep scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center,
                    targetScale = zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.0f),
                    offset = OffsetCompat(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, mediumScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.06f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.06f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, out minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center,
                    targetScale = zoomable.minScaleState.value - 0.1f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.0f),
                    offset = OffsetCompat(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, out maxScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center,
                    targetScale = zoomable.maxScaleState.value + 0.1f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-4128, -3353, 0, -774).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(9.0f),
                    offset = OffsetCompat(-2064.0f, -2061.19f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(4.22f),
                    offset = OffsetCompat(-2064.0f, -1287.19f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate top start in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center / 2f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-129.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-129.0f, 0.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate top start out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center / 2f * -1f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(0.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(0.0f, 0.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate bottom end in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center * 1.5f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-903.0f, -772.59f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-903.0f, -514.59f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate bottom end out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().center * 2.5f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-1032.0f, -774.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-1032.0f, -516.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testLocate2() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, keep scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().toRect().center,
                    targetScale = zoomable.transformState.value.scaleX,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.0f),
                    offset = OffsetCompat(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, mediumScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().toRect().center,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.77f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.77f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, out minScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().toRect().center,
                    targetScale = zoomable.minScaleState.value - 0.1f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.0f),
                    offset = OffsetCompat(0f, 0.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // locate center, out maxScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().toRect().center,
                    targetScale = zoomable.maxScaleState.value + 0.1f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-4128, -3353, 0, -774).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(9.0f),
                    offset = OffsetCompat(-2064.0f, -2063.3f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(4.22f),
                    offset = OffsetCompat(-2064.0f, -1289.3f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate top start in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect().toRect().center / 2f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-129.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-129.0f, 0.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate top start out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect()
                        .toRect().center / 2f * -1f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(0.0f, -258.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(0.0f, 0.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate bottom end in bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect()
                        .toRect().center * 1.5f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-903.0f, -773.65f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-903.0f, -515.65f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, locate bottom end out bounds
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.locate(
                    contentPoint = zoomable.contentSizeState.value.toIntRect()
                        .toRect().center * 2.5f,
                    targetScale = zoomable.mediumScaleState.value,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-1032.0f, -774.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-1032.0f, -516.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // The animation effect of scale cannot be tested because the time delay is invalid in the kotlin test environment
    }

    @Test
    fun testRotate() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
            }
            Thread.sleep(100)

            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 90
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 180
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(180)
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 270
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(270)
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.84f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.84f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 90, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(90)
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-515.84f, -516.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.08f, -257.77f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 180, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(180)
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.84f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.84f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotate 270, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotate(270)
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-515.84f, -516.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.08f, -257.77f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotate 90
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotate 180
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotate(180)
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotate 270
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotate(270)
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }
    }

    @Test
    fun testRotateBy() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
            }
            Thread.sleep(100)

            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 90
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(90 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 180
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(180 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 270
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(270 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.84f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.84f),
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 90, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(90 - zoomable.transformState.value.rotation.roundToInt())
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-515.84f, -516.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.08f, -257.77f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 180, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(180 - zoomable.transformState.value.rotation.roundToInt())
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-1032, -774, 0, -258).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-516.0f, -515.84f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.0f, -257.84f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // rotateBy 270, scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.rotateBy(270 - zoomable.transformState.value.rotation.roundToInt())
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(-774, -1032, -258, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(3.0f),
                    offset = OffsetCompat(-515.84f, -516.0f),
                ).toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(1.41f),
                    offset = OffsetCompat(-516.08f, -257.77f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotateBy 90
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotateBy(90 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 90f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotateBy 180
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotateBy(180 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 180f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(0.0f, 86.0f),
                    rotation = 180f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }

        // scale, rotateBy 270
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(1100, 733))
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
                zoomable.rotateBy(270 - zoomable.transformState.value.rotation.roundToInt())
            }
            Thread.sleep(100)

            assertEquals(expected = 270f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = IntRectCompat(0, 0, 0, 0).toString(),
                actual = zoomable.userOffsetBoundsRectState.value.toString()
            )
            assertEquals(
                expected = TransformCompat(
                    scale = ScaleFactorCompat(0.47f),
                    offset = OffsetCompat(-0.08f, 86.08f),
                    rotation = 270f,
                    rotationOrigin = TransformOriginCompat(1.07f, 0.71f)
                ).toString(),
                actual = zoomable.baseTransformState.value.toString()
            )
            assertEquals(
                expected = TransformCompat.Origin.toString(),
                actual = zoomable.userTransformState.value.toString()
            )
            assertEquals(
                expected = zoomable.baseTransformState.value.toString(),
                actual = zoomable.transformState.value.toString()
            )
        }
    }

    @Test
    fun testTouchPointToContentPoint() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity)
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.containerSizeState.value)
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(expected = IntSizeCompat.Zero, actual = zoomable.contentSizeState.value)
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0), OffsetCompat(0.0, 0.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat.Zero,
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(19.9, 380.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.7, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(19.9, 380.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.7, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        // contentScaleState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setContentScale(ContentScaleCompat.Crop)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Crop,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(21.5, 380.5), OffsetCompat(43.0, 761.0), OffsetCompat(64.5, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        // alignmentState.value
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.setAlignment(AlignmentCompat.BottomEnd)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(
                expected = AlignmentCompat.BottomEnd,
                actual = zoomable.alignmentState.value
            )
            assertEquals(expected = 0.0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(19.9, 380.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.7, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        // rotation
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.rotate(90)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 90f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.0f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 1522.0), OffsetCompat(19.9, 1141.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.7, 380.5), OffsetCompat(86.0, 0.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        // scale
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 1.5f,
                    animated = false
                )
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.5f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(20.2, 380.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.4, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }

        // offset
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.scale(
                    targetScale = zoomable.transformState.value.scaleX * 1.5f,
                    animated = false
                )
                zoomable.offset(targetOffset = OffsetCompat(-180f, -172f), animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = IntSizeCompat(516, 516),
                actual = zoomable.containerSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(86, 1522),
                actual = zoomable.contentSizeState.value
            )
            assertEquals(
                expected = IntSizeCompat(690, 12176),
                actual = zoomable.contentOriginSizeState.value
            )
            assertEquals(
                expected = ContentScaleCompat.Fit,
                actual = zoomable.contentScaleState.value
            )
            assertEquals(expected = AlignmentCompat.Center, actual = zoomable.alignmentState.value)
            assertEquals(expected = 0f, actual = zoomable.transformState.value.rotation)
            assertEquals(
                expected = 1.5f,
                actual = zoomable.userTransformState.value.scaleX.format(2)
            )
            val contentDisplayCenter = zoomable.contentDisplayRectState.value.center.toOffset()
            val contentDisplaySize = zoomable.contentDisplayRectState.value.size
            val add = (contentDisplaySize.toSize() / 4f).let { OffsetCompat(it.width, it.height) }
            withContext(Dispatchers.Main) {
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
                assertEquals(
                    expected = "OffsetCompat(0.0, 0.0), OffsetCompat(20.2, 380.5), OffsetCompat(41.3, 761.0), OffsetCompat(62.4, 1141.5), OffsetCompat(86.0, 1522.0)",
                    actual = listOf(
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add * 3f),
                        zoomable.touchPointToContentPointF(contentDisplayCenter - add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add),
                        zoomable.touchPointToContentPointF(contentDisplayCenter + add * 3f)
                    ).joinToString()
                )
            }
        }
    }

    @Test
    fun testCheckSupportGestureType() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomableState = ZoomableEngine(Logger("Test"), imageView)
        assertEquals(
            expected = "[ONE_FINGER_DRAG:true, TWO_FINGER_SCALE:true, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(GestureType.ONE_FINGER_DRAG)
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:true, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:true, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:true, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:true, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:true, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE or GestureType.KEYBOARD_SCALE
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:false, KEYBOARD_DRAG:true]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )

        zoomableState.setDisabledGestureTypes(
            GestureType.ONE_FINGER_DRAG or GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE or GestureType.MOUSE_WHEEL_SCALE or GestureType.KEYBOARD_SCALE or GestureType.KEYBOARD_DRAG
        )
        assertEquals(
            expected = "[ONE_FINGER_DRAG:false, TWO_FINGER_SCALE:false, ONE_FINGER_SCALE:false, DOUBLE_TAP_SCALE:false, MOUSE_WHEEL_SCALE:false, KEYBOARD_SCALE:false, KEYBOARD_DRAG:false]",
            actual = GestureType.values.joinToString(prefix = "[", postfix = "]") {
                "${GestureType.name(it)}:${zoomableState.checkSupportGestureType(it)}"
            }
        )
    }

    @Test
    fun testToString() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
                zoomable.setContentOriginSize(IntSizeCompat(690, 12176))
                zoomable.rotate(90)
                zoomable.scale(targetScale = 20f, animated = false)
            }
            Thread.sleep(100)

            assertEquals(
                expected = "ZoomableEngine(" +
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

    // TODO test: rollbackScale, gestureTransform, fling, continuousTransformType, reset
}