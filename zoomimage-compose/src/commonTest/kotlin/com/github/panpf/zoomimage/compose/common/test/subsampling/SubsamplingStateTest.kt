package com.github.panpf.zoomimage.compose.common.test.subsampling

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.util.isNotEmpty
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.TestImageSource
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.TestTileBitmapCache
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.waitMillis
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class SubsamplingStateTest {

    @Test
    fun testRememberSubsamplingState() = runComposeUiTest {
        setContent {
            TestLifecycle {
                val zoomableState = rememberZoomableState()
                val subsamplingState = rememberSubsamplingState(zoomableState)
                assertSame(
                    expected = zoomableState,
                    actual = subsamplingState.zoomableState
                )
                assertSame(
                    expected = LocalLifecycleOwner.current.lifecycle,
                    actual = subsamplingState.lifecycle
                )
            }
        }
    }

    @Test
    fun testConstructor() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertSame(expected = zoomable, actual = subsampling.zoomableState)
        assertSame(expected = testLifecycle, actual = subsampling.lifecycle)
        @Suppress("USELESS_IS_CHECK")
        assertEquals(expected = true, actual = subsampling is RememberObserver)
    }

    @Test
    fun testLogger() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertSame(expected = logger, actual = subsampling.logger)
    }

    @Test
    fun testImageKey() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = null, actual = subsampling.imageKey)

        subsampling.setImageSource(TestImageSource())
        assertEquals(expected = "TestImageSource", actual = subsampling.imageKey)

        subsampling.setImageSource(TestImageSource("TestImageSource2"))
        assertEquals(expected = "TestImageSource2", actual = subsampling.imageKey)

        subsampling.setImageSource(null as TestImageSource?)
        assertEquals(expected = null, actual = subsampling.imageKey)
    }

    @Test
    fun testTileBitmapCache() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = null, actual = subsampling.tileBitmapCache)

        val testTileBitmapCache = TestTileBitmapCache()
        subsampling.tileBitmapCache = testTileBitmapCache
        assertSame(expected = testTileBitmapCache, actual = subsampling.tileBitmapCache)
    }

    @Test
    fun testDisabledTileBitmapCache() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = false, actual = subsampling.disabledTileBitmapCache)

        subsampling.disabledTileBitmapCache = true
        assertEquals(expected = true, actual = subsampling.disabledTileBitmapCache)
    }

    @Test
    fun testTileAnimationSpec() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = TileAnimationSpec.Default, actual = subsampling.tileAnimationSpec)

        subsampling.tileAnimationSpec = TileAnimationSpec.None
        assertEquals(expected = TileAnimationSpec.None, actual = subsampling.tileAnimationSpec)
    }

    @Test
    fun testPausedContinuousTransformTypes() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = TileManager.DefaultPausedContinuousTransformTypes, actual = subsampling.pausedContinuousTransformTypes)

        subsampling.pausedContinuousTransformTypes = ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
        assertEquals(expected = ContinuousTransformType.GESTURE or ContinuousTransformType.FLING, actual = subsampling.pausedContinuousTransformTypes)
    }

    @Test
    fun testDisabledBackgroundTiles() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = false, actual = subsampling.disabledBackgroundTiles)

        subsampling.disabledBackgroundTiles = true
        assertEquals(expected = true, actual = subsampling.disabledBackgroundTiles)
    }

    @Test
    fun testStopped() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = false, actual = subsampling.stopped)

        subsampling.stopped = true
        assertEquals(expected = true, actual = subsampling.stopped)
    }

    @Test
    fun testShowTileBounds() {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = SubsamplingState(zoomable, testLifecycle)
        assertEquals(expected = false, actual = subsampling.showTileBounds)

        subsampling.showTileBounds = true
        assertEquals(expected = true, actual = subsampling.showTileBounds)
    }

    @Test
    fun testImageInfo() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        // basic
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val zoomable = rememberZoomableState()
                    rememberSubsamplingState(zoomable).apply { subsamplingHolder = this }
                }
            }
            val subsampling = subsamplingHolder!!
            assertEquals(expected = null, actual = subsampling.imageInfo)
        }

        // setImageSource
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { !subsampling.ready }
            assertEquals(expected = null, actual = subsampling.imageInfo)
        }

        // setImageSource, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.containerSize = IntSize(516, 516)
                        zoomable.contentSize = IntSize(86, 1522)
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { subsampling.ready }
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfo.toString()
            )
        }
    }

    @Test
    fun testTileGridSizeMap() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        // basic
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val zoomable = rememberZoomableState()
                    rememberSubsamplingState(zoomable).apply { subsamplingHolder = this }
                }
            }
            val subsampling = subsamplingHolder!!
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }

        // setImageSource
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { !subsampling.ready }
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }

        // setImageSource, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.containerSize = IntSize(516, 516)
                        zoomable.contentSize = IntSize(86, 1522)
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { subsampling.ready }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    fun testReady() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        // basic
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val zoomable = rememberZoomableState()
                    rememberSubsamplingState(zoomable).apply { subsamplingHolder = this }
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { !subsampling.ready }
            assertEquals(expected = false, actual = subsampling.ready)
        }

        // setImageSource
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { !subsampling.ready }
            assertEquals(expected = false, actual = subsampling.ready)
        }

        // setImageSource, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.containerSize = IntSize(516, 516)
                        zoomable.contentSize = IntSize(86, 1522)
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(1000) { subsampling.ready }
            assertEquals(expected = true, actual = subsampling.ready)
        }
    }

    @Test
    fun testForegroundTiles() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
//        // basic
//        runComposeUiTest {
//            var subsamplingHolder: SubsamplingState? = null
//            setContent {
//                TestLifecycle {
//                    val zoomable = rememberZoomableState()
//                    rememberSubsamplingState(zoomable).apply { subsamplingHolder = this }
//                }
//            }
//            val subsampling = subsamplingHolder!!
//            waitUntil(1000) { !subsampling.ready }
//            assertEquals(expected = emptyList(), actual = subsampling.foregroundTiles)
//        }
//
//        // setImageSource, containerSize, contentSize
//        runComposeUiTest {
//            var subsamplingHolder: SubsamplingState? = null
//            setContent {
//                TestLifecycle {
//                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
//                    val zoomable = rememberZoomableState(logger)
//                    LaunchedEffect(Unit) {
//                        zoomable.containerSize = IntSize(516, 516)
//                        zoomable.contentSize = IntSize(86, 1522)
//                    }
//                    val subsampling = rememberSubsamplingState(zoomable)
//                        .apply { subsamplingHolder = this }
//                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
//                }
//            }
//            val subsampling = subsamplingHolder!!
//            waitUntil(1000) { subsampling.ready }
//            assertEquals(expected = emptyList(), actual = subsampling.foregroundTiles)
//        }

        // setImageSource, containerSize, contentSize, scale 10
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = 48, actual = subsampling.foregroundTiles.size)
        }

        // setImageSource, containerSize, contentSize, scale 20
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(20f, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
        }
    }

    @Test
    fun testBackgroundTiles() {
        // TODO test
    }

    @Test
    fun testSampleSize() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        // setImageSource, containerSize, contentSize, scale 10
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(2000) { subsampling.ready }
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 0, actual = subsampling.sampleSize)
        }

        // setImageSource, containerSize, contentSize, scale 10
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 2, actual = subsampling.sampleSize)
        }

        // setImageSource, containerSize, contentSize, scale 20
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(20f, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitUntil(2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 1, actual = subsampling.sampleSize)
        }
    }

    @Test
    fun testImageLoadRect() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 0, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(0, 0, 690, 12176)", actual = subsampling.imageLoadRect.toString())
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.mediumScale, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(0, 5703, 690, 6473)", actual = subsampling.imageLoadRect.toString())
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(127, 5871, 563, 6305)", actual = subsampling.imageLoadRect.toString())
        }
    }

    @Test
    fun testClean() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(127, 5871, 563, 6305)", actual = subsampling.imageLoadRect.toString())
            assertEquals(expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')", actual = subsampling.imageInfo.toString())

            subsampling.setImageSource(null as ImageSource?)
            waitMillis(1000)
            assertEquals(expected = "{}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 0, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 0, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(0, 0, 0, 0)", actual = subsampling.imageLoadRect.toString())
            assertEquals(expected = "null", actual = subsampling.imageInfo.toString())
        }
    }

    @Test
    fun testRememberObserver() {
        if (Platform.current == Platform.iOS) {
            // TODO Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        var subsamplingHolder: SubsamplingState? = null
        runComposeUiTest {
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.containerSize = IntSize(516, 516)
                    zoomable.contentSize = IntSize(86, 1522)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImageSource(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(expected = "IntRect.fromLTRB(127, 5871, 563, 6305)", actual = subsampling.imageLoadRect.toString())
            assertEquals(expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')", actual = subsampling.imageInfo.toString())
        }

        val subsampling = subsamplingHolder!!
        assertEquals(expected = "{}", actual = subsampling.tileGridSizeMap.toString())
        assertEquals(expected = 0, actual = subsampling.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsampling.sampleSize)
        assertEquals(expected = "IntRect.fromLTRB(0, 0, 0, 0)", actual = subsampling.imageLoadRect.toString())
        assertEquals(expected = "null", actual = subsampling.imageInfo.toString())
    }
}