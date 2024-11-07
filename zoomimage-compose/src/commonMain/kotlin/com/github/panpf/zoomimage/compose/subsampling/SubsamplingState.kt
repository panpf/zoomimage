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
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.panpf.zoomimage.compose.util.isEmpty
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.compose.util.toShortString
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
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
import kotlin.math.roundToInt

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
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var resetTileDecoderJob: Job? = null
    private val tileImageCacheSpec = TileImageCacheSpec()
    private val tileImageCacheHelper = TileImageCacheHelper(tileImageCacheSpec)
    private val tileImageConvertor = ComposeTileImageConvertor()
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


    val logger: Logger = zoomableState.logger

    var subsamplingImage: SubsamplingImage? = null
        private set


    /* *********************************** Configurable properties ****************************** */

    /**
     * Set up the TileImage memory cache container
     */
    var tileImageCache: TileImageCache? by mutableStateOf(null)

    /**
     * If true, disabled TileImage memory cache
     */
    var disabledTileImageCache: Boolean by mutableStateOf(false)

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

    /**
     * User-defined RegionDecoder
     */
    var regionDecoders: List<RegionDecoder.Factory> by mutableStateOf(emptyList())


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, etc
     */
    var imageInfo: ImageInfo? by mutableStateOf(null)
        private set

    /**
     * Tile grid size map, key is sample size, value is tile grid size
     */
    var tileGridSizeMap: Map<Int, IntOffset> by mutableStateOf(emptyMap())
        private set

    /**
     * Whether the image is ready for subsampling
     */
    var ready: Boolean by mutableStateOf(false)
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
     * Foreground tiles, all tiles corresponding to the current sampleSize, this list will be updated when the sampleSize changes, when the loading state of any of the tiles and the progress of the animation changes
     */
    var foregroundTiles: List<TileSnapshot> by mutableStateOf(emptyList())
        private set

    /**
     * Background tiles to avoid revealing the basemap during the process of switching sampleSize to load a new tile, the background tile will be emptied after the new tile is fully loaded and the transition animation is complete, the list of background tiles contains only tiles within the currently loaded area
     */
    var backgroundTiles: List<TileSnapshot> by mutableStateOf(emptyList())
        private set


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set subsampling image
     */
    fun setImage(subsamplingImage: SubsamplingImage?): Boolean {
        if (this.subsamplingImage == subsamplingImage) return false
        logger.d { "SubsamplingState. setImage. '${this.subsamplingImage}' -> '${subsamplingImage}'" }
        clean("setImage")
        this.subsamplingImage = subsamplingImage
        if (rememberedCount > 0) {
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
            snapshotFlow { tileImageCache }.collect {
                tileImageCacheSpec.tileImageCache = it
            }
        }
        coroutineScope.launch(Dispatchers.Main.immediate) {
            snapshotFlow { disabledTileImageCache }.collect {
                tileImageCacheSpec.disabled = it
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
            @Suppress("OPT_IN_USAGE")
            snapshotFlow { zoomableState.containerSize }.debounce(80).collect {
                val oldPreferredTileSize = this@SubsamplingState.preferredTileSize.toCompat()
                val newPreferredTileSize = calculatePreferredTileSize(it.toCompat())
                val checkPassed = checkNewPreferredTileSize(
                    oldPreferredTileSize = oldPreferredTileSize,
                    newPreferredTileSize = newPreferredTileSize
                )
                logger.d {
                    "SubsamplingState. reset preferredTileSize. " +
                            "oldPreferredTileSize=$oldPreferredTileSize, " +
                            "newPreferredTileSize=$newPreferredTileSize, " +
                            "checkPassed=$checkPassed. " +
                            "'${subsamplingImage?.key}'"
                }
                if (checkPassed) {
                    this@SubsamplingState.preferredTileSize = newPreferredTileSize.toPlatform()
                }
            }
        }
        coroutineScope.launch {
            snapshotFlow { zoomableState.contentSize }.collect {
                contentSize = it
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
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val subsamplingImage = subsamplingImage
        val contentSize = contentSize
        val coroutineScope = coroutineScope
        if (subsamplingImage == null || contentSize.isEmpty() || coroutineScope == null) {
            logger.d {
                "SubsamplingState. resetTileDecoder:$caller. skipped. " +
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
                contentSize = contentSize.toCompat(),
                regionDecoders = regionDecoders,
                onImageInfoPassed = {
                    zoomableState.contentOriginSize = it.size.toPlatform()
                }
            )
            if (tileDecoderResult.isFailure) {
                logger.d {
                    "SubsamplingState. resetTileDecoder:$caller. failed. " +
                            "${tileDecoderResult.exceptionOrNull()!!.message}. " +
                            "'${subsamplingImage.key}'"
                }
                zoomableState.contentOriginSize = IntSize.Zero
                return@launch
            }

            val tileDecoder = tileDecoderResult.getOrThrow()
            val imageInfo = subsamplingImage.imageInfo ?: tileDecoder.imageInfo
            this@SubsamplingState.imageInfo = imageInfo
            this@SubsamplingState.tileDecoder = tileDecoder
            logger.d {
                "SubsamplingState. resetTileDecoder:$caller. success. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "imageInfo=${imageInfo.toShortString()}. " +
                        "'${subsamplingImage.key}'"
            }

            resetTileManager(caller)
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager("resetTileManager:$caller")

        val subsamplingImage = subsamplingImage
        val tileDecoder = tileDecoder
        val imageInfo = imageInfo
        val contentSize = contentSize
        val preferredTileSize = preferredTileSize
        if (subsamplingImage == null || tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "SubsamplingState. resetTileManager:$caller. failed. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileDecoder=${tileDecoder}, " +
                        "imageInfo=${imageInfo}. " +
                        "'${subsamplingImage?.key}'"
            }
            return
        }

        val tileManager = TileManager(
            logger = logger,
            subsamplingImage = subsamplingImage,
            tileDecoder = tileDecoder,
            tileImageConvertor = tileImageConvertor,
            contentSize = contentSize.toCompat(),
            preferredTileSize = preferredTileSize.toCompat(),
            tileImageCacheHelper = tileImageCacheHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                if (this@SubsamplingState.tileManager == manager) {
                    backgroundTiles = manager.backgroundTiles
                    foregroundTiles = manager.foregroundTiles
                }
            },
            onSampleSizeChanged = { manager ->
                if (this@SubsamplingState.tileManager == manager) {
                    sampleSize = manager.sampleSize
                }
            },
            onImageLoadRectChanged = { manager ->
                if (this@SubsamplingState.tileManager == manager) {
                    imageLoadRect = manager.imageLoadRect.toPlatform()
                }
            }
        )
        tileManager.pausedContinuousTransformTypes =
            this@SubsamplingState.pausedContinuousTransformTypes
        tileManager.disabledBackgroundTiles = this@SubsamplingState.disabledBackgroundTiles
        tileManager.tileAnimationSpec = this@SubsamplingState.tileAnimationSpec

        tileGridSizeMap = tileManager.sortedTileGridMap.associate { entry ->
            entry.sampleSize to entry.tiles.last().coordinate.let { IntOffset(it.x + 1, it.y + 1) }
        }
        logger.d {
            "SubsamplingState. resetTileManager:$caller. success. " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${subsamplingImage.key}'"
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
            logger.d { "SubsamplingState. refreshTiles:$caller. interrupted, stopped. '${subsamplingImage?.key}'" }
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
        logger.d { "SubsamplingState. refreshReadyState:$caller. ready=$newReady. '${subsamplingImage?.key}'" }
        ready = newReady
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val resetTileDecoderJob1 = this@SubsamplingState.resetTileDecoderJob
        if (resetTileDecoderJob1?.isActive == true) {
            resetTileDecoderJob1.cancel("cleanTileDecoder:$caller")
            this@SubsamplingState.resetTileDecoderJob = null
        }
        val tileDecoder = this@SubsamplingState.tileDecoder
        if (tileDecoder != null) {
            logger.d { "SubsamplingState. cleanTileDecoder:$caller. '${subsamplingImage?.key}'" }
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch(ioCoroutineDispatcher()) {
                tileDecoder.close()
            }
            this@SubsamplingState.tileDecoder = null
            refreshReadyState("cleanTileDecoder:$caller")
        }
        imageInfo = null
        zoomableState.contentOriginSize = IntSizeCompat.Zero.toPlatform()
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
            logger.d { "SubsamplingState. cleanTileManager:$caller. '${subsamplingImage?.key}'" }
            refreshReadyState("cleanTileManager:$caller")
        }
    }

    private fun clean(@Suppress("SameParameterValue") caller: String) {
        cleanTileManager("clean:$caller")
        cleanTileDecoder("clean:$caller")
    }
}