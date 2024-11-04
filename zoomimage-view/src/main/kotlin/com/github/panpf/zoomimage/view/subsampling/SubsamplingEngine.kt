/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.github.panpf.zoomimage.subsampling.TileImageCacheSpec
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.TileDecoder
import com.github.panpf.zoomimage.subsampling.internal.TileImageCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.subsampling.internal.calculatePreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.checkNewPreferredTileSize
import com.github.panpf.zoomimage.subsampling.internal.createTileDecoder
import com.github.panpf.zoomimage.subsampling.internal.toIntroString
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Engines that control subsampling
 *
 * @see com.github.panpf.zoomimage.view.test.subsampling.SubsamplingEngineTest
 */
class SubsamplingEngine(val zoomableEngine: ZoomableEngine) {

    private var coroutineScope: CoroutineScope? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private val tileImageCacheSpec = TileImageCacheSpec()
    private var tileImageCacheHelper = TileImageCacheHelper(tileImageCacheSpec)
    private var resetTileDecoderJob: Job? = null
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private val preferredTileSizeState = MutableStateFlow(IntSizeCompat.Zero)
    private val contentSizeState = MutableStateFlow(IntSizeCompat.Zero)
    private val stoppedLifecycleObserver = LifecycleEventObserver { _, _ ->
        val lifecycle = lifecycle ?: return@LifecycleEventObserver
        val stopped = !lifecycle.currentState.isAtLeast(STARTED)
        this@SubsamplingEngine.stoppedState.value = stopped
        if (stopped) {
            tileManager?.clean("stopped")
        }
        coroutineScope?.launch {
            refreshTilesFlow.emit(if (stopped) "stopped" else "started")
        }
    }

    val logger: Logger = zoomableEngine.logger
    val view: View = zoomableEngine.view

    var subsamplingImage: SubsamplingImage? = null
        private set


    /* *********************************** Configurable properties ****************************** */

    /**
     * Set up the TileImage memory cache container
     */
    val tileImageCacheState = MutableStateFlow<TileImageCache?>(null)

    /**
     * If true, disabled TileImage memory cache
     */
    val disabledTileImageCacheState = MutableStateFlow(false)

    /**
     * The animation spec for tile animation
     */
    val tileAnimationSpecState = MutableStateFlow(TileAnimationSpec.Default)

    /**
     * A continuous transform type that needs to pause loading
     */
    val pausedContinuousTransformTypesState =
        MutableStateFlow(DefaultPausedContinuousTransformTypes)

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
    var lifecycle: Lifecycle? = null
        set(value) {
            if (field != value) {
                field?.removeObserver(stoppedLifecycleObserver)
                field = value
                if (view.isAttachedToWindow) {
                    value?.addObserver(stoppedLifecycleObserver)
                }
            }
        }

    /**
     * If true, the bounds of each tile is displayed
     */
    val showTileBoundsState = MutableStateFlow(false)

    /**
     * User-defined RegionDecoder
     */
    var regionDecodersState = MutableStateFlow<List<RegionDecoder.Factory>>(emptyList())


    /* *********************************** Information properties ******************************* */

    private val _imageInfoState = MutableStateFlow<ImageInfo?>(null)
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
     * Tile grid size map, key is sample size, value is tile grid size
     */
    val tileGridSizeMapState: StateFlow<Map<Int, IntOffsetCompat>> = _tileGridSizeMapState

    /**
     * Whether the image is ready for subsampling
     */
    val readyState: StateFlow<Boolean> = _readyState

    /**
     * The sample size of the image
     */
    val sampleSizeState: StateFlow<Int> = _sampleSizeState

    /**
     * The image load rect
     */
    val imageLoadRectState: StateFlow<IntRectCompat> = _imageLoadRectState

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    val foregroundTilesState: StateFlow<List<TileSnapshot>> = _foregroundTilesState

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    val backgroundTilesState: StateFlow<List<TileSnapshot>> = _backgroundTilesState


    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttachToWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDetachFromWindow()
            }
        })
        if (view.isAttachedToWindow) {
            onAttachToWindow()
        }
    }

    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set subsampling image
     */
    fun setImage(subsamplingImage: SubsamplingImage?): Boolean {
        if (this.subsamplingImage == subsamplingImage) return false
        logger.d { "SubsamplingEngine. setImage. '${this.subsamplingImage}' -> '${subsamplingImage}'" }
        clean("setImage")
        this.subsamplingImage = subsamplingImage
        if (view.isAttachedToWindow) {
            resetTileDecoder("setImage")
        }
        return true
    }

    /**
     * Set subsampling image
     */
    fun setImage(imageSource: ImageSource.Factory?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let { SubsamplingImage(it, imageInfo) })
    }

    /**
     * Set subsampling image
     */
    fun setImage(imageSource: ImageSource?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let {
            SubsamplingImage(ImageSource.WrapperFactory(it), imageInfo)
        })
    }

    /**
     * Set up an image source from which image tile are loaded
     */
    @Deprecated(
        message = "Use setImage(ImageSource.Factory?, ImageInfo?) instead",
        replaceWith = ReplaceWith("setImage(imageSource)"),
        level = DeprecationLevel.WARNING
    )
    fun setImageSource(imageSource: ImageSource.Factory?): Boolean {
        return setImage(imageSource)
    }

    /**
     * Set up an image source from which image tile are loaded
     */
    @Deprecated(
        message = "Use setImage(ImageSource?, ImageInfo?) instead",
        replaceWith = ReplaceWith("setImage(imageSource)"),
        level = DeprecationLevel.WARNING
    )
    fun setImageSource(imageSource: ImageSource?): Boolean {
        return setImage(imageSource)
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
            tileImageCacheState.collect {
                tileImageCacheSpec.tileImageCache = it
            }
        }
        coroutineScope.launch {
            disabledTileImageCacheState.collect {
                tileImageCacheSpec.disabled = it
            }
        }
        coroutineScope.launch {
            tileAnimationSpecState.collect {
                tileManager?.tileAnimationSpec = it
            }
        }
        coroutineScope.launch {
            pausedContinuousTransformTypesState.collect {
                tileManager?.pausedContinuousTransformTypes = it
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
            @Suppress("OPT_IN_USAGE")
            zoomableEngine.containerSizeState.debounce(80).collect {
                val oldPreferredTileSize = preferredTileSizeState.value
                val newPreferredTileSize = calculatePreferredTileSize(it)
                val checkPassed = checkNewPreferredTileSize(
                    oldPreferredTileSize = oldPreferredTileSize,
                    newPreferredTileSize = newPreferredTileSize
                )
                logger.d {
                    "SubsamplingEngine. reset preferredTileSize. " +
                            "oldPreferredTileSize=$oldPreferredTileSize, " +
                            "newPreferredTileSize=$newPreferredTileSize, " +
                            "checkPassed=$checkPassed. " +
                            "'${subsamplingImage?.key}'"
                }
                if (checkPassed) {
                    preferredTileSizeState.value = newPreferredTileSize
                }
            }
        }
        coroutineScope.launch {
            zoomableEngine.contentSizeState.collect {
                contentSizeState.value = it
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

        lifecycle?.addObserver(stoppedLifecycleObserver)
    }

    private fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            coroutineScope.cancel("onDetachFromWindow")
            this.coroutineScope = null
        }

        clean("onViewDetachedFromWindow")
        lifecycle?.removeObserver(stoppedLifecycleObserver)
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val subsamplingImage = subsamplingImage
        val contentSize = contentSizeState.value
        val coroutineScope = coroutineScope
        if (subsamplingImage == null || contentSize.isEmpty() || coroutineScope == null) {
            logger.d {
                "SubsamplingEngine. resetTileDecoder:$caller. skipped. " +
                        "parameters are not ready yet. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "coroutineScope=$coroutineScope"
            }
            return
        }

        resetTileDecoderJob = coroutineScope.launch {
            val tileDecoderResult = createTileDecoder(
                logger = logger,
                subsamplingImage = subsamplingImage,
                contentSize = contentSize,
                regionDecoders = regionDecodersState.value,
                onImageInfoPassed = {
                    zoomableEngine.contentOriginSizeState.value = it.size
                }
            )
            if (tileDecoderResult.isFailure) {
                logger.d {
                    "SubsamplingEngine. resetTileDecoder:$caller. failed. " +
                            "${tileDecoderResult.exceptionOrNull()!!.message}. " +
                            "'${subsamplingImage.key}'"
                }
                zoomableEngine.contentOriginSizeState.value = IntSizeCompat.Zero
                return@launch
            }

            val tileDecoder = tileDecoderResult.getOrThrow()
            val imageInfo = subsamplingImage.imageInfo ?: tileDecoder.imageInfo
            this@SubsamplingEngine._imageInfoState.value = imageInfo
            this@SubsamplingEngine.tileDecoder = tileDecoder
            logger.d {
                "SubsamplingEngine. resetTileDecoder:$caller. success. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "imageInfo=${imageInfo.toShortString()}. " +
                        "'${subsamplingImage.key}'"
            }

            resetTileManager(caller)
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val subsamplingImage = subsamplingImage
        val tileDecoder = tileDecoder
        val imageInfo = imageInfoState.value
        val preferredTileSize = preferredTileSizeState.value
        val contentSize = contentSizeState.value
        if (subsamplingImage == null || tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "SubsamplingEngine. resetTileManager:$caller. failed. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileDecoder=${tileDecoder}, " +
                        "'${subsamplingImage?.key}'"
            }
            return
        }

        val tileManager = TileManager(
            logger = logger,
            subsamplingImage = subsamplingImage,
            tileDecoder = tileDecoder,
            tileImageConvertor = null,
            preferredTileSize = preferredTileSize,
            contentSize = contentSize,
            tileImageCacheHelper = tileImageCacheHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                if (this@SubsamplingEngine.tileManager == manager) {
                    _backgroundTilesState.value = manager.backgroundTiles
                    _foregroundTilesState.value = manager.foregroundTiles
                }
            },
            onSampleSizeChanged = { manager ->
                if (this@SubsamplingEngine.tileManager == manager) {
                    _sampleSizeState.value = manager.sampleSize
                }
            },
            onImageLoadRectChanged = { manager ->
                if (this@SubsamplingEngine.tileManager == manager) {
                    _imageLoadRectState.value = manager.imageLoadRect
                }
            }
        )
        tileManager.pausedContinuousTransformTypes =
            this@SubsamplingEngine.pausedContinuousTransformTypesState.value
        tileManager.disabledBackgroundTiles =
            this@SubsamplingEngine.disabledBackgroundTilesState.value
        tileManager.tileAnimationSpec = this@SubsamplingEngine.tileAnimationSpecState.value

        _tileGridSizeMapState.value = tileManager.sortedTileGridMap.associate { entry ->
            entry.sampleSize to entry.tiles.last().coordinate
                .let { IntOffsetCompat(it.x + 1, it.y + 1) }
        }
        logger.d {
            "SubsamplingEngine. resetTileManager:$caller. success. " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${subsamplingImage.key}'"
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
            logger.d { "SubsamplingEngine. refreshTiles:$caller. interrupted, stopped. '${subsamplingImage?.key}'" }
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
        logger.d { "SubsamplingEngine. refreshReadyState:$caller. ready=$newReady. '${subsamplingImage?.key}'" }
        _readyState.value = newReady
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val resetTileDecoderJob1 = this@SubsamplingEngine.resetTileDecoderJob
        if (resetTileDecoderJob1 != null && resetTileDecoderJob1.isActive) {
            resetTileDecoderJob1.cancel("cleanTileDecoder:$caller")
            this@SubsamplingEngine.resetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingEngine.tileDecoder
        if (tileDecoder != null) {
            logger.d { "SubsamplingEngine. cleanTileDecoder:$caller. '${subsamplingImage?.key}'" }
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(ioCoroutineDispatcher()) {
                tileDecoder.close()
            }
            this@SubsamplingEngine.tileDecoder = null
            refreshReadyState("cleanTileDecoder:$caller")
        }
        _imageInfoState.value = null
        zoomableEngine.contentOriginSizeState.value = IntSizeCompat.Zero
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingEngine.tileManager
        if (tileManager != null) {
            logger.d { "SubsamplingEngine. cleanTileManager:$caller. '${subsamplingImage?.key}'" }
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