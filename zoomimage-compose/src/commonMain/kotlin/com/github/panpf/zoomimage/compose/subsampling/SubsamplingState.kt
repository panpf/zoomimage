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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapCache
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapConvertor
import com.github.panpf.zoomimage.subsampling.internal.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.decodeAndCreateTileDecoder
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val subsamplingState = remember(logger, zoomableState, lifecycle) {
        SubsamplingState(logger, zoomableState, lifecycle)
    }
    return subsamplingState
}

/**
 * A state object that can be used to subsampling of the content.
 */
@Stable
class SubsamplingState constructor(
    val logger: Logger,
    val zoomableState: ZoomableState,
    val lifecycle: Lifecycle
) : RememberObserver {

    private var coroutineScope: CoroutineScope? = null
    private var imageSourceFactory: ImageSource.Factory? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null
    private val tileBitmapCacheSpec = TileBitmapCacheSpec()
    private val tileBitmapCacheHelper = TileBitmapCacheHelper(tileBitmapCacheSpec)
    private val tileBitmapConvertor = createTileBitmapConvertor()
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private var preferredTileSize: IntSize by mutableStateOf(IntSize.Zero)
    private var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    private var rememberedCount = 0
    private val stoppedLifecycleObserver = LifecycleEventObserver { _, _ ->
        val stopped = !lifecycle.currentState.isAtLeast(STARTED)
        this@SubsamplingState.stopped = stopped
        if (stopped) {
            tileManager?.clean("stopped")
        }
        coroutineScope?.launch {
            refreshTilesFlow.emit(if (stopped) "stopped" else "started")
        }
    }


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
     * The animation spec for tile animation
     */
    var tileAnimationSpec: TileAnimationSpec by mutableStateOf(TileAnimationSpec.Default)

    /**
     * A continuous transform type that needs to pause loading. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.ContinuousTransformType
     */
    var pausedContinuousTransformTypes: Int by mutableIntStateOf(
        DefaultPausedContinuousTransformTypes
    )

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
    fun setImageSource(imageSource: ImageSource.Factory?): Boolean {
        if (this.imageSourceFactory == imageSource) return false
        logger.d { "SubsamplingState. setImageSource. '${this.imageSourceFactory?.key}' -> '${imageSource?.key}'" }
        clean("setImageSource")
        this.imageSourceFactory = imageSource
        imageKey = imageSource?.key
        if (rememberedCount > 0) {
            resetTileDecoder("setImageSource")
        }
        return true
    }

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?): Boolean {
        return setImageSource(imageSource?.let { ImageSource.WrapperFactory(it) })
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
            snapshotFlow { pausedContinuousTransformTypes }.collect {
                tileManager?.pausedContinuousTransformTypes = it
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

        lifecycle.addObserver(stoppedLifecycleObserver)
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
        lifecycle.removeObserver(stoppedLifecycleObserver)
    }

    private fun resetTileDecoder(caller: String) {
        // TODO Unexpectedly executed twice in a row
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSourceFactory = imageSourceFactory
        val contentSize = contentSize
        if (imageSourceFactory == null || contentSize.isEmpty()) {
            logger.d {
                "SubsamplingState. resetTileDecoder:$caller. failed. " +
                        "imageSource=${imageSourceFactory}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "'${imageKey}'"
            }
            return
        }

        lastResetTileDecoderJob = coroutineScope?.launch {
            val result = withContext(ioCoroutineDispatcher()) {
                val imageSource = imageSourceFactory.create()
                decodeAndCreateTileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    thumbnailSize = contentSize.toCompat(),
                )
            }
            val newTileDecoder = result.getOrNull()
            if (newTileDecoder != null) {
                val imageInfo = newTileDecoder.imageInfo
                logger.d {
                    "SubsamplingState. resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingState.tileDecoder = newTileDecoder
                this@SubsamplingState.imageInfo = imageInfo
                resetTileManager(caller)
            } else {
                val exception = result.exceptionOrNull()!! as CreateTileDecoderException
                this@SubsamplingState.imageInfo = exception.imageInfo
                val level = if (exception.skipped) Logger.Level.Debug else Logger.Level.Error
                val type = if (exception.skipped) "skipped" else "error"
                logger.log(level) {
                    "SubsamplingState. resetTileDecoder:$caller. $type, ${exception.message}. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "'${imageKey}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager("resetTileManager:$caller")

        val tileDecoder = tileDecoder
        val imageInfo = imageInfo
        val contentSize = contentSize
        val preferredTileSize = preferredTileSize
        if (tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "SubsamplingState. resetTileManager:$caller. failed. " +
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
            contentSize = contentSize.toCompat(),
            preferredTileSize = preferredTileSize.toCompat(),
            tileBitmapCacheHelper = tileBitmapCacheHelper,
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
            pausedContinuousTransformTypes = this@SubsamplingState.pausedContinuousTransformTypes
            disabledBackgroundTiles = this@SubsamplingState.disabledBackgroundTiles
            tileAnimationSpec = this@SubsamplingState.tileAnimationSpec
        }
        tileGridSizeMap = tileManager.sortedTileGridMap.associate { entry ->
            entry.sampleSize to entry.tiles.last().coordinate.let { IntOffset(it.x + 1, it.y + 1) }
        }
        logger.d {
            "SubsamplingState. resetTileManager:$caller. success. " +
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
            logger.d { "SubsamplingState. refreshTiles:$caller. interrupted, stopped. '${imageKey}'" }
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
        logger.d { "SubsamplingState. refreshReadyState:$caller. ready=$newReady. '${imageKey}'" }
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
            logger.d { "SubsamplingState. cleanTileDecoder:$caller. '${imageKey}'" }
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(ioCoroutineDispatcher()) {
                tileDecoder.close()
            }
            this@SubsamplingState.tileDecoder = null
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
            logger.d { "SubsamplingState. cleanTileManager:$caller. '${imageKey}'" }
            refreshReadyState("cleanTileManager:$caller")
        }
    }

    private fun clean(@Suppress("SameParameterValue") caller: String) {
        cleanTileManager("destroy:$caller")
        cleanTileDecoder("destroy:$caller")
    }
}

expect fun createTileBitmapConvertor(): TileBitmapConvertor?