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
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toHexString
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.subsampling.internal.createTileBitmapConvertor
import com.github.panpf.zoomimage.compose.subsampling.internal.defaultStoppedController
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.StoppedController
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapCache
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformType
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.createTileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.internal.decodeAndCreateTileDecoder
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Creates and remember a [SubsamplingState] that can be used to subsampling of the content.
 */
@Composable
fun rememberSubsamplingState(
    logger: Logger = rememberZoomImageLogger(),
    zoomableState: ZoomableState
): SubsamplingState {
    val defaultStopAutoController = defaultStoppedController()
    val subsamplingState = remember(logger, zoomableState) {
        SubsamplingState(logger, zoomableState).apply {
            stoppedController = defaultStopAutoController
        }
    }
    return subsamplingState
}

/**
 * A state object that can be used to subsampling of the content.
 */
@Stable
class SubsamplingState constructor(logger: Logger, private val zoomableState: ZoomableState) :
    RememberObserver {

    val logger: Logger = logger.newLogger(module = "SubsamplingState@${logger.toHexString()}")

    private var coroutineScope: CoroutineScope? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null
    private val tileBitmapCacheSpec = TileBitmapCacheSpec()
    private val tileBitmapReuseSpec = TileBitmapReuseSpec()
    private val tileBitmapCacheHelper = TileBitmapCacheHelper(this.logger, tileBitmapCacheSpec)
    private val tileBitmapReuseHelper =
        createTileBitmapReuseHelper(this.logger, tileBitmapReuseSpec)
    private val tileBitmapConvertor = createTileBitmapConvertor()
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private var preferredTileSize: IntSize by mutableStateOf(IntSize.Zero)
    private var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    private var rememberedCount = 0

    var imageKey: String? = null


    /* *********************************** Configurable properties ****************************** */

    /**
     * Set up the TileBitmap memory cache container
     */
    var tileBitmapCache: TileBitmapCache? by mutableStateOf(null)

    /**
     * If true, disabled TileBitmap memory cache
     */
    var disabledTileBitmapCache: Boolean by mutableStateOf(false)

    /**
     * Set up a shared TileBitmap pool for the tile
     */
    var tileBitmapPool: TileBitmapPool? by mutableStateOf(null)

    /**
     * If true, TileBitmap reuse is disabled
     */
    var disabledTileBitmapReuse: Boolean by mutableStateOf(false)

    /**
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec by mutableStateOf(TileAnimationSpec.Default)

    /**
     * A continuous transform type that needs to pause loading. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.ContinuousTransformType
     */
    var pausedContinuousTransformType: Int by mutableIntStateOf(DefaultPausedContinuousTransformType)

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
     * The stopped property controller, which can automatically stop and restart with the help of Lifecycle
     */
    var stoppedController: StoppedController? = null
        set(value) {
            if (field != value) {
                field?.onDestroy()
                field = value
                value?.bindStoppedWrapper(object : StoppedController.StoppedWrapper {
                    override var stopped: Boolean
                        get() = this@SubsamplingState.stopped
                        set(value) {
                            this@SubsamplingState.stopped = value
                            if (value) {
                                tileManager?.clean("stopped")
                            }
                            coroutineScope?.launch {
                                refreshTilesFlow.emit(if (value) "stopped" else "started")
                            }
                        }
                })
            }
        }

    /**
     * If true, the bounds of each tile is displayed
     */
    var showTileBounds: Boolean by mutableStateOf(false)


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, etc
     */
    var imageInfo: ImageInfo? by mutableStateOf(null)
        private set

    /**
     * Whether the image is ready for subsampling
     */
    var ready: Boolean by mutableStateOf(false)
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

    /**
     * Tile grid size map, key is sample size, value is tile grid size
     */
    var tileGridSizeMap: Map<Int, IntOffset> by mutableStateOf(emptyMap())
        private set


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${this.imageSource?.key}' -> '${imageSource?.key}'" }
        clean("setImageSource")
        this.imageSource = imageSource
        imageKey = imageSource?.key
        if (rememberedCount > 0) {
            resetTileDecoder("setImageSource")
        }
        return true
    }


    /* *************************************** Internal ***************************************** */

    override fun onRemembered() {
        // Since SubsamplingState is annotated with @Stable, onRemembered will be executed multiple times,
        // but we only need execute it once
        rememberedCount++
        if (rememberedCount != 1) return

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { preferredTileSize }.collect {
                resetTileManager("preferredTileSizeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { contentSize }.collect {
                resetTileDecoder("contentSizeChanged")
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { tileBitmapCache }.collect {
                tileBitmapCacheSpec.tileBitmapCache = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { disabledTileBitmapCache }.collect {
                tileBitmapCacheSpec.disabled = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { tileBitmapPool }.collect {
                tileBitmapReuseSpec.tileBitmapPool = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { disabledTileBitmapReuse }.collect {
                tileBitmapReuseSpec.disabled = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { pausedContinuousTransformType }.collect {
                tileManager?.pausedContinuousTransformType = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { disabledBackgroundTiles }.collect {
                tileManager?.disabledBackgroundTiles = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { tileAnimationSpec }.collect {
                tileManager?.tileAnimationSpec = it
            }
        }

        coroutineScope.launch {
            // Changes in containerSize cause a large chain reaction that can cause large memory fluctuations.
            // Size animations cause frequent changes in containerSize, so a delayed reset avoids this problem
            snapshotFlow { zoomableState.containerSize }.debounce(80).collect {
                val newTileSize = calculatePreferredTileSize(it.toCompat())
                if (preferredTileSize.isEmpty()) {
                    preferredTileSize = newTileSize.toPlatform()
                } else if (abs(newTileSize.width - preferredTileSize.width) >=
                    // When the width changes by more than 1x, the preferredTileSize is recalculated to reduce the need to reset the TileManager
                    preferredTileSize.width * (if (newTileSize.width > preferredTileSize.width) 1f else 0.5f)
                ) {
                    preferredTileSize = newTileSize.toPlatform()
                } else if (abs(newTileSize.height - preferredTileSize.height) >=
                    preferredTileSize.height * (if (newTileSize.height > preferredTileSize.height) 1f else 0.5f)
                ) {
                    // When the height changes by more than 1x, the preferredTileSize is recalculated to reduce the need to reset the TileManager
                    preferredTileSize = newTileSize.toPlatform()
                }
            }
        }
        coroutineScope.launch {
            snapshotFlow { zoomableState.contentSize }.collect {
                contentSize = it
            }
        }

        coroutineScope.launch {
            snapshotFlow { ready }.collect { ready ->
                val imageInfo = imageInfo
                val imageSize = if (ready && imageInfo != null)
                    imageInfo.size else IntSizeCompat.Zero
                zoomableState.contentOriginSize = imageSize.toPlatform()
            }
        }
        coroutineScope.launch {
            snapshotFlow { imageInfo }.collect { imageInfo ->
                val ready = ready
                val imageSize = if (ready && imageInfo != null)
                    imageInfo.size else IntSizeCompat.Zero
                zoomableState.contentOriginSize = imageSize.toPlatform()
            }
        }

        coroutineScope.launch {
            refreshTilesFlow.collect {
                refreshTiles(
                    contentVisibleRect = zoomableState.contentVisibleRect,
                    scale = zoomableState.transform.scaleX,
                    rotation = zoomableState.transform.rotation.roundToInt(),
                    continuousTransformType = zoomableState.continuousTransformType,
                    caller = it
                )
            }
        }
        coroutineScope.launch {
            snapshotFlow { zoomableState.transform }.collect {
                refreshTiles(
                    contentVisibleRect = zoomableState.contentVisibleRect,
                    scale = zoomableState.transform.scaleX,
                    rotation = zoomableState.transform.rotation.roundToInt(),
                    continuousTransformType = zoomableState.continuousTransformType,
                    caller = "transformChanged"
                )
            }
        }
        coroutineScope.launch {
            snapshotFlow { zoomableState.continuousTransformType }.collect {
                refreshTiles(
                    contentVisibleRect = zoomableState.contentVisibleRect,
                    scale = zoomableState.transform.scaleX,
                    rotation = zoomableState.transform.rotation.roundToInt(),
                    continuousTransformType = zoomableState.continuousTransformType,
                    caller = "continuousTransformTypeChanged"
                )
            }
        }
    }

    override fun onAbandoned() = onForgotten()
    override fun onForgotten() {
        // Since SubsamplingState is annotated with @Stable, onForgotten will be executed multiple times,
        // but we only need execute it once
        if (rememberedCount <= 0) return
        rememberedCount--
        if (rememberedCount != 0) return

        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            coroutineScope.cancel("onForgotten")
            this.coroutineScope = null
        }

        clean("onForgotten")
        stoppedController?.onDestroy()
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource
        val contentSize = contentSize
        if (imageSource == null || contentSize.isEmpty()) {
            logger.d {
                "resetTileDecoder:$caller. failed. " +
                        "imageSource=${imageSource}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "'${imageKey}'"
            }
            return
        }

        lastResetTileDecoderJob = coroutineScope?.launch {
            val result = withContext(Dispatchers.IO) {
                decodeAndCreateTileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    thumbnailSize = contentSize.toCompat(),
                    tileBitmapReuseHelper = tileBitmapReuseHelper,
                )
            }
            val newTileDecoder = result.getOrNull()
            if (newTileDecoder != null) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "imageInfo=${newTileDecoder.imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingState.tileDecoder = newTileDecoder
                this@SubsamplingState.imageInfo = newTileDecoder.imageInfo
                resetTileManager(caller)
            } else {
                val exception = result.exceptionOrNull()!! as CreateTileDecoderException
                this@SubsamplingState.imageInfo = exception.imageInfo
                val level = if (exception.skipped) Logger.DEBUG else Logger.ERROR
                val type = if (exception.skipped) "skipped" else "error"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, ${exception.message}. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "'${imageKey}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager("resetTileManager:$caller")

        val imageSource = imageSource
        val tileDecoder = tileDecoder
        val imageInfo = imageInfo
        val contentSize = contentSize
        val preferredTileSize = preferredTileSize
        if (imageSource == null || tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "resetTileManager:$caller. failed. " +
                        "imageSource=${imageSource}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileDecoder=${tileDecoder}, " +
                        "'${imageKey}'"
            }
            return
        }

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            tileBitmapConvertor = tileBitmapConvertor,
            imageSource = imageSource,
            contentSize = contentSize.toCompat(),
            preferredTileSize = preferredTileSize.toCompat(),
            tileBitmapCacheHelper = tileBitmapCacheHelper,
            tileBitmapReuseHelper = tileBitmapReuseHelper,
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
            pausedContinuousTransformType = this@SubsamplingState.pausedContinuousTransformType
            disabledBackgroundTiles = this@SubsamplingState.disabledBackgroundTiles
            tileAnimationSpec = this@SubsamplingState.tileAnimationSpec
        }
        tileGridSizeMap = tileManager.sortedTileGridMap.mapValues { entry ->
            entry.value.last().coordinate.let { IntOffset(it.x + 1, it.y + 1) }
        }
        logger.d {
            "resetTileManager:$caller. success. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${imageKey}'"
        }
        this@SubsamplingState.tileManager = tileManager
        refreshReadyState("resetTileManager:$caller")
    }

    private fun refreshTiles(
        contentVisibleRect: IntRect,
        scale: Float,
        rotation: Int,
        @ContinuousTransformType continuousTransformType: Int,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stopped) {
            logger.d { "refreshTiles:$caller. interrupted, stopped. '${imageKey}'" }
            return
        }
        tileManager.refreshTiles(
            scale = scale,
            contentVisibleRect = contentVisibleRect.toCompat(),
            rotation = rotation,
            continuousTransformType = continuousTransformType,
            caller = caller
        )
    }

    private fun refreshReadyState(caller: String) {
        val newReady = imageInfo != null && tileDecoder != null && tileManager != null
        logger.d { "refreshReadyState:$caller. ready=$newReady. '${imageKey}'" }
        ready = newReady
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
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
            refreshReadyState("cleanTileDecoder:$caller")
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingState.tileManager
        if (tileManager != null) {
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingState.tileManager = null
            tileGridSizeMap = emptyMap()
            foregroundTiles = emptyList()
            backgroundTiles = emptyList()
            sampleSize = 0
            imageLoadRect = IntRect.Zero
            logger.d { "cleanTileManager:$caller. '${imageKey}'" }
            refreshReadyState("cleanTileManager:$caller")
        }
    }

    private fun clean(@Suppress("SameParameterValue") caller: String) {
        cleanTileManager("destroy:$caller")
        cleanTileDecoder("destroy:$caller")
    }
}