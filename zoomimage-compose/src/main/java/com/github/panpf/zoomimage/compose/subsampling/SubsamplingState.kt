/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.readImageInfo
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * Creates and remember a [SubsamplingState] that can be used to subsampling of the content.
 */
@Composable
fun rememberSubsamplingState(logger: Logger = rememberZoomImageLogger()): SubsamplingState {
    val subsamplingState = remember(logger) {
        SubsamplingState(logger)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        subsamplingState.setLifecycle(lifecycle)
    }
    return subsamplingState
}

/**
 * A state object that can be used to subsampling of the content.
 */
@Stable
class SubsamplingState(logger: Logger) : RememberObserver {

    private val logger: Logger = logger.newLogger(module = "SubsamplingState")
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null
    private val tileMemoryCacheHelper = TileMemoryCacheHelper(this.logger)
    private val tileBitmapPoolHelper = TileBitmapPoolHelper(this.logger)
    private var lifecycle: Lifecycle? = null
    private val resetStoppedLifecycleObserver by lazy { ResetStoppedLifecycleObserver(this) }
    internal var imageKey: String? = null

    internal var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    internal var contentSize: IntSize by mutableStateOf(IntSize.Zero)


    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    var ignoreExifOrientation: Boolean  by mutableStateOf(false)

    /**
     * Set up the tile memory cache container
     */
    var tileMemoryCache: TileMemoryCache? by mutableStateOf(null)

    /**
     * If true, disable memory cache
     */
    var disableMemoryCache: Boolean by mutableStateOf(false)

    /**
     * Set up a shared Bitmap pool for the tile
     */
    var tileBitmapPool: TileBitmapPool? by mutableStateOf(null)

    /**
     * If true, Bitmap reuse is disabled
     */
    var disallowReuseBitmap: Boolean by mutableStateOf(false)

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec by mutableStateOf(TileAnimationSpec.Default)

    /**
     * Whether to pause loading tiles when transforming, which improves performance,
     * but delays the loading of tiles, allowing users to perceive the loading process more,
     * and the user experience will be reduced
     */
    var pauseWhenTransforming: Boolean  by mutableStateOf(false)

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    var disabledBackgroundTiles: Boolean by mutableStateOf(false)

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    var stopped by mutableStateOf(false)

    /**
     * If true, the bounds of each tile is displayed
     */
    var showTileBounds: Boolean by mutableStateOf(false)


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, exif information, etc
     */
    var imageInfo: ImageInfo? by mutableStateOf(null)
        private set

    /**
     * Whether the image is ready for subsampling
     */
    var ready: Boolean  by mutableStateOf(false)
        private set

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    var foregroundTiles: List<TileSnapshot> by mutableStateOf(emptyList())
        private set

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    var backgroundTiles: List<TileSnapshot> by mutableStateOf(emptyList())
        private set

    /**
     * The sample size of the image
     */
    var sampleSize: Int by mutableIntStateOf(0)
        private set

    /**
     * The image load rect
     */
    var imageLoadRect: IntRect by mutableStateOf(IntRect.Zero)
        private set


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${this.imageSource?.key}' -> '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        imageKey = imageSource?.key
        resetTileDecoder("setImageSource")
        return true
    }

    /**
     * Set the lifecycle, which automatically controls stop and start, which is obtained from [LocalLifecycleOwner] by default,
     * and can be set by this method if the default acquisition method is not applicable
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetStopped("setLifecycle")
        }
    }


    /* *************************************** Internal ***************************************** */

    @Composable
    internal fun BindZoomableState(zoomableState: ZoomableState) {
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.containerSize }.collect {
                // Changes in containerSize cause a large chain reaction that can cause large memory fluctuations.
                // Size animations cause frequent changes in containerSize, so a delayed reset avoids this problem
                if (it.isNotEmpty()) {
                    delay(60)
                }
                containerSize = it
                resetTileManager("containerSizeChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.contentSize }.collect {
                contentSize = it
                resetTileDecoder("contentSizeChanged")
            }
        }

        val refreshTiles: (caller: String) -> Unit = { caller ->
            refreshTiles(
                contentVisibleRect = zoomableState.contentVisibleRect,
                scale = zoomableState.transform.scaleX,
                rotation = zoomableState.transform.rotation.roundToInt(),
                transforming = zoomableState.transforming,
                caller = caller
            )
        }
        LaunchedEffect(Unit) {
            snapshotFlow { ready }.collect {
                val imageInfo = imageInfo
                zoomableState.contentOriginSize = if (it && imageInfo != null) {
                    IntSize(imageInfo.width, imageInfo.height)
                } else {
                    IntSize.Zero
                }
                refreshTiles("readyChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.transform }.collect {
                refreshTiles("transformChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.transforming }.collect {
                refreshTiles("transformingChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { stopped }.collect {
                refreshTiles(if (it) "stopped" else "started")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { ignoreExifOrientation }.collect {
                resetTileDecoder("ignoreExifOrientationChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { tileMemoryCache }.collect {
                tileMemoryCacheHelper.tileMemoryCache = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { disableMemoryCache }.collect {
                tileMemoryCacheHelper.disableMemoryCache = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { tileBitmapPool }.collect {
                tileBitmapPoolHelper.tileBitmapPool = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { disallowReuseBitmap }.collect {
                tileBitmapPoolHelper.disallowReuseBitmap = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { pauseWhenTransforming }.collect {
                tileManager?.pauseWhenTransforming = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { disabledBackgroundTiles }.collect {
                tileManager?.disabledBackgroundTiles = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { tileAnimationSpec }.collect {
                tileManager?.tileAnimationSpec = it
            }
        }
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val ignoreExifOrientation = ignoreExifOrientation

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val imageInfoResult = imageSource.readImageInfo(ignoreExifOrientation)
            val imageInfo = imageInfoResult.getOrNull()
            this@SubsamplingState.imageInfo = imageInfo
            val canUseSubsamplingResult =
                imageInfo?.let { canUseSubsampling(it, contentSize.toCompat()) }
            if (imageInfo != null && canUseSubsamplingResult == 0) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingState.tileDecoder = TileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    tileBitmapPoolHelper = tileBitmapPoolHelper,
                    imageInfo = imageInfo,
                )
                resetTileManager(caller)
            } else {
                val cause = when {
                    imageInfo == null -> imageInfoResult.exceptionOrNull()!!.message
                    canUseSubsamplingResult == -1 -> "The content size is greater than or equal to the original image"
                    canUseSubsamplingResult == -2 -> "The content aspect ratio is different with the original image"
                    canUseSubsamplingResult == -3 -> "Image type not support subsampling"
                    else -> "Unknown canUseSubsamplingResult: $canUseSubsamplingResult"
                }
                val level = if (canUseSubsamplingResult == -1) Logger.DEBUG else Logger.ERROR
                val type = if (canUseSubsamplingResult == -1) "skipped" else "failed"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, $cause. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
                            "'${imageKey}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager("resetTileManager:$caller")

        val imageSource = imageSource ?: return
        val tileDecoder = tileDecoder ?: return
        val imageInfo = imageInfo ?: return
        val containerSize = containerSize.takeIf { !it.isEmpty() } ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            tileMemoryCacheHelper = tileMemoryCacheHelper,
            tileBitmapPoolHelper = tileBitmapPoolHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                backgroundTiles = manager.backgroundTiles
                foregroundTiles = manager.foregroundTiles
            },
            onSampleSizeChanged = { manager ->
                sampleSize = manager.sampleSize
            },
            onImageLoadRectChanged = {
                imageLoadRect = it.imageLoadRect.toPlatform()
            }
        ).apply {
            pauseWhenTransforming = this@SubsamplingState.pauseWhenTransforming
            disabledBackgroundTiles = this@SubsamplingState.disabledBackgroundTiles
            tileAnimationSpec = this@SubsamplingState.tileAnimationSpec
        }
        logger.d {
            val tileMaxSize = tileManager.tileMaxSize
            val tileMapInfoList = tileManager.sortedTileMap.entries.map { entry ->
                val tableSize = entry.value.last().coordinate.let { IntSizeCompat(it.width + 1, it.height + 1) }
                "${entry.key}:${entry.value.size}:${tableSize.toShortString()}"
            }
            "resetTileManager:$caller. success. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileMaxSize=${tileMaxSize.toShortString()}, " +
                    "tileMap=$tileMapInfoList, " +
                    "'${imageKey}'"
        }
        this@SubsamplingState.tileManager = tileManager
        refreshReadyState()
    }

    private fun refreshTiles(
        contentVisibleRect: IntRect,
        scale: Float,
        rotation: Int,
        transforming: Boolean,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stopped) {
            logger.d { "refreshTiles:$caller. interrupted, stopped. '${imageKey}'" }
            tileManager.clean("refreshTiles:stopped")
            return
        }
        tileManager.refreshTiles(
            contentVisibleRect = contentVisibleRect.toCompat(),
            scale = scale,
            rotation = rotation,
            transforming = transforming,
            caller = caller
        )
    }

    override fun onRemembered() {
    }

    override fun onForgotten() {
        destroy("onForgotten")
    }

    override fun onAbandoned() {
        destroy("onAbandoned")
    }

    private fun refreshReadyState() {
        ready = imageInfo != null && tileDecoder != null && tileManager != null
    }

    private fun cleanTileDecoder(caller: String) {
        val lastResetTileDecoderJob = this@SubsamplingState.lastResetTileDecoderJob
        if (lastResetTileDecoderJob != null) {
            lastResetTileDecoderJob.cancel("cleanTileDecoder:$caller")
            this@SubsamplingState.lastResetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingState.tileDecoder
        if (tileDecoder != null) {
            tileDecoder.destroy("cleanTileDecoder:$caller")
            this@SubsamplingState.tileDecoder = null
            logger.d { "cleanTileDecoder:$caller. '${imageKey}'" }
            refreshReadyState()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingState.tileManager
        if (tileManager != null) {
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingState.tileManager = null
            logger.d { "cleanTileManager:$caller. '${imageKey}'" }
            refreshReadyState()
        }
    }

    private fun destroy(caller: String) {
        cleanTileManager("destroy:$caller")
        cleanTileDecoder("destroy:$caller")
        unregisterLifecycleObserver()
    }

    private fun resetStopped(caller: String) {
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val stopped = !lifecycleStarted
        logger.d {
            "resetStopped:$caller. $stopped. lifecycleStarted=$lifecycleStarted. '${imageKey}'"
        }
        this.stopped = stopped
    }

    private fun registerLifecycleObserver() {
        lifecycle?.addObserver(resetStoppedLifecycleObserver)
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(resetStoppedLifecycleObserver)
    }

    private class ResetStoppedLifecycleObserver(
        val subsamplingState: SubsamplingState
    ) : LifecycleEventObserver {

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == ON_START) {
                subsamplingState.resetStopped("LifecycleStateChanged:ON_START")
            } else if (event == ON_STOP) {
                subsamplingState.resetStopped("LifecycleStateChanged:ON_STOP")
            }
        }
    }
}