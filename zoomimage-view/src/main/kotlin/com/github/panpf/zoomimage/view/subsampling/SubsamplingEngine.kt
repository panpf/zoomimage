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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

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
        zoomableCore = object : ZoomableBridge {
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

            override fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
                zoomableEngine.contentOriginSizeState.value = contentOriginSize
            }
        },
        onReadyChanged = {
            _readyState.value = it.ready
            _imageInfoState.value = it.imageInfo
            _tileGridSizeMapState.value = it.tileGridSizeMap
            stoppedState.value = it.stopped
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

    /**
     * If true, disabled subsampling
     */
    val disabledState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabled)

    /**
     * Set up the TileImage memory cache container
     */
    val tileImageCacheState: MutableStateFlow<TileImageCache?> =
        MutableStateFlow(subsamplingCore.tileImageCache)

    /**
     * If true, disabled TileImage memory cache
     */
    val disabledTileImageCacheState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabledTileImageCache)

    /**
     * The animation spec for tile animation
     */
    val tileAnimationSpecState: MutableStateFlow<TileAnimationSpec> =
        MutableStateFlow(subsamplingCore.tileAnimationSpec)

    /**
     * A continuous transform type that needs to pause loading
     */
    val pausedContinuousTransformTypesState: MutableStateFlow<Int> =
        MutableStateFlow(subsamplingCore.pausedContinuousTransformTypes)

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    val disabledBackgroundTilesState: MutableStateFlow<Boolean> =
        MutableStateFlow(subsamplingCore.disabledBackgroundTiles)

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    val stoppedState: MutableStateFlow<Boolean> = MutableStateFlow(subsamplingCore.stopped)

    /**
     * User-defined RegionDecoder
     */
    var regionDecodersState: MutableStateFlow<List<RegionDecoder.Factory>> =
        MutableStateFlow(subsamplingCore.regionDecoders)


    /**
     * If true, the bounds of each tile is displayed
     */
    val showTileBoundsState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * The stopped property controller, which can automatically stop and restart with the help of Lifecycle
     */
    var lifecycle: Lifecycle?
        get() = subsamplingCore.lifecycle
        set(value) {
            subsamplingCore.lifecycle = value
        }


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


    /* *************************************** Internal ***************************************** */

    fun onAttachToWindow() {
        val coroutineScope = this.coroutineScope
        if (coroutineScope != null) return

        val newCoroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = newCoroutineScope

        bindProperties(newCoroutineScope)
        subsamplingCore.setCoroutineScope(newCoroutineScope)
    }

    fun onDetachFromWindow() {
        val coroutineScope = this.coroutineScope ?: return

        subsamplingCore.setCoroutineScope(null)
        coroutineScope.cancel("onDetachFromWindow")
        this.coroutineScope = null
    }

    private fun bindProperties(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            // Changes in viewSize cause a large chain reaction that can cause large memory fluctuations.
            // View size animations cause frequent changes in viewSize, so a delayed reset avoids this problem
            @Suppress("OPT_IN_USAGE")
            zoomableEngine.containerSizeState.debounce(80).collect {
                subsamplingCore.setContainerSize(it)
            }
        }
        coroutineScope.launch {
            zoomableEngine.contentSizeState.collect {
                subsamplingCore.setContentSize(it)
            }
        }

        coroutineScope.launch {
            disabledState.collect {
                subsamplingCore.disabled = it
            }
        }
        coroutineScope.launch {
            tileImageCacheState.collect {
                subsamplingCore.tileImageCache = it
            }
        }
        coroutineScope.launch {
            disabledTileImageCacheState.collect {
                subsamplingCore.disabledTileImageCache = it
            }
        }
        coroutineScope.launch {
            tileAnimationSpecState.collect {
                subsamplingCore.tileAnimationSpec = it
            }
        }
        coroutineScope.launch {
            pausedContinuousTransformTypesState.collect {
                subsamplingCore.pausedContinuousTransformTypes = it
            }
        }
        coroutineScope.launch {
            disabledBackgroundTilesState.collect {
                subsamplingCore.disabledBackgroundTiles = it
            }
        }
        coroutineScope.launch {
            stoppedState.collect {
                subsamplingCore.stopped = it
            }
        }
        coroutineScope.launch {
            regionDecodersState.collect {
                subsamplingCore.setRegionDecoders(it)
            }
        }
    }
}