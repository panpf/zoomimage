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

package com.github.panpf.zoomimage.view.subsampling

import android.view.View
import com.github.panpf.zoomimage.subsampling.ExifOrientation
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
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.internal.toHexString
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Engines that control subsampling
 */
class SubsamplingEngine constructor(
    logger: Logger,
    private val zoomableEngine: ZoomableEngine,
    private val view: View,
) {

    val logger: Logger = logger.newLogger(module = "SubsamplingEngine@${logger.toHexString()}")

    private var coroutineScope: CoroutineScope? = null
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private val tileBitmapCacheSpec = TileBitmapCacheSpec()
    private val tileBitmapReuseSpec = TileBitmapReuseSpec()
    private var tileBitmapCacheHelper = TileBitmapCacheHelper(this.logger, tileBitmapCacheSpec)
    private var tileBitmapReuseHelper =
        createTileBitmapReuseHelper(this.logger, tileBitmapReuseSpec)
    private var lastResetTileDecoderJob: Job? = null
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private val preferredTileSizeState = MutableStateFlow(IntSizeCompat.Zero)
    private val contentSizeState = MutableStateFlow(IntSizeCompat.Zero)

    var imageKey: String? = null


    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    val ignoreExifOrientationState = MutableStateFlow(false)

    /**
     * Set up the TileBitmap memory cache container
     */
    val tileBitmapCacheState = MutableStateFlow<TileBitmapCache?>(null)

    /**
     * If true, disabled TileBitmap memory cache
     */
    val disabledTileBitmapCacheState = MutableStateFlow(false)

    /**
     * Set up a shared TileBitmap pool for the tile
     */
    val tileBitmapPoolState = MutableStateFlow<TileBitmapPool?>(null)

    /**
     * If true, TileBitmap reuse is disabled
     */
    val disabledTileBitmapReuseState = MutableStateFlow(false)

    /**
     * The animation spec for tile animation
     */
    val tileAnimationSpecState = MutableStateFlow(TileAnimationSpec.Default)

    /**
     * A continuous transform type that needs to pause loading
     */
    val pausedContinuousTransformTypeState = MutableStateFlow(DefaultPausedContinuousTransformType)

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    val disabledBackgroundTilesState = MutableStateFlow(false)

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    val stoppedState = MutableStateFlow(false)

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
                        get() = this@SubsamplingEngine.stoppedState.value
                        set(value) {
                            this@SubsamplingEngine.stoppedState.value = value
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
    val showTileBoundsState = MutableStateFlow(false)


    /* *********************************** Information properties ******************************* */

    private val _imageInfoState = MutableStateFlow<ImageInfo?>(null)
    private val _exifOrientation = MutableStateFlow<ExifOrientation?>(null)
    private val _readyState = MutableStateFlow(false)
    private val _foregroundTilesState = MutableStateFlow<List<TileSnapshot>>(emptyList())
    private val _backgroundTilesState = MutableStateFlow<List<TileSnapshot>>(emptyList())
    private val _sampleSizeState = MutableStateFlow(0)
    private val _imageLoadRectState = MutableStateFlow(IntRectCompat.Zero)
    private val _tileGridSizeMapState = MutableStateFlow<Map<Int, IntOffsetCompat>>(emptyMap())

    /**
     * The information of the image, including width, height, format, etc
     */
    val imageInfoState: StateFlow<ImageInfo?> = _imageInfoState

    /**
     * The exif information of the image
     */
    val exifOrientationState: StateFlow<ExifOrientation?> = _exifOrientation

    /**
     * Whether the image is ready for subsampling
     */
    val readyState: StateFlow<Boolean> = _readyState

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    val foregroundTilesState: StateFlow<List<TileSnapshot>> = _foregroundTilesState

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    val backgroundTilesState: StateFlow<List<TileSnapshot>> = _backgroundTilesState

    /**
     * The sample size of the image
     */
    val sampleSizeState: StateFlow<Int> = _sampleSizeState

    /**
     * The image load rect
     */
    val imageLoadRectState: StateFlow<IntRectCompat> = _imageLoadRectState

    /**
     * Tile grid size map, key is sample size, value is tile grid size
     */
    val tileGridSizeMapState: StateFlow<Map<Int, IntOffsetCompat>> = _tileGridSizeMapState


    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttachToWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDetachFromWindow()
            }
        })
        if (view.isAttachedToWindowCompat) {
            onAttachToWindow()
        }
    }

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
        if (view.isAttachedToWindowCompat) {
            resetTileDecoder("setImageSource")
        }
        return true
    }


    /* *************************************** Internal ***************************************** */

    private fun onAttachToWindow() {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope

        coroutineScope.launch {
            preferredTileSizeState.collect {
                resetTileManager("preferredTileSizeChanged")
            }
        }
        coroutineScope.launch {
            contentSizeState.collect {
                resetTileDecoder("contentSizeChanged")
            }
        }
        coroutineScope.launch {
            ignoreExifOrientationState.collect {
                resetTileDecoder("ignoreExifOrientationChanged")
            }
        }
        coroutineScope.launch {
            tileBitmapCacheState.collect {
                tileBitmapCacheSpec.tileBitmapCache = it
            }
        }
        coroutineScope.launch {
            disabledTileBitmapCacheState.collect {
                tileBitmapCacheSpec.disabled = it
            }
        }
        coroutineScope.launch {
            tileBitmapPoolState.collect {
                tileBitmapReuseSpec.tileBitmapPool = it
            }
        }
        coroutineScope.launch {
            disabledTileBitmapReuseState.collect {
                tileBitmapReuseSpec.disabled = it
            }
        }
        coroutineScope.launch {
            tileAnimationSpecState.collect {
                tileManager?.tileAnimationSpec = it
            }
        }
        coroutineScope.launch {
            pausedContinuousTransformTypeState.collect {
                tileManager?.pausedContinuousTransformType = it
            }
        }
        coroutineScope.launch {
            disabledBackgroundTilesState.collect {
                tileManager?.disabledBackgroundTiles = it
            }
        }

        coroutineScope.launch {
            // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
            // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
            zoomableEngine.containerSizeState.debounce(80).collect {
                val newTileSize = calculatePreferredTileSize(it)
                val preferredTileSize = preferredTileSizeState.value
                if (preferredTileSize.isEmpty()) {
                    preferredTileSizeState.value = newTileSize
                } else if (abs(newTileSize.width - preferredTileSize.width) >=
                    // When the width changes by more than 1x, the preferredTileSize is recalculated to reduce the need to reset the TileManager
                    preferredTileSizeState.value.width * (if (newTileSize.width > preferredTileSize.width) 1f else 0.5f)
                ) {
                    preferredTileSizeState.value = newTileSize
                } else if (abs(newTileSize.height - preferredTileSize.height) >=
                    preferredTileSize.height * (if (newTileSize.height > preferredTileSize.height) 1f else 0.5f)
                ) {
                    // When the height changes by more than 1x, the preferredTileSize is recalculated to reduce the need to reset the TileManager
                    preferredTileSizeState.value = newTileSize
                }
            }
        }
        coroutineScope.launch {
            zoomableEngine.contentSizeState.collect {
                contentSizeState.value = it
            }
        }

        coroutineScope.launch {
            readyState.collect { ready ->
                val imageInfo = imageInfoState.value
                val imageSize = if (ready && imageInfo != null)
                    imageInfo.size else IntSizeCompat.Zero
                zoomableEngine.contentOriginSizeState.value = imageSize
            }
        }
        coroutineScope.launch {
            imageInfoState.collect { imageInfo ->
                val ready = readyState.value
                val imageSize = if (ready && imageInfo != null)
                    imageInfo.size else IntSizeCompat.Zero
                zoomableEngine.contentOriginSizeState.value = imageSize
            }
        }

        coroutineScope.launch {
            refreshTilesFlow.collect {
                refreshTiles(
                    contentVisibleRect = zoomableEngine.contentVisibleRectState.value,
                    scale = zoomableEngine.transformState.value.scaleX,
                    rotation = zoomableEngine.transformState.value.rotation.roundToInt(),
                    continuousTransformType = zoomableEngine.continuousTransformTypeState.value,
                    caller = it
                )
            }
        }
        coroutineScope.launch {
            zoomableEngine.transformState.collect {
                refreshTiles(
                    contentVisibleRect = zoomableEngine.contentVisibleRectState.value,
                    scale = zoomableEngine.transformState.value.scaleX,
                    rotation = zoomableEngine.transformState.value.rotation.roundToInt(),
                    continuousTransformType = zoomableEngine.continuousTransformTypeState.value,
                    caller = "transformChanged"
                )
            }
        }
        coroutineScope.launch {
            zoomableEngine.continuousTransformTypeState.collect {
                refreshTiles(
                    contentVisibleRect = zoomableEngine.contentVisibleRectState.value,
                    scale = zoomableEngine.transformState.value.scaleX,
                    rotation = zoomableEngine.transformState.value.rotation.roundToInt(),
                    continuousTransformType = zoomableEngine.continuousTransformTypeState.value,
                    caller = "continuousTransformTypeChanged"
                )
            }
        }
    }

    private fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            coroutineScope.cancel("onDetachFromWindow")
            this.coroutineScope = null
        }

        clean("onViewDetachedFromWindow")
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource
        val contentSize = contentSizeState.value
        if (imageSource == null || contentSize.isEmpty()) {
            logger.d {
                "resetTileDecoder:$caller. failed. " +
                        "imageSource=${imageSource}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "'${imageKey}'"
            }
            return
        }

        val ignoreExifOrientation = ignoreExifOrientationState.value
        lastResetTileDecoderJob = coroutineScope?.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                decodeAndCreateTileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    thumbnailSize = contentSize,
                    ignoreExifOrientation = ignoreExifOrientation,
                    tileBitmapReuseHelper = tileBitmapReuseHelper,
                )
            }
            val newTileDecoder = result.getOrNull()
            if (newTileDecoder != null) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${newTileDecoder.imageInfo.toShortString()}. " +
                            "'${imageKey}'"
                }
                this@SubsamplingEngine.tileDecoder = newTileDecoder
                this@SubsamplingEngine._imageInfoState.value = newTileDecoder.imageInfo
                this@SubsamplingEngine._exifOrientation.value = newTileDecoder.exifOrientation
                resetTileManager(caller)
            } else {
                val exception = result.exceptionOrNull()!! as CreateTileDecoderException
                this@SubsamplingEngine._imageInfoState.value = exception.imageInfo
                val level = if (exception.skipped) Logger.DEBUG else Logger.ERROR
                val type = if (exception.skipped) "skipped" else "error"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, ${exception.message}. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo: ${exception.imageInfo?.toShortString()}. " +
                            "'${imageKey}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val imageSource = imageSource
        val tileDecoder = tileDecoder
        val imageInfo = imageInfoState.value
        val preferredTileSize = preferredTileSizeState.value
        val contentSize = contentSizeState.value
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
            tileBitmapConvertor = null,
            imageSource = imageSource,
            preferredTileSize = preferredTileSize,
            contentSize = contentSize,
            tileBitmapCacheHelper = tileBitmapCacheHelper,
            tileBitmapReuseHelper = tileBitmapReuseHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                _backgroundTilesState.value = manager.backgroundTiles
                _foregroundTilesState.value = manager.foregroundTiles
            },
            onSampleSizeChanged = { manager ->
                _sampleSizeState.value = manager.sampleSize
            },
            onImageLoadRectChanged = { manager ->
                _imageLoadRectState.value = manager.imageLoadRect
            }
        ).apply {
            pausedContinuousTransformType =
                this@SubsamplingEngine.pausedContinuousTransformTypeState.value
            disabledBackgroundTiles = this@SubsamplingEngine.disabledBackgroundTilesState.value
            tileAnimationSpec = this@SubsamplingEngine.tileAnimationSpecState.value
        }
        _tileGridSizeMapState.value = tileManager.sortedTileGridMap.mapValues { entry ->
            entry.value.last().coordinate.let { IntOffsetCompat(it.x + 1, it.y + 1) }
        }
        logger.d {
            "resetTileManager:$caller. success. " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${imageKey}'"
        }
        this@SubsamplingEngine.tileManager = tileManager
        refreshReadyState("resetTileManager:$caller")
    }

    private fun refreshTiles(
        contentVisibleRect: IntRectCompat,
        scale: Float,
        rotation: Int,
        @ContinuousTransformType continuousTransformType: Int,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stoppedState.value) {
            logger.d { "refreshTiles:$caller. interrupted, stopped. '${imageKey}'" }
            return
        }
        tileManager.refreshTiles(
            scale = scale,
            contentVisibleRect = contentVisibleRect,
            rotation = rotation,
            continuousTransformType = continuousTransformType,
            caller = caller
        )
    }

    private fun refreshReadyState(caller: String) {
        val newReady = imageInfoState.value != null && tileManager != null && tileDecoder != null
        logger.d { "refreshReadyState:$caller. ready=$newReady. '${imageKey}'" }
        _readyState.value = newReady
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val lastResetTileDecoderJob = this@SubsamplingEngine.lastResetTileDecoderJob
        if (lastResetTileDecoderJob != null) {
            lastResetTileDecoderJob.cancel("cleanTileDecoder:$caller")
            this@SubsamplingEngine.lastResetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingEngine.tileDecoder
        if (tileDecoder != null) {
            logger.d { "cleanTileDecoder:$caller. '${imageKey}'" }
            tileDecoder.destroy("cleanTileDecoder:$caller")
            this@SubsamplingEngine.tileDecoder = null
            refreshReadyState("cleanTileDecoder:$caller")
        }
        _imageInfoState.value = null
        _exifOrientation.value = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingEngine.tileManager
        if (tileManager != null) {
            logger.d { "cleanTileManager:$caller. '${imageKey}'" }
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingEngine.tileManager = null
            _tileGridSizeMapState.value = emptyMap()
            _foregroundTilesState.value = emptyList()
            _backgroundTilesState.value = emptyList()
            _sampleSizeState.value = 0
            _imageLoadRectState.value = IntRectCompat.Zero
            refreshReadyState("cleanTileManager:$caller")
        }
    }

    private fun clean(caller: String) {
        cleanTileDecoder("clean:$caller")
        cleanTileManager("clean:$caller")
    }
}