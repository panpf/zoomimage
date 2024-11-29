package com.github.panpf.zoomimage.view.test.subsampling

import android.view.ViewGroup
import android.widget.ImageView
import androidx.test.platform.app.InstrumentationRegistry
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.tools4a.test.ktx.getActivitySync
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.test.TestActivity
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
import kotlin.test.assertSame

class SubsamplingEngineTest {

    @Test
    fun testConstructor() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertSame(expected = zoomable, actual = subsampling.zoomableEngine)
        assertSame(expected = zoomable.logger, actual = subsampling.logger)
        assertSame(expected = zoomable.view, actual = subsampling.view)
    }

    @Test
    fun testTileImageCache() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(expected = null, actual = subsampling.tileImageCacheState.value)

        val testTileImageCache = TestTileImageCache()
        subsampling.tileImageCacheState.value = testTileImageCache
        assertSame(expected = testTileImageCache, actual = subsampling.tileImageCacheState.value)
    }

    @Test
    fun testDisabledTileImageCache() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(expected = false, actual = subsampling.disabledTileImageCacheState.value)

        subsampling.disabledTileImageCacheState.value = true
        assertEquals(expected = true, actual = subsampling.disabledTileImageCacheState.value)
    }

    @Test
    fun testTileAnimationSpec() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(
            expected = TileAnimationSpec.Default,
            actual = subsampling.tileAnimationSpecState.value
        )

        subsampling.tileAnimationSpecState.value = TileAnimationSpec.None
        assertEquals(
            expected = TileAnimationSpec.None,
            actual = subsampling.tileAnimationSpecState.value
        )
    }

    @Test
    fun testPausedContinuousTransformTypes() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(
            expected = TileManager.DefaultPausedContinuousTransformTypes,
            actual = subsampling.pausedContinuousTransformTypesState.value
        )

        subsampling.pausedContinuousTransformTypesState.value =
            ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
        assertEquals(
            expected = ContinuousTransformType.GESTURE or ContinuousTransformType.FLING,
            actual = subsampling.pausedContinuousTransformTypesState.value
        )
    }

    @Test
    fun testDisabledBackgroundTiles() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(expected = false, actual = subsampling.disabledBackgroundTilesState.value)

        subsampling.disabledBackgroundTilesState.value = true
        assertEquals(expected = true, actual = subsampling.disabledBackgroundTilesState.value)
    }

    @Test
    fun testStopped() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(expected = false, actual = subsampling.stoppedState.value)

        subsampling.stoppedState.value = true
        assertEquals(expected = true, actual = subsampling.stoppedState.value)
    }

    @Test
    fun testShowTileBounds() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageView = ImageView(context)
        val zoomable = ZoomableEngine(Logger("Test"), imageView)
        val subsampling = SubsamplingEngine(zoomable)
        assertEquals(expected = false, actual = subsampling.showTileBoundsState.value)

        subsampling.showTileBoundsState.value = true
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
            val subsampling = SubsamplingEngine(zoomable)
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
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            val subsampling = SubsamplingEngine(zoomable)
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
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            val subsampling = SubsamplingEngine(zoomable)
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
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            Thread.sleep(500)
            delayUntil(1000) { subsampling.readyState.value }
            assertEquals(expected = true, actual = subsampling.readyState.value)
        }
    }

    @Test
    fun testForegroundTiles() = runTest {
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
            Thread.sleep(500)
            delayUntil(2000) { subsampling.readyState.value }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMapState.value.toString()
            )
            assertEquals(expected = 0, actual = subsampling.sampleSizeState.value)
        }

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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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

            subsampling.setImage(null as ImageSource?)
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
            zoomable.containerSizeState.value = IntSizeCompat(516, 516)
            zoomable.contentSizeState.value = IntSizeCompat(86, 1522)
            val subsampling = SubsamplingEngine(zoomable)
                .apply { subsamplingHolder = this }
            subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
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

    // TODO test: backgroundTiles
}