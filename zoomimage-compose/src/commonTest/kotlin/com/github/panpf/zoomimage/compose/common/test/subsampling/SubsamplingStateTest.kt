package com.github.panpf.zoomimage.compose.common.test.subsampling

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.util.isNotEmpty
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileState
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.test.Platform
import com.github.panpf.zoomimage.test.TestLifecycle
import com.github.panpf.zoomimage.test.TestTileImageCache
import com.github.panpf.zoomimage.test.current
import com.github.panpf.zoomimage.test.toImageSource
import com.github.panpf.zoomimage.test.waitMillis
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("OPT_IN_USAGE")
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
    fun testConstructor() = runTest {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling = withContext(Dispatchers.Main) {
            SubsamplingState(zoomable, testLifecycle)
        }
        assertSame(expected = zoomable, actual = subsampling.zoomableState)
        assertSame(expected = testLifecycle, actual = subsampling.lifecycle)
        @Suppress("USELESS_IS_CHECK")
        assertEquals(expected = true, actual = subsampling is RememberObserver)
    }

    @Test
    fun testLogger() = runTest {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertSame(expected = logger, actual = subsampling.logger)
    }

    @Test
    fun testTileImageCache() = runTest {
        val logger = Logger("Test")
        val zoomable = withContext(Dispatchers.Main) { ZoomableState(logger) }
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(expected = null, actual = subsampling.tileImageCache)

        val testTileImageCache = TestTileImageCache()
        withContext(Dispatchers.Main) {
            subsampling.setTileImageCache(testTileImageCache)
        }
        assertSame(expected = testTileImageCache, actual = subsampling.tileImageCache)
    }

    @Test
    fun testDisabledTileImageCache() = runTest {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(expected = false, actual = subsampling.disabledTileImageCache)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledTileImageCache(true)
        }
        assertEquals(expected = true, actual = subsampling.disabledTileImageCache)
    }

    @Test
    fun testTileAnimationSpec() = runTest {
        val logger = Logger("Test")
        val zoomable = withContext(Dispatchers.Main) { ZoomableState(logger) }
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(expected = TileAnimationSpec.Default, actual = subsampling.tileAnimationSpec)

        withContext(Dispatchers.Main) {
            subsampling.setTileAnimationSpec(TileAnimationSpec.None)
        }
        assertEquals(expected = TileAnimationSpec.None, actual = subsampling.tileAnimationSpec)
    }

    @Test
    fun testPausedContinuousTransformTypes() = runTest {
        val logger = Logger("Test")
        val zoomable = withContext(Dispatchers.Main) { ZoomableState(logger) }
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(
            expected = TileManager.DefaultPausedContinuousTransformTypes,
            actual = subsampling.pausedContinuousTransformTypes
        )

        withContext(Dispatchers.Main) {
            subsampling.setPausedContinuousTransformTypes(
                ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
            )
        }
        assertEquals(
            expected = ContinuousTransformType.GESTURE or ContinuousTransformType.FLING,
            actual = subsampling.pausedContinuousTransformTypes
        )
    }

    @Test
    fun testDisabledBackgroundTiles() = runTest {
        val logger = Logger("Test")
        val zoomable = ZoomableState(logger)
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(expected = false, actual = subsampling.disabledBackgroundTiles)

        withContext(Dispatchers.Main) {
            subsampling.setDisabledBackgroundTiles(true)
        }
        assertEquals(expected = true, actual = subsampling.disabledBackgroundTiles)
    }

    @Test
    fun testStopped() {
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(2000)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { subsampling.ready }
            assertEquals(expected = true, actual = subsampling.ready)
            assertEquals(expected = false, actual = subsampling.stopped)
            assertEquals(
                expected = 48,
                actual = subsampling.foregroundTiles.size,
            )
            assertEquals(
                expected = true,
                actual = subsampling.foregroundTiles.any { it.state != TileState.STATE_NONE },
            )

            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setStopped(true)
            }
            waitMillis(2000)
            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
            assertEquals(expected = false, actual = subsampling.ready)
            assertEquals(expected = true, actual = subsampling.stopped)
            assertEquals(
                expected = 48,
                actual = subsampling.foregroundTiles.size,
            )
            assertEquals(
                expected = true,
                actual = subsampling.foregroundTiles.all { it.state == TileState.STATE_NONE },
            )
        }
    }

    @Test
    fun testDisabledAutoStopWithLifecycle() {
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            var lifecycleHolder: TestLifecycle? = null
            setContent {
                TestLifecycle {
                    lifecycleHolder = LocalLifecycleOwner.current.lifecycle as TestLifecycle?
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            val lifecycle = lifecycleHolder!!
            assertEquals(expected = Lifecycle.State.RESUMED, actual = lifecycle.currentState)
            assertEquals(expected = false, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)


            GlobalScope.launch(Dispatchers.Main) {
                lifecycle.currentState = Lifecycle.State.CREATED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
            assertEquals(expected = false, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = true, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                lifecycle.currentState = Lifecycle.State.STARTED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
            assertEquals(expected = false, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)

            // disabledAutoStopWithLifecycle is true, so it will not stop when the lifecycle is stopped
            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setDisabledAutoStopWithLifecycle(true)
                lifecycle.currentState = Lifecycle.State.STARTED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
            assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                lifecycle.currentState = Lifecycle.State.CREATED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
            assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                lifecycle.currentState = Lifecycle.State.STARTED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
            assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)


            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setDisabledAutoStopWithLifecycle(false)
                lifecycle.currentState = Lifecycle.State.CREATED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
            assertEquals(expected = false, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = true, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setDisabledAutoStopWithLifecycle(true)
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.CREATED, actual = lifecycle.currentState)
            assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setDisabledAutoStopWithLifecycle(true)
                subsampling.setStopped(true)
                lifecycle.currentState = Lifecycle.State.STARTED
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
            assertEquals(expected = true, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = true, actual = subsampling.stopped)

            GlobalScope.launch(Dispatchers.Main) {
                subsampling.setDisabledAutoStopWithLifecycle(false)
            }
            waitMillis(100)
            assertEquals(expected = Lifecycle.State.STARTED, actual = lifecycle.currentState)
            assertEquals(expected = false, actual = subsampling.disabledAutoStopWithLifecycle)
            assertEquals(expected = false, actual = subsampling.stopped)
        }
    }

    @Test
    fun testShowTileBounds() = runTest {
        val logger = Logger("Test")
        val zoomable = withContext(Dispatchers.Main) { ZoomableState(logger) }
        val testLifecycle = TestLifecycle()
        val subsampling =
            withContext(Dispatchers.Main) { SubsamplingState(zoomable, testLifecycle) }
        assertEquals(expected = false, actual = subsampling.showTileBounds)

        withContext(Dispatchers.Main) {
            subsampling.setShowTileBounds(true)
        }
        assertEquals(expected = true, actual = subsampling.showTileBounds)
    }

    @Test
    fun testImageInfo() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
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
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            assertEquals(expected = null, actual = subsampling.imageInfo)
        }

        // setImage
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
            assertEquals(expected = null, actual = subsampling.imageInfo)
        }

        // setImage, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { subsampling.ready }
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfo.toString()
            )
        }
    }

    @Test
    fun testTileGridSizeMap() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
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
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }

        // setImage
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
            assertEquals(
                expected = "{}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }

        // setImage, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { subsampling.ready }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
        }
    }

    @Test
    fun testReady() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
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
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
            assertEquals(expected = false, actual = subsampling.ready)
        }

        // setImage
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
            assertEquals(expected = false, actual = subsampling.ready)
        }

        // setImage, containerSize, contentSize
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 1000) { subsampling.ready }
            assertEquals(expected = true, actual = subsampling.ready)
        }

        // setImage, containerSize, contentSize, CREATED Lifecycle
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle(Lifecycle.State.CREATED) {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(1000)
            val subsampling = subsamplingHolder!!
            assertEquals(expected = false, actual = subsampling.ready)
        }
    }

    @Test
    fun testForegroundTiles() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
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
//            waitMillis(100)
//            val subsampling = subsamplingHolder!!
//            waitUntil(timeoutMillis = 1000) { !subsampling.ready }
//            assertEquals(expected = emptyList(), actual = subsampling.foregroundTiles)
//        }
//
//        // setImage, containerSize, contentSize
//        runComposeUiTest {
//            var subsamplingHolder: SubsamplingState? = null
//            setContent {
//                TestLifecycle {
//                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
//                    val zoomable = rememberZoomableState(logger)
//                    LaunchedEffect(Unit) {
//                        zoomable.setContainerSize(IntSize(516, 516))
//                        zoomable.setContentSize(IntSize(86, 1522))
//                    }
//                    val subsampling = rememberSubsamplingState(zoomable)
//                        .apply { subsamplingHolder = this }
//                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
//                }
//            }
//            waitMillis(100)
//            val subsampling = subsamplingHolder!!
//            waitUntil(timeoutMillis = 1000) { subsampling.ready }
//            assertEquals(expected = emptyList(), actual = subsampling.foregroundTiles)
//        }

        // setImage, containerSize, contentSize, scale 5
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = 48, actual = subsampling.foregroundTiles.size)
        }

        // setImage, containerSize, contentSize, scale 20
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(20f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
        }
    }

    @Test
    fun testSampleSize() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        // setImage, containerSize, contentSize, scale 10
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 2000) { subsampling.ready }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 0, actual = subsampling.sampleSize)
        }

        // setImage, containerSize, contentSize, scale 5
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 2, actual = subsampling.sampleSize)
        }

        // setImage, containerSize, contentSize, scale 20
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(20f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitUntil(timeoutMillis = 2000) { subsampling.foregroundTiles.isNotEmpty() }
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSize)
        }
    }

    @Test
    fun testImageLoadRect() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 0, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 690, 12176)",
                actual = subsampling.imageLoadRect.toString()
            )
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.mediumScale, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(0, 5703, 690, 6473)",
                actual = subsampling.imageLoadRect.toString()
            )
        }

        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRect.toString()
            )
        }
    }

    @Test
    fun testClean() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRect.toString()
            )
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfo.toString()
            )

            GlobalScope.launch(Dispatchers.Main.immediate) {
                subsampling.setImage(null as ImageSource?)
            }
            waitMillis(1000)
            assertEquals(expected = "{}", actual = subsampling.tileGridSizeMap.toString())
            assertEquals(expected = 0, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 0, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(0, 0, 0, 0)",
                actual = subsampling.imageLoadRect.toString()
            )
            assertEquals(expected = "null", actual = subsampling.imageInfo.toString())
        }
    }

    @Test
    fun testRememberObserver() {
        if (Platform.current == Platform.iOS) {
            // Files in kotlin resources cannot be accessed in ios test environment.
            return
        }

        var subsamplingHolder: SubsamplingState? = null
        runComposeUiTest {
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    zoomable.setContainerSize(IntSize(516, 516))
                    zoomable.setContentSize(IntSize(86, 1522))
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(zoomable.maxScale, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(100)
            val subsampling = subsamplingHolder!!
            waitMillis(1000)
            assertEquals(
                expected = "{8=(1, 6), 4=(1, 12), 2=(2, 24), 1=(3, 48)}",
                actual = subsampling.tileGridSizeMap.toString()
            )
            assertEquals(expected = 144, actual = subsampling.foregroundTiles.size)
            assertEquals(expected = 1, actual = subsampling.sampleSize)
            assertEquals(
                expected = "IntRect.fromLTRB(127, 5871, 563, 6305)",
                actual = subsampling.imageLoadRect.toString()
            )
            assertEquals(
                expected = "ImageInfo(size=690x12176, mimeType='image/jpeg')",
                actual = subsampling.imageInfo.toString()
            )
        }

        val subsampling = subsamplingHolder!!
        assertEquals(expected = "{}", actual = subsampling.tileGridSizeMap.toString())
        assertEquals(expected = 0, actual = subsampling.foregroundTiles.size)
        assertEquals(expected = 0, actual = subsampling.sampleSize)
        assertEquals(
            expected = "IntRect.fromLTRB(0, 0, 0, 0)",
            actual = subsampling.imageLoadRect.toString()
        )
        assertEquals(expected = "null", actual = subsampling.imageInfo.toString())
    }

    @Test
    fun testDisabled() {
        runComposeUiTest {
            var subsamplingHolder: SubsamplingState? = null
            setContent {
                TestLifecycle {
                    val logger = rememberZoomImageLogger(level = Logger.Level.Debug)
                    val zoomable = rememberZoomableState(logger)
                    LaunchedEffect(Unit) {
                        zoomable.setContainerSize(IntSize(516, 516))
                        zoomable.setContentSize(IntSize(86, 1522))
                    }
                    val subsampling = rememberSubsamplingState(zoomable)
                        .apply { subsamplingHolder = this }
                    subsampling.setImage(ResourceImages.hugeLongComic.toImageSource())
                    LaunchedEffect(Unit) {
                        snapshotFlow { zoomable.contentOriginSize }.collect {
                            if (it.isNotEmpty()) {
                                zoomable.scale(5f, animated = false)
                            }
                        }
                    }
                }
            }
            waitMillis(2000)
            val subsampling = subsamplingHolder!!
            assertEquals(expected = false, actual = subsampling.disabled)
            assertNotNull(subsampling.subsamplingImage)
            assertEquals(expected = 48, actual = subsampling.foregroundTiles.size)

            GlobalScope.launch(Dispatchers.Main.immediate) {
                subsampling.setDisabled(true)
            }
            waitMillis(2000)
            assertEquals(expected = true, actual = subsampling.disabled)
            assertNull(subsampling.subsamplingImage)
            assertEquals(expected = 0, actual = subsampling.foregroundTiles.size)

            GlobalScope.launch(Dispatchers.Main.immediate) {
                subsampling.setDisabled(false)
            }
            waitMillis(2000)
            assertEquals(expected = false, actual = subsampling.disabled)
            assertNotNull(subsampling.subsamplingImage)
            assertEquals(expected = 48, actual = subsampling.foregroundTiles.size)
        }
    }

    // TODO test: backgroundTiles
}