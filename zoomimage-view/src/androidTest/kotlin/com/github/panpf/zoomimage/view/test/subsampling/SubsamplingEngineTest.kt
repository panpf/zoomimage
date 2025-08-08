package com.github.panpf.zoomimage.view.test.subsampling

import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.test.TestActivity
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.TestTileImageCache
import com.github.panpf.zoomimage.test.delayUntil
import com.github.panpf.zoomimage.test.suspendLaunchActivityWithUse
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.view.subsampling.SubsamplingEngine
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class SubsamplingEngineTest {

    @Test
    fun testConstructor() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertSame(expected = zoomable, actual = subsampling.zoomableEngine)
        assertSame(expected = zoomable.logger, actual = subsampling.logger)
        assertSame(expected = zoomable.view, actual = subsampling.view)
    }

    @Test
    fun testTileImageCache() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(expected = null, actual = subsampling.tileImageCacheState.value)

        val testTileImageCache = TestTileImageCache()
        withContext(Dispatchers.Main) {
            subsampling.setTileImageCache(testTileImageCache)
        }
        assertSame(expected = testTileImageCache, actual = subsampling.tileImageCacheState.value)
    }

    @Test
    fun testDisabledTileImageCache() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(expected = false, actual = subsampling.disabledTileImageCacheState.value)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledTileImageCache(true)
        }
        assertEquals(expected = true, actual = subsampling.disabledTileImageCacheState.value)
    }

    @Test
    fun testTileAnimationSpec() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(
            expected = TileAnimationSpec.Default,
            actual = subsampling.tileAnimationSpecState.value
        )

        withContext(Dispatchers.Main) {
            subsampling.setTileAnimationSpec(TileAnimationSpec.None)
        }
        assertEquals(
            expected = TileAnimationSpec.None,
            actual = subsampling.tileAnimationSpecState.value
        )
    }

    @Test
    fun testPausedContinuousTransformTypes() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(
            expected = TileManager.DefaultPausedContinuousTransformTypes,
            actual = subsampling.pausedContinuousTransformTypesState.value
        )

        withContext(Dispatchers.Main) {
            subsampling.setPausedContinuousTransformTypes(
                ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
            )
        }
        assertEquals(
            expected = ContinuousTransformType.GESTURE or ContinuousTransformType.FLING,
            actual = subsampling.pausedContinuousTransformTypesState.value
        )
    }

    @Test
    fun testDisabledBackgroundTiles() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(expected = false, actual = subsampling.disabledBackgroundTilesState.value)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledBackgroundTiles(true)
        }
        assertEquals(expected = true, actual = subsampling.disabledBackgroundTilesState.value)
    }

    @Test
    fun testStopped() = runTest {
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(5f, animated = false)
            }

            Thread.sleep(2000)
            delayUntil(2000) { subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(expected = true, actual = subsampling.readyState.value)
            assertEquals(expected = false, actual = subsampling.stoppedState.value)
            assertEquals(expected = 48, actual = subsampling.foregroundTilesState.value.size)
            assertEquals(
                expected = true,
                actual = subsampling.foregroundTilesState.value.any { it.state != TileState.STATE_NONE },
            )

            withContext(Dispatchers.Main) {
                subsampling.setStopped(true)
            }
            Thread.sleep(2000)
            delayUntil(2000) { !subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(expected = false, actual = subsampling.readyState.value)
            assertEquals(expected = true, actual = subsampling.stoppedState.value)
            assertEquals(expected = 48, actual = subsampling.foregroundTilesState.value.size)
            assertEquals(
                expected = true,
                actual = subsampling.foregroundTilesState.value.all { it.state == TileState.STATE_NONE },
            )
        }
    }

    @Test
    fun testDisabledAutoStopWithLifecycle() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        val lifecycle = TestLifecycle()
        withContext(Dispatchers.Main) {
            subsampling.onAttachToWindow()
            subsampling.setLifecycle(lifecycle)
        }

        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.RESUMED, actual = lifecycle.currentState)
        assertEquals(
            expected = false,
            actual = subsampling.disabledAutoStopWithLifecycleState.value
        )
        assertEquals(expected = false, actual = subsampling.stoppedState.value)


        withContext(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.CREATED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
        assertEquals(
            expected = false,
            actual = subsampling.disabledAutoStopWithLifecycleState.value
        )
        assertEquals(expected = true, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.STARTED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
        assertEquals(
            expected = false,
            actual = subsampling.disabledAutoStopWithLifecycleState.value
        )
        assertEquals(expected = false, actual = subsampling.stoppedState.value)

        // disabledAutoStopWithLifecycle is true, so it will not stop when the lifecycle is stopped
        withContext(Dispatchers.Main) {
            subsampling.setDisabledAutoStopWithLifecycle(true)
            lifecycle.currentState = Lifecycle.State.STARTED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
        assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycleState.value)
        assertEquals(expected = false, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.CREATED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
        assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycleState.value)
        assertEquals(expected = false, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            lifecycle.currentState = Lifecycle.State.STARTED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
        assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycleState.value)
        assertEquals(expected = false, actual = subsampling.stoppedState.value)


        withContext(Dispatchers.Main) {
            subsampling.setDisabledAutoStopWithLifecycle(false)
            lifecycle.currentState = Lifecycle.State.CREATED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
        assertEquals(
            expected = false,
            actual = subsampling.disabledAutoStopWithLifecycleState.value
        )
        assertEquals(expected = true, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledAutoStopWithLifecycle(true)
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
        assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycleState.value)
        assertEquals(expected = false, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledAutoStopWithLifecycle(true)
            subsampling.setStopped(true)
            lifecycle.currentState = Lifecycle.State.STARTED
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
        assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycleState.value)
        assertEquals(expected = true, actual = subsampling.stoppedState.value)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledAutoStopWithLifecycle(false)
        }
        Thread.sleep(100)
        assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
        assertEquals(
            expected = false,
            actual = subsampling.disabledAutoStopWithLifecycleState.value
        )
        assertEquals(expected = false, actual = subsampling.stoppedState.value)
    }

    @Test
    fun testShowTileBounds() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingEngine(zoomable)
        }
        assertEquals(expected = false, actual = subsampling.showTileBoundsState.value)

        withContext(Dispatchers.Main) {
            subsampling.setShowTileBounds(true)
        }
        assertEquals(expected = true, actual = subsampling.showTileBoundsState.value)
    }

    @Test
    fun testImageInfo() = runTest {
        // basic
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            Thread.sleep(500)
            assertEquals(expected = null, actual = subsampling.imageInfoState.value)
        }

        // setImage
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { !subsampling.readyState.value }
            assertEquals(expected = null, actual = subsampling.imageInfoState.value)
        }

        // setImage, containerSizeState.value, contentSizeState.value
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { subsampling.readyState.value }
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfoState.value.toString()
            )
        }
    }

    @Test
    fun testTileGridSizeMap() = runTest {
        // basic
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            Thread.sleep(500)
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
        }

        // setImage
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { !subsampling.readyState.value }
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
        }

        // setImage, containerSizeState.value, contentSizeState.value
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { subsampling.readyState.value }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
        }
    }

    @Test
    fun testReady() = runTest {
        // basic
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            Thread.sleep(500)
            delayUntil(1000) { !subsampling.readyState.value }
            assertEquals(expected = false, actual = subsampling.readyState.value)
        }

        // setImage
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test"), imageView)
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { !subsampling.readyState.value }
            assertEquals(expected = false, actual = subsampling.readyState.value)
        }

        // setImage, containerSizeState.value, contentSizeState.value
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(1000) { subsampling.readyState.value }
            assertEquals(expected = true, actual = subsampling.readyState.value)
        }

        // setImage, containerSizeState.value, contentSizeState.value, CREATED Lifecycle
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                subsampling.setLifecycle(TestLifecycle(Lifecycle.State.CREATED))
            }
            Thread.sleep(500)
            delayUntil(1000) { subsampling.readyState.value }
            assertEquals(expected = false, actual = subsampling.readyState.value)
        }
    }

    @Test
    fun testForegroundTiles() = runTest {
        // setImage, containerSizeState.value, contentSizeState.value, scale 5
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(5f, animated = false)
            }
            Thread.sleep(500)
            delayUntil(2000) { subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(expected = 48, actual = subsampling.foregroundTilesState.value.size)
        }

        // setImage, containerSizeState.value, contentSizeState.value, scale 20
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(20f, animated = false)
            }
            Thread.sleep(500)
            delayUntil(2000) { subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(expected = 144, actual = subsampling.foregroundTilesState.value.size)
        }
    }

    @Test
    fun testSampleSize() = runTest {
        // setImage, containerSizeState.value, contentSizeState.value, scale 10
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            delayUntil(2000) { subsampling.readyState.value }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 0, actual = subsampling.sampleSizeState.value)
        }

        // setImage, containerSizeState.value, contentSizeState.value, scale 5
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(5f, animated = false)
            }
            Thread.sleep(500)
            delayUntil(2000) { subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 2, actual = subsampling.sampleSizeState.value)
        }

        // setImage, containerSizeState.value, contentSizeState.value, scale 20
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(20f, animated = false)
            }
            Thread.sleep(500)
            delayUntil(2000) { subsampling.foregroundTilesState.value.isNotEmpty() }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSizeState.value)
        }
    }

    @Test
    fun testImageLoadRect() = runTest {
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            Thread.sleep(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 0, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(0, 0, 690, 12176)",
                actual = subsampling.imageLoadRectState.value.toString()
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(zoomable.mediumScaleState.value, animated = false)
            }
            Thread.sleep(500)
            Thread.sleep(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(0, 5703, 690, 6473)",
                actual = subsampling.imageLoadRectState.value.toString()
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(zoomable.maxScaleState.value, animated = false)
            }
            Thread.sleep(500)
            Thread.sleep(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRectState.value.toString()
            )
        }
    }

    @Test
    fun testClean() = runTest {
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(zoomable.maxScaleState.value, animated = false)
            }
            Thread.sleep(500)
            Thread.sleep(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 144, actual = subsampling.foregroundTilesState.value.size)
            assertEquals(expected = 1, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRectState.value.toString()
            )
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfoState.value.toString()
            )

            withContext(Dispatchers.Main) {
                subsampling.setImage(null as ImageSource?)
            }
            Thread.sleep(1000)
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 0, actual = subsampling.foregroundTilesState.value.size)
            assertEquals(expected = 0, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(0, 0, 0, 0)",
                actual = subsampling.imageLoadRectState.value.toString()
            )
            assertEquals(expected = "null", actual = subsampling.imageInfoState.value.toString())
        }
    }

    @Test
    fun testAttach() = runTest {
        var subsamplingHolder: SubsamplingEngine? = null
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
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
                .apply { subsamplingHolder = this }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(zoomable.maxScaleState.value, animated = false)
            }
            Thread.sleep(500)
            Thread.sleep(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 144, actual = subsampling.foregroundTilesState.value.size)
            assertEquals(expected = 1, actual = subsampling.sampleSizeState.value)
            assertEquals(
                expected = "IntRectCompat.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRectState.value.toString()
            )
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfoState.value.toString()
            )
        }

        Thread.sleep(500)
        val subsampling = subsamplingHolder!!
        assertEquals(expected = "{}", actual = subsampling.tileGridSizeMapState.value.toString())
        assertEquals(expected = 0, actual = subsampling.foregroundTilesState.value.size)
        assertEquals(expected = 0, actual = subsampling.sampleSizeState.value)
        assertEquals(
            expected = "IntRectCompat.fromLTRB(0, 0, 0, 0)",
            actual = subsampling.imageLoadRectState.value.toString()
        )
        assertEquals(expected = "null", actual = subsampling.imageInfoState.value.toString())
    }

    @Test
    fun testDisabled() = runTest {
        TestActivity::class.suspendLaunchActivityWithUse { scenario ->
            val activity = scenario.getActivitySync()
            val imageView = ImageView(activity).apply {
                withContext(Dispatchers.Main) {
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .addView(this@apply, ViewGroup.LayoutParams(516, 516))
                }
            }
            val zoomable = ZoomableEngine(Logger("Test", level = Logger.Level.Debug), imageView)
            withContext(Dispatchers.Main) {
                zoomable.setContainerSize(IntSizeCompat(516, 516))
                zoomable.setContentSize(IntSizeCompat(86, 1522))
            }
            val subsampling = withContext(Dispatchers.Main) {
                SubsamplingEngine(zoomable)
            }
            withContext(Dispatchers.Main) {
                subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            }
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                zoomable.scale(5f, animated = false)
            }

            Thread.sleep(2000)
            assertEquals(expected = false, actual = subsampling.disabledState.value)
            assertNotNull(subsampling.subsamplingImage)
            assertEquals(expected = 48, actual = subsampling.foregroundTilesState.value.size)

            withContext(Dispatchers.Main) {
                subsampling.setDisabled(true)
            }
            Thread.sleep(2000)
            assertEquals(expected = true, actual = subsampling.disabledState.value)
            assertNull(subsampling.subsamplingImage)
            assertEquals(expected = 0, actual = subsampling.foregroundTilesState.value.size)

            withContext(Dispatchers.Main) {
                subsampling.setDisabled(false)
            }
            Thread.sleep(2000)
            assertEquals(expected = false, actual = subsampling.disabledState.value)
            assertNotNull(subsampling.subsamplingImage)
            assertEquals(expected = 48, actual = subsampling.foregroundTilesState.value.size)
        }
    }

    // TODO test: backgroundTiles
}