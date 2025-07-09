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

package com.github.panpf.zoomimage.subsampling.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.RegionDecoder
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.TileAnimationSpec
import com.github.panpf.zoomimage.subsampling.TileImageCache
import com.github.panpf.zoomimage.subsampling.TileSnapshot
import com.github.panpf.zoomimage.subsampling.internal.TileManager.Companion.DefaultPausedContinuousTransformTypes
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.closeQuietly
import com.github.panpf.zoomimage.util.ioCoroutineDispatcher
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.util.round
import com.github.panpf.zoomimage.util.toShortString
import com.github.panpf.zoomimage.zoom.ContinuousTransformType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Core that control subsampling
 */
class SubsamplingCore(
    val module: String,
    val logger: Logger,
    val tileImageConvertor: TileImageConvertor?,
    val zoomableCore: ZoomableBridge,
    val onReadyChanged: (SubsamplingCore) -> Unit,
    val onTileChanged: (SubsamplingCore) -> Unit,
) {

    private var coroutineScope: CoroutineScope? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var tileImageCacheHelper = TileImageCacheHelper()
    private var resetTileDecoderJob: Job? = null
    private val refreshTilesFlow = MutableSharedFlow<String>()
    private var preferredTileSize = IntSizeCompat.Zero
    private var contentSize = IntSizeCompat.Zero
    private var cachedImage: SubsamplingImage? = null
    private val logKey: String?
        get() = subsamplingImage?.key ?: "null"
    private val stoppedLifecycleObserver = LifecycleEventObserver { owner, _ ->
        val disabledAutoStopWithLifecycle = disabledAutoStopWithLifecycle
        logger.d {
            "$module. lifecycle. ${owner.lifecycle.currentState}. " +
                    "disabledAutoStopWithLifecycle=$disabledAutoStopWithLifecycle. " +
                    "'$logKey'"
        }
        if (!disabledAutoStopWithLifecycle) {
            refreshStoppedState()
        }
    }

    var subsamplingImage: SubsamplingImage? = null
        private set

    var disabled: Boolean = false
        set(value) {
            if (field != value) {
                logger.d { "$module. disabled=$value. '$logKey'" }
                if (value) {
                    cachedImage = subsamplingImage
                    setImage(null as SubsamplingImage?)
                    field = value
                } else {
                    field = value
                    setImage(cachedImage)
                    cachedImage = null
                }
            }
        }

    var tileImageCache: TileImageCache?
        get() = tileImageCacheHelper.tileImageCache
        set(value) {
            tileImageCacheHelper.tileImageCache = value
        }

    var disabledTileImageCache: Boolean
        get() = tileImageCacheHelper.disabled
        set(value) {
            if (tileImageCacheHelper.disabled != value) {
                logger.d { "$module. disabledTileImageCache=$value. '$logKey'" }
                tileImageCacheHelper.disabled = value
            }
        }

    var tileAnimationSpec: TileAnimationSpec = TileAnimationSpec.Default
        set(value) {
            if (field != value) {
                field = value
                logger.d { "$module. tileAnimationSpec=$value. '$logKey'" }
                tileManager?.tileAnimationSpec = value
            }
        }

    var pausedContinuousTransformTypes: Int = DefaultPausedContinuousTransformTypes
        set(value) {
            if (field != value) {
                field = value
                logger.d {
                    val namesString = ContinuousTransformType.names(value)
                        .joinToString(prefix = "[", postfix = "]")
                    "$module. pausedContinuousTransformTypes=$namesString. '$logKey'"
                }
                tileManager?.pausedContinuousTransformTypes = value
            }
        }

    var disabledBackgroundTiles: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                logger.d { "$module. disabledBackgroundTiles=$value. '$logKey'" }
                tileManager?.disabledBackgroundTiles = value
            }
        }

    var stopped: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                logger.d { "$module. stopped=$value. '$logKey'" }
                val stoppedState = if (value) "stopped" else "started"
                if (value) {
                    tileManager?.clean(stoppedState)
                }
                refreshReadyState(stoppedState)
            }
        }

    var lifecycle: Lifecycle? = null
        set(value) {
            if (field != value) {
                field?.removeObserver(stoppedLifecycleObserver)
                field = value
                if (coroutineScope != null) {
                    value?.addObserver(stoppedLifecycleObserver)
                }
            }
        }

    var disabledAutoStopWithLifecycle: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                logger.d { "$module. disabledAutoStopWithLifecycle=$value. '$logKey'" }
                if (value) {
                    stopped = false
                } else {
                    refreshStoppedState()
                }
            }
        }

    var regionDecoders: List<RegionDecoder.Factory> = emptyList()
        private set


    var imageInfo: ImageInfo? = null
        private set
    var ready: Boolean = false
        private set
    var foregroundTiles: List<TileSnapshot> = emptyList()
        private set
    var backgroundTiles: List<TileSnapshot> = emptyList()
        private set
    var sampleSize: Int = 0
        private set
    var imageLoadRect: IntRectCompat = IntRectCompat.Zero
        private set
    var tileGridSizeMap: Map<Int, IntOffsetCompat> = emptyMap()
        private set


    fun setImage(subsamplingImage: SubsamplingImage?): Boolean {
        if (disabled) {
            logger.d { "$module. setImage. disabled. '${subsamplingImage}'" }
            cachedImage = subsamplingImage
            return false
        }

        if (this.subsamplingImage == subsamplingImage) return false
        logger.d { "$module. setImage. '${this.subsamplingImage}' -> '${subsamplingImage}'" }
        clean("setImage")
        this.subsamplingImage = subsamplingImage
        if (coroutineScope != null && subsamplingImage != null) {
            resetTileDecoder("setImage")
        }
        return true
    }

    fun setImage(imageSource: ImageSource.Factory?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let { SubsamplingImage(it, imageInfo) })
    }

    fun setImage(imageSource: ImageSource?, imageInfo: ImageInfo? = null): Boolean {
        return setImage(imageSource?.let {
            SubsamplingImage(ImageSource.WrapperFactory(it), imageInfo)
        })
    }


    fun setContainerSize(containerSize: IntSizeCompat) {
        val oldPreferredTileSize = preferredTileSize
        val newPreferredTileSize = calculatePreferredTileSize(containerSize)
        val checkPassed = checkNewPreferredTileSize(
            oldPreferredTileSize = oldPreferredTileSize,
            newPreferredTileSize = newPreferredTileSize
        )
        logger.d {
            "$module. setContainerSize. preferredTileSize ${if (checkPassed) "changed" else "keep"}. " +
                    "oldPreferredTileSize=${oldPreferredTileSize.toShortString()}, " +
                    "newPreferredTileSize=${newPreferredTileSize.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}. " +
                    "'$logKey'"
        }
        if (checkPassed) {
            this.preferredTileSize = newPreferredTileSize
            resetTileManager("preferredTileSizeChanged")
        }
    }

    fun setContentSize(contentSize: IntSizeCompat) {
        if (this.contentSize != contentSize) {
            this.contentSize = contentSize
            resetTileDecoder("contentSizeChanged")
        }
    }

    fun setRegionDecoders(regionDecoders: List<RegionDecoder.Factory>) {
        if (this.regionDecoders != regionDecoders) {
            this.regionDecoders = regionDecoders
            resetTileDecoder("regionDecodersChanged")
        }
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope?) {
        val lastCoroutineScope = this.coroutineScope
        if (coroutineScope != null) {
            this.coroutineScope = coroutineScope
            if (lastCoroutineScope == null) {
                lifecycle?.addObserver(stoppedLifecycleObserver)

                coroutineScope.launch {
                    zoomableCore.transformFlow.collect {
                        refreshTiles(caller = "transformChanged")
                    }
                }
                coroutineScope.launch {
                    zoomableCore.continuousTransformTypeFlow.collect {
                        refreshTiles(caller = "continuousTransformTypeChanged")
                    }
                }
                coroutineScope.launch {
                    refreshTilesFlow.collect {
                        refreshTiles(caller = it)
                    }
                }
            }
        } else {
            if (lastCoroutineScope != null) {
                lifecycle?.removeObserver(stoppedLifecycleObserver)
                clean("setCoroutineScope")
            }
            this.coroutineScope = coroutineScope
        }
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager(caller)
        cleanTileDecoder(caller)

        val subsamplingImage = subsamplingImage
        val contentSize = contentSize
        val coroutineScope = coroutineScope
        if (subsamplingImage == null || contentSize.isEmpty() || coroutineScope == null) {
            logger.d {
                "$module. resetTileDecoder:$caller. skipped. " +
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
                regionDecoders = regionDecoders,
                onImageInfoPassed = {
                    zoomableCore.setContentOriginSize(it.size)
                }
            )
            if (tileDecoderResult.isFailure) {
                logger.d {
                    "$module. resetTileDecoder:$caller. failed. " +
                            "${tileDecoderResult.exceptionOrNull()!!.message}. " +
                            "'${subsamplingImage.key}'"
                }
                zoomableCore.setContentOriginSize(IntSizeCompat.Zero)
                return@launch
            }

            val tileDecoder = tileDecoderResult.getOrThrow()
            val imageInfo = subsamplingImage.imageInfo ?: tileDecoder.imageInfo
            this@SubsamplingCore.imageInfo = imageInfo
            this@SubsamplingCore.tileDecoder = tileDecoder
            logger.d {
                "$module. resetTileDecoder:$caller. success. " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "imageInfo=${imageInfo.toShortString()}. " +
                        "'${subsamplingImage.key}'"
            }
            refreshReadyState(caller)
            resetTileManager(caller)
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager(caller)

        val subsamplingImage = subsamplingImage
        val tileDecoder = tileDecoder
        val imageInfo = imageInfo
        val preferredTileSize = preferredTileSize
        val contentSize = contentSize
        if (subsamplingImage == null || tileDecoder == null || imageInfo == null || preferredTileSize.isEmpty() || contentSize.isEmpty()) {
            logger.d {
                "$module. resetTileManager:$caller. failed. " +
                        "subsamplingImage=${subsamplingImage}, " +
                        "contentSize=${contentSize.toShortString()}, " +
                        "preferredTileSize=${preferredTileSize.toShortString()}, " +
                        "tileDecoder=${tileDecoder}, " +
                        "'$logKey'"
            }
            return
        }

        val tileManager = TileManager(
            logger = logger,
            subsamplingImage = subsamplingImage,
            tileDecoder = tileDecoder,
            tileImageConvertor = tileImageConvertor,
            preferredTileSize = preferredTileSize,
            contentSize = contentSize,
            tileImageCacheHelper = tileImageCacheHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.backgroundTiles = manager.backgroundTiles
                    this@SubsamplingCore.foregroundTiles = manager.foregroundTiles
                    onTileChanged(this@SubsamplingCore)
                }
            },
            onSampleSizeChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.sampleSize = manager.sampleSize
                    onTileChanged(this@SubsamplingCore)
                }
            },
            onImageLoadRectChanged = { manager ->
                if (this@SubsamplingCore.tileManager == manager) {
                    this@SubsamplingCore.imageLoadRect = manager.imageLoadRect
                    onTileChanged(this@SubsamplingCore)
                }
            }
        )
        tileManager.pausedContinuousTransformTypes =
            this@SubsamplingCore.pausedContinuousTransformTypes
        tileManager.disabledBackgroundTiles =
            this@SubsamplingCore.disabledBackgroundTiles
        tileManager.tileAnimationSpec = this@SubsamplingCore.tileAnimationSpec

        this@SubsamplingCore.tileGridSizeMap = tileManager.sortedTileGridMap.associate { entry ->
            entry.sampleSize to entry.tiles.last().coordinate
                .let { IntOffsetCompat(it.x + 1, it.y + 1) }
        }
        logger.d {
            "$module. resetTileManager:$caller. success. " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "preferredTileSize=${preferredTileSize.toShortString()}, " +
                    "tileGridMap=${tileManager.sortedTileGridMap.toIntroString()}. " +
                    "'${subsamplingImage.key}'"
        }
        this@SubsamplingCore.tileManager = tileManager
        refreshReadyState(caller)
    }

    private fun refreshTiles(
        contentVisibleRect: IntRectCompat = zoomableCore.contentVisibleRect.round(),
        scale: Float = zoomableCore.transform.scaleX,
        rotation: Int = zoomableCore.transform.rotation.roundToInt(),
        @ContinuousTransformType continuousTransformType: Int = zoomableCore.continuousTransformType,
        caller: String,
    ) {
        val tileManager = tileManager ?: return
        if (stopped) {
            logger.d { "$module. refreshTiles:$caller. interrupted, stopped. '$logKey'" }
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
        val newReady = imageInfo != null && tileManager != null && tileDecoder != null && !stopped
        // Duplicate callbacks cannot be intercepted by validating 'this@SubsamplingCore.ready != newReady',
        // because SubsamplingState and SubsamplingEngine need to rely on this callback to update properties such as stopped, imageInfo, tileGridSizeMap, etc
        logger.d { "$module. refreshReadyState:$caller. ready=$newReady. '$logKey'" }
        this@SubsamplingCore.ready = newReady
        onReadyChanged(this@SubsamplingCore)
        coroutineScope?.launch {
            refreshTilesFlow.emit("refreshReadyState:$caller")
        }
    }

    private fun refreshStoppedState() {
        val lifecycle = lifecycle
        if (lifecycle != null) {
            val stopped = !lifecycle.currentState.isAtLeast(STARTED)
            this@SubsamplingCore.stopped = stopped
        }
    }

    private fun cleanTileDecoder(caller: String) {
        val resetTileDecoderJob1 = this@SubsamplingCore.resetTileDecoderJob
        if (resetTileDecoderJob1 != null && resetTileDecoderJob1.isActive) {
            resetTileDecoderJob1.cancel("cleanTileDecoder:$caller")
            this@SubsamplingCore.resetTileDecoderJob = null
        }

        val tileDecoder = this@SubsamplingCore.tileDecoder
        val imageInfo = this@SubsamplingCore.imageInfo
        if (tileDecoder != null) {
            logger.d { "$module. cleanTileDecoder:$caller. '$logKey'" }
            @Suppress("OPTthis@SubsamplingCore.IN_USAGE", "OPT_IN_USAGE")
            GlobalScope.launch(ioCoroutineDispatcher()) {
                tileDecoder.closeQuietly()
            }
            this@SubsamplingCore.tileDecoder = null
        }
        if (imageInfo != null) {
            this@SubsamplingCore.imageInfo = null
        }
        if (tileDecoder != null || imageInfo != null) {
            refreshReadyState(caller)
        }

        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.Main) {
            zoomableCore.setContentOriginSize(IntSizeCompat.Zero)
        }
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingCore.tileManager
        if (tileManager != null) {
            logger.d { "$module. cleanTileManager:$caller. '$logKey'" }
            tileManager.clean(caller)
            this@SubsamplingCore.tileManager = null
            this@SubsamplingCore.tileGridSizeMap = emptyMap()
            this@SubsamplingCore.foregroundTiles = emptyList()
            this@SubsamplingCore.backgroundTiles = emptyList()
            this@SubsamplingCore.sampleSize = 0
            this@SubsamplingCore.imageLoadRect = IntRectCompat.Zero
            refreshReadyState(caller)
            onTileChanged(this@SubsamplingCore)
        }
    }

    private fun clean(caller: String) {
        cleanTileDecoder(caller)
        cleanTileManager(caller)
    }
}