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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.SubsamplingCore
import com.github.panpf.zoomimage.subsampling.internal.ZoomableBridge
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.RectCompat
import com.github.panpf.zoomimage.util.TransformCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Creates and remember a [SubsamplingState] that can be used to subsampling of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.subsampling.SubsamplingStateTest.testRememberSubsamplingState
 */
@Composable
fun rememberSubsamplingState(zoomableState: ZoomableState): SubsamplingState {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val subsamplingState = remember(zoomableState, lifecycle) {
        SubsamplingState(zoomableState, lifecycle)
    }
    return subsamplingState
}

/**
 * A state object that can be used to subsampling of the content.
 *
 * @see com.github.panpf.zoomimage.compose.common.test.subsampling.SubsamplingStateTest
 */
@Stable
class SubsamplingState(
    val zoomableState: ZoomableState,
    val lifecycle: Lifecycle
) : RememberObserver {

    private var coroutineScope: CoroutineScope? = null
    private var rememberedCount = 0
    private val subsamplingCore: SubsamplingCore = SubsamplingCore(
        module = "SubsamplingState",
        logger = zoomableState.logger,
        tileImageConvertor = ComposeTileImageConvertor(),
        zoomableBridge = object : ZoomableBridge {
            override val contentVisibleRect: RectCompat
                get() = zoomableState.contentVisibleRectF.toCompat()

            override val transform: TransformCompat
                get() = zoomableState.transform.toCompat()

            override val continuousTransformType: Int
                get() = zoomableState.continuousTransformType

            override val transformFlow: Flow<TransformCompat>
                get() = snapshotFlow { zoomableState.transform.toCompat() }

            override val continuousTransformTypeFlow: Flow<Int>
                get() = snapshotFlow { zoomableState.continuousTransformType }

            override val containerSizeFlow: Flow<IntSizeCompat>
                get() = snapshotFlow { zoomableState.containerSize }.map { it.toCompat() }

            override val contentSizeFlow: Flow<IntSizeCompat>
                get() = snapshotFlow { zoomableState.contentSize }.map { it.toCompat() }

            override fun setContentOriginSize(contentOriginSize: IntSizeCompat) {
                zoomableState.setContentOriginSize(contentOriginSize.toPlatform())
            }
        },
        onReadyChanged = {
            ready = it.ready
            imageInfo = it.imageInfo
            tileGridSizeMap = it.tileGridSizeMap.mapValues { entry -> entry.value.toPlatform() }
            _stoppedState.value = it.stopped
        },
        onTileChanged = {
            backgroundTiles = it.backgroundTiles
            foregroundTiles = it.foregroundTiles
            sampleSize = it.sampleSize
            imageLoadRect = it.imageLoadRect.toPlatform()
        }
    )

    val logger: Logger = zoomableState.logger
    val subsamplingImage: SubsamplingImage?
        get() = subsamplingCore.subsamplingImage


    /* *********************************** Configurable properties ****************************** */

    private val _disabledState = mutableStateOf(subsamplingCore.disabled)
    private val _tileImageCacheState = mutableStateOf(subsamplingCore.tileImageCache)
    private val _disabledTileImageCacheState =
        mutableStateOf(subsamplingCore.disabledTileImageCache)
    private val _tileAnimationSpecState = mutableStateOf(subsamplingCore.tileAnimationSpec)
    private val _pausedContinuousTransformTypesState = mutableIntStateOf(
        subsamplingCore.pausedContinuousTransformTypes
    )
    private val _disabledBackgroundTilesState =
        mutableStateOf(subsamplingCore.disabledBackgroundTiles)
    private val _stoppedState = mutableStateOf(subsamplingCore.stopped)
    private val _disabledAutoStopWithLifecycleState =
        mutableStateOf(subsamplingCore.disabledAutoStopWithLifecycle)
    private val _regionDecodersState = mutableStateOf(subsamplingCore.regionDecoders)
    private val _showTileBoundsState = mutableStateOf(false)

    /**
     * If true, disabled subsampling
     */
    val disabled: Boolean by _disabledState

    /**
     * Set up the TileImage memory cache
     */
    val tileImageCache: TileImageCache? by _tileImageCacheState

    /**
     * If true, disabled TileImage memory cache
     */
    val disabledTileImageCache: Boolean by _disabledTileImageCacheState

    /**
     * The animation spec for tile animation
     */
    val tileAnimationSpec: TileAnimationSpec by _tileAnimationSpecState

    /**
     * A continuous transform type that needs to pause loading. Allow multiple types to be combined through the 'and' operator
     *
     * @see com.github.panpf.zoomimage.zoom.ContinuousTransformType
     */
    val pausedContinuousTransformTypes: Int by _pausedContinuousTransformTypesState

    /**
     * Disabling the background tile, which saves memory and improves performance, but when switching sampleSize,
     * the basemap will be exposed, the user will be able to perceive a choppy switching process,
     * and the user experience will be reduced
     */
    val disabledBackgroundTiles: Boolean by _disabledBackgroundTilesState

    /**
     * If true, subsampling stops and free loaded tiles, which are reloaded after restart
     */
    val stopped: Boolean by _stoppedState

    /**
     * If true, the automatic stop function based on lifecycle is disabled
     */
    val disabledAutoStopWithLifecycle: Boolean by _disabledAutoStopWithLifecycleState

    /**
     * User-defined RegionDecoder
     */
    val regionDecoders: List<RegionDecoder.Factory> by _regionDecodersState

    /**
     * If true, the bounds of each tile is displayed
     */
    val showTileBounds: Boolean by _showTileBoundsState


    /* *********************************** Information properties ******************************* */

    /**
     * Whether the image is ready for subsampling
     */
    var ready: Boolean by mutableStateOf(subsamplingCore.ready)
        private set

    /**
     * The information of the image, including width, height, format, etc
     */
    var imageInfo: ImageInfo? by mutableStateOf(subsamplingCore.imageInfo)
        private set

    /**
     * Tile grid size map, key is sample size, value is tile grid size
     */
    var tileGridSizeMap: Map<Int, IntOffset> by mutableStateOf(
        subsamplingCore.tileGridSizeMap
            .mapValues { entry -> entry.value.toPlatform() }
    )
        private set

    /**
     * The sample size of the image
     */
    var sampleSize: Int by mutableIntStateOf(subsamplingCore.sampleSize)
        private set

    /**
     * The image load rect
     */
    var imageLoadRect: IntRect by mutableStateOf(subsamplingCore.imageLoadRect.toPlatform())
        private set

    /**
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    var foregroundTiles: List<TileSnapshot> by mutableStateOf(subsamplingCore.foregroundTiles)
        private set

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    var backgroundTiles: List<TileSnapshot> by mutableStateOf(subsamplingCore.backgroundTiles)
        private set

    init {
        subsamplingCore.setLifecycle(lifecycle)
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
    fun setImageSource(imageSource: ImageSource?): Boolean =
        subsamplingCore.setImage(imageSource)

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


    /* *************************************** Internal ***************************************** */

    override fun onRemembered() {
        // Since SubsamplingState is annotated with @Stable, onRemembered will be executed multiple times,
        // but we only need execute it once
        rememberedCount++
        if (rememberedCount != 1) return

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        this.coroutineScope = coroutineScope
        subsamplingCore.onAttached()
    }

    override fun onAbandoned() = onForgotten()
    override fun onForgotten() {
        // Since SubsamplingState is annotated with @Stable, onForgotten will be executed multiple times,
        // but we only need execute it once
        if (rememberedCount <= 0) return
        rememberedCount--
        if (rememberedCount != 0) return

        val coroutineScope = this.coroutineScope ?: return
        subsamplingCore.onDetached()
        coroutineScope.cancel("onForgotten")
        this.coroutineScope = null
    }
}