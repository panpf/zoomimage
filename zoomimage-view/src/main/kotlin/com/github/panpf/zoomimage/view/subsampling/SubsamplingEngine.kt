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
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.SubsamplingCore
import com.github.panpf.zoomimage.subsampling.internal.ZoomableBridge
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.TransformCompat
import com.github.panpf.zoomimage.view.zoom.ZoomableEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Engines that control subsampling
 *
 * @see com.github.panpf.zoomimage.view.test.subsampling.SubsamplingEngineTest
 */
class SubsamplingEngine(val zoomableEngine: ZoomableEngine) {

    private var coroutineScope: CoroutineScope? = null
    private val subsamplingCore: SubsamplingCore = SubsamplingCore(
        module = "SubsamplingEngine",
        logger = zoomableEngine.logger,
        tileImageConvertor = null,
        zoomableBridge = object : ZoomableBridge {
            override val contentVisibleRect: RectCompat
                get() = zoomableEngine.contentVisibleRectFState.value

            override val transform: TransformCompat
                get() = zoomableEngine.transformState.value

            override val continuousTransformType: Int
                get() = zoomableEngine.continuousTransformTypeState.value

            override val transformFlow: Flow<TransformCompat>
                get() = zoomableEngine.transformState

            override val continuousTransformTypeFlow: Flow<Int>
                get() = zoomableEngine.continuousTransformTypeState

            override val containerSizeFlow: Flow<IntSizeCompat>
                get() = zoomableEngine.containerSizeState

            override val contentSizeFlow: Flow<IntSizeCompat>
                get() = zoomableEngine.contentSizeState

            override fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
                zoomableEngine.setContentOriginSize(contentOriginSize)
            }
        },
        onReadyChanged = {
            _readyState.value = it.ready
            _imageInfoState.value = it.imageInfo
            _tileGridSizeMapState.value = it.tileGridSizeMap
            _stoppedState.value = it.stopped
        },
        onTileChanged = {
            _backgroundTilesState.value = it.backgroundTiles
            _foregroundTilesState.value = it.foregroundTiles
            _sampleSizeState.value = it.sampleSize
            _imageLoadRectState.value = it.imageLoadRect
        }
    )

    val view: View = zoomableEngine.view
    val logger: Logger = zoomableEngine.logger
    val subsamplingImage: SubsamplingImage?
        get() = subsamplingCore.subsamplingImage


    /* *********************************** Configurable properties ****************************** */

    private val _disabledState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabled)
    private val _tileImageCacheState: MutableStateFlow<TileImageCache?> =
        MutableStateFlow(subsamplingCore.tileImageCache)
    private val _disabledTileImageCacheState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabledTileImageCache)
    private val _tileAnimationSpecState: MutableStateFlow<TileAnimationSpec> =
        MutableStateFlow(subsamplingCore.tileAnimationSpec)
    private val _pausedContinuousTransformTypesState: MutableStateFlow<Int> =
        MutableStateFlow(subsamplingCore.pausedContinuousTransformTypes)
    private val _disabledBackgroundTilesState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabledBackgroundTiles)
    private val _stoppedState: MutableStateFlow<Boolean> = MutableStateFlow(subsamplingCore.stopped)
    private val _disabledAutoStopWithLifecycleState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabledAutoStopWithLifecycle)
    private val _regionDecodersState: MutableStateFlow<List<RegionDecoder.Factory>> =
        MutableStateFlow(subsamplingCore.regionDecoders)
    private val _showTileBoundsState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * If true, disabled subsampling
     */
    val disabledState: StateFlow<Boolean> = _disabledState

    /**
     * Set up the TileImage memory cache container
     */
    val tileImageCacheState: StateFlow<TileImageCache?> = _tileImageCacheState

    /**
     * If true, disabled TileImage memory cache
     */
    val disabledTileImageCacheState: StateFlow<Boolean> = _disabledTileImageCacheState

    /**
     * The animation spec for tile animation
     */
    val tileAnimationSpecState: StateFlow<TileAnimationSpec> = _tileAnimationSpecState

    /**
     * A continuous transform type that needs to pause loading
     */
    val pausedContinuousTransformTypesState: StateFlow<Int> = _pausedContinuousTransformTypesState

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    val disabledBackgroundTilesState: StateFlow<Boolean> = _disabledBackgroundTilesState

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    val stoppedState: StateFlow<Boolean> = _stoppedState

    /**
     * If true, the automatic stop function based on lifecycle is disabled
     */
    val disabledAutoStopWithLifecycleState: StateFlow<Boolean> = _disabledAutoStopWithLifecycleState

    /**
     * User-defined RegionDecoder
     */
    val regionDecodersState: StateFlow<List<RegionDecoder.Factory>> = _regionDecodersState

    /**
     * If true, the bounds of each tile is displayed
     */
    val showTileBoundsState: StateFlow<Boolean> = _showTileBoundsState

    /**
     * The stopped property controller, which can automatically stop and restart with the help of Lifecycle
     */
    val lifecycle: Lifecycle?
        get() = subsamplingCore.lifecycle


    /* *********************************** Information properties ******************************* */

    private val _imageInfoState: MutableStateFlow<ImageInfo?> =
        MutableStateFlow(subsamplingCore.imageInfo)
    private val _readyState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.ready)
    private val _foregroundTilesState: MutableStateFlow<List<TileSnapshot>> =
        MutableStateFlow(subsamplingCore.foregroundTiles)
    private val _backgroundTilesState: MutableStateFlow<List<TileSnapshot>> =
        MutableStateFlow(subsamplingCore.backgroundTiles)
    private val _sampleSizeState: MutableStateFlow<Int> =
        MutableStateFlow(subsamplingCore.sampleSize)
    private val _imageLoadRectState: MutableStateFlow<IntRectCompat> =
        MutableStateFlow(subsamplingCore.imageLoadRect)
    private val _tileGridSizeMapState: MutableStateFlow<Map<Int, IntOffsetCompat>> =
        MutableStateFlow(subsamplingCore.tileGridSizeMap)

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
    fun setImage(subsamplingImage: SubsamplingImage?): Boolean =
        subsamplingCore.setImage(subsamplingImage)

    /**
     * Set subsampling image
     */
    fun setImage(imageSource: ImageSource.Factory?, imageInfo: ImageInfo? = null): Boolean =
        subsamplingCore.setImage(imageSource, imageInfo)

    /**
     * Set subsampling image
     */
    fun setImage(imageSource: ImageSource?, imageInfo: ImageInfo? = null): Boolean =
        subsamplingCore.setImage(imageSource, imageInfo)

    /**
     * Set up an image source from which image tile are loaded
     */
    @Deprecated(
        message = "Use setImage(ImageSource.Factory?, ImageInfo?) instead",
        replaceWith = ReplaceWith("setImage(imageSource)"),
        level = DeprecationLevel.WARNING
    )
    fun setImageSource(imageSource: ImageSource.Factory?): Boolean =
        subsamplingCore.setImage(imageSource)

    /**
     * Set up an image source from which image tile are loaded
     */
    @Deprecated(
        message = "Use setImage(ImageSource?, ImageInfo?) instead",
        replaceWith = ReplaceWith("setImage(imageSource)"),
        level = DeprecationLevel.WARNING
    )
    fun setImageSource(imageSource: ImageSource?): Boolean = subsamplingCore.setImage(imageSource)

    /**
     * Set whether to disable subsampling
     */
    fun setDisabled(disabled: Boolean) {
        _disabledState.value = disabled
        subsamplingCore.setDisabled(disabled)
    }

    /**
     * Set up the TileImage memory cache
     */
    fun setTileImageCache(tileImageCache: TileImageCache?) {
        _tileImageCacheState.value = tileImageCache
        subsamplingCore.setTileImageCache(tileImageCache)
    }

    /**
     * Set whether to disable TileImage memory cache
     */
    fun setDisabledTileImageCache(disabledTileImageCache: Boolean) {
        _disabledTileImageCacheState.value = disabledTileImageCache
        subsamplingCore.setDisabledTileImageCache(disabledTileImageCache)
    }

    /**
     * Set the animation spec for tile animation
     */
    fun setTileAnimationSpec(tileAnimationSpec: TileAnimationSpec) {
        _tileAnimationSpecState.value = tileAnimationSpec
        subsamplingCore.setTileAnimationSpec(tileAnimationSpec)
    }

    /**
     * Set a continuous transform type that needs to pause loading. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.ContinuousTransformType
     */
    fun setPausedContinuousTransformTypes(pausedContinuousTransformTypes: Int) {
        _pausedContinuousTransformTypesState.value = pausedContinuousTransformTypes
        subsamplingCore.setPausedContinuousTransformTypes(pausedContinuousTransformTypes)
    }

    /**
     * Set whether to disable background tiles, which can save memory and improve performance.
     */
    fun setDisabledBackgroundTiles(disabledBackgroundTiles: Boolean) {
        _disabledBackgroundTilesState.value = disabledBackgroundTiles
        subsamplingCore.setDisabledBackgroundTiles(disabledBackgroundTiles)
    }

    /**
     * Set whether to stop subsampling and free loaded tiles, which are reloaded after restart.
     */
    fun setStopped(stopped: Boolean) {
        _stoppedState.value = stopped
        subsamplingCore.setStopped(stopped)
    }

    /**
     * Set whether to disable the life cycle-based automatic stop function
     */
    fun setDisabledAutoStopWithLifecycle(disabledAutoStopWithLifecycle: Boolean) {
        _disabledAutoStopWithLifecycleState.value = disabledAutoStopWithLifecycle
        subsamplingCore.setDisabledAutoStopWithLifecycle(disabledAutoStopWithLifecycle)
    }

    /**
     * Set user-defined RegionDecoder
     */
    fun setRegionDecoders(regionDecoders: List<RegionDecoder.Factory>) {
        _regionDecodersState.value = regionDecoders
        subsamplingCore.setRegionDecoders(regionDecoders)
    }

    /**
     * Set whether to display the boundary of each tile
     */
    fun setShowTileBounds(showTileBounds: Boolean) {
        _showTileBoundsState.value = showTileBounds
    }

    /**
     * Set the lifecycle for automatic stop and restart of subsampling.
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        subsamplingCore.setLifecycle(lifecycle)
    }


    /* *************************************** Internal ***************************************** */

    fun onAttachToWindow() {
        if (this.coroutineScope != null) return
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope
        subsamplingCore.onAttached()
    }

    fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope ?: return
        subsamplingCore.onDetached()
        coroutineScope.cancel("onDetachFromWindow")
        this.coroutineScope = null
    }
}