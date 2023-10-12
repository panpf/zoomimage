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
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.createTileBitmapReuseHelper
import com.github.panpf.zoomimage.subsampling.CreateTileDecoderException
import com.github.panpf.zoomimage.subsampling.ExifOrientation
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.StoppedController
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapCache
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheHelper
import com.github.panpf.zoomimage.subsampling.TileBitmapCacheSpec
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileBitmapReuseSpec
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileManager.Companion.DefaultPausedContinuousTransformType
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.decodeAndCreateTileDecoder
import com.github.panpf.zoomimage.subsampling.toIntroString
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.internal.isAttachedToWindowCompat
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Engines that control subsampling
 */
class SubsamplingEngine constructor(
    logger: Logger,
    private val view: View,
) {

    private val logger: Logger = logger.newLogger(module = "SubsamplingEngine")
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private val tileBitmapCacheSpec = TileBitmapCacheSpec()
    private val tileBitmapReuseSpec = TileBitmapReuseSpec()
    private var tileBitmapCacheHelper = TileBitmapCacheHelper(this.logger, tileBitmapCacheSpec)
    private var tileBitmapReuseHelper =
        createTileBitmapReuseHelper(this.logger, tileBitmapReuseSpec)
    private var lastResetTileDecoderJob: Job? = null
    private val _containerSizeState = MutableStateFlow(IntSizeCompat.Zero)
    private val _contentSizeState = MutableStateFlow(IntSizeCompat.Zero)
    internal var imageKey: String? = null
        private set

    /**
     * The size of the container that holds the content, this is usually the size of the [ZoomImageView] component
     */
    val containerSizeState: StateFlow<IntSizeCompat> = _containerSizeState

    /**
     * The size of the content, this is usually the size of the thumbnail Drawable, setup by the [ZoomImageView] component
     */
    val contentSizeState: StateFlow<IntSizeCompat> = _contentSizeState


    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    val ignoreExifOrientationState = MutableStateFlow(false)

    /**
     * Set up the tile memory cache container
     */
    // todo rename
    val tileMemoryCacheState = MutableStateFlow<TileBitmapCache?>(null)

    /**
     * If true, disable memory cache
     */
    // todo rename
    val disableMemoryCacheState = MutableStateFlow(false)

    /**
     * Set up a shared Bitmap pool for the tile
     */
    val tileBitmapPoolState = MutableStateFlow<TileBitmapPool?>(null)

    /**
     * If true, Bitmap reuse is disabled
     */
    // todo rename
    val disallowReuseBitmapState = MutableStateFlow(false)

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
    val exifOrientation: StateFlow<ExifOrientation?> = _exifOrientation

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
                reset("onViewAttachedToWindow")
            }

            override fun onViewDetachedFromWindow(v: View) {
                clean("onViewDetachedFromWindow")
            }
        })

        coroutineScope.launch {
            containerSizeState.collect {
                resetTileManager("containerSizeChanged")
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
            tileMemoryCacheState.collect {
                tileBitmapCacheSpec.tileBitmapCache = it
            }
        }
        coroutineScope.launch {
            disableMemoryCacheState.collect {
                tileBitmapCacheSpec.disabled = it
            }
        }
        coroutineScope.launch {
            tileBitmapPoolState.collect {
                tileBitmapReuseSpec.tileBitmapPool = it
            }
        }
        coroutineScope.launch {
            disallowReuseBitmapState.collect {
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
    }

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
        if (view.isAttachedToWindowCompat) {
            reset("setImageSource")
        }
        return true
    }


    /* *************************************** Internal ***************************************** */

    internal fun bindZoomEngine(zoomableEngine: ZoomableEngine) {
        coroutineScope.launch {
            // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
            // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
            zoomableEngine.containerSizeState.debounce(80).collect {
                _containerSizeState.value = it
            }
        }
        coroutineScope.launch {
            zoomableEngine.contentSizeState.collect {
                _contentSizeState.value = it
            }
        }

        val refreshTiles: (caller: String) -> Unit = { caller ->
            val transform = zoomableEngine.transformState.value
            refreshTiles(
                contentVisibleRect = zoomableEngine.contentVisibleRectState.value,
                scale = transform.scaleX,
                rotation = transform.rotation.roundToInt(),
                continuousTransformType = zoomableEngine.continuousTransformTypeState.value,
                caller = caller
            )
        }

        coroutineScope.launch {
            zoomableEngine.transformState.collect {
                refreshTiles("transformChanged")
            }
        }
        coroutineScope.launch {
            zoomableEngine.continuousTransformTypeState.collect {
                refreshTiles("continuousTransformTypeChanged")
            }
        }

        coroutineScope.launch {
            readyState.collect {
                val imageInfo = imageInfoState.value
                zoomableEngine.contentOriginSizeState.value = if (it && imageInfo != null) {
                    imageInfo.size
                } else {
                    IntSizeCompat.Zero
                }

                refreshTiles("readyChanged")
            }
        }
        coroutineScope.launch {
            stoppedState.collect {
                refreshTiles(if (it) "stopped" else "started")
            }
        }
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
            tileManager.clean("refreshTiles:stopped")
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

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSizeState.value.takeIf { !it.isEmpty() } ?: return
        val ignoreExifOrientation = ignoreExifOrientationState.value

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
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

        val imageSource = imageSource ?: return
        val tileDecoder = tileDecoder ?: return
        val imageInfo = imageInfoState.value ?: return
        val containerSize = containerSizeState.value.takeIf { !it.isEmpty() } ?: return
        val contentSize = contentSizeState.value.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            containerSize = containerSize,
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
                    "containerSize=${containerSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${tileManager.preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${imageKey}'"
        }
        this@SubsamplingEngine.tileManager = tileManager
        refreshReadyState()
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
            refreshReadyState()
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
            refreshReadyState()
        }
    }

    private fun refreshReadyState() {
        _readyState.value =
            imageInfoState.value != null && tileManager != null && tileDecoder != null
    }

    private fun reset(caller: String) {
        clean("reset:$caller")
        resetTileDecoder("destroy:$caller")
    }

    private fun clean(caller: String) {
        cleanTileDecoder("clean:$caller")
        cleanTileManager("clean:$caller")
    }
}