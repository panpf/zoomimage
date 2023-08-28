package com.github.panpf.zoomimage.compose.subsampling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toPlatform
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.rememberZoomImageLogger
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * Creates and remember a [SubsamplingState] that can be used to subsampling of the content.
 */
@Composable
fun rememberSubsamplingState(logger: Logger = rememberZoomImageLogger()): SubsamplingState {
    val subsamplingState = remember(logger) {
        SubsamplingState(logger)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        subsamplingState.setLifecycle(lifecycle)
    }
    return subsamplingState
}

/**
 * A state object that can be used to subsampling of the content.
 */
@Stable
class SubsamplingState(logger: Logger) : RememberObserver {

    val logger: Logger = logger.newLogger(module = "SubsamplingState")
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null
    private val tileMemoryCacheHelper = TileMemoryCacheHelper(this.logger)
    private val tileBitmapPoolHelper = TileBitmapPoolHelper(this.logger)
    private var lifecycle: Lifecycle? = null
    private val resetPausedLifecycleObserver by lazy {
        LifecycleEventObserver { _, event ->
            if (event == ON_START) {
                resetPaused("LifecycleStateChanged:ON_START")
            } else if (event == ON_STOP) {
                resetPaused("LifecycleStateChanged:ON_STOP")
            }
        }
    }
    internal var imageKey: String? = null

    internal var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    internal var contentSize: IntSize by mutableStateOf(IntSize.Zero)


    /* *********************************** Configurable properties ****************************** */

    /**
     * If true, the Exif rotation information for the image is ignored
     */
    var ignoreExifOrientation by mutableStateOf(false)

    /**
     * Set up the tile memory cache container
     */
    var tileMemoryCache: TileMemoryCache? by mutableStateOf(null)

    /**
     * If true, disable memory cache
     */
    var disableMemoryCache: Boolean by mutableStateOf(false)

    /**
     * Set up a shared Bitmap pool for the tile
     */
    var tileBitmapPool: TileBitmapPool? by mutableStateOf(null)

    /**
     * If true, Bitmap reuse is disabled
     */
    var disallowReuseBitmap: Boolean by mutableStateOf(false)

    /**
     * If true, subsampling is paused and loaded tiles are released, which will be reloaded after resumed
     */
    var paused by mutableStateOf(false)

    /**
     * If true, the bounds of each tile is displayed
     */
    var showTileBounds: Boolean by mutableStateOf(false)


    /* *********************************** Information properties ******************************* */

    /**
     * The information of the image, including width, height, format, exif information, etc
     */
    var imageInfo: ImageInfo? by mutableStateOf(null)
        private set

    /**
     * Whether the image is ready for subsampling
     */
    var ready by mutableStateOf(false)
        private set

    /**
     * A snapshot of the tile list
     */
    // todo rename to tileSnapshotList
    var tileList: List<TileSnapshot> by mutableStateOf(emptyList())
        private set

    /**
     * The image load rect
     */
    var imageLoadRect: IntRect by mutableStateOf(IntRect.Zero)
        private set


    /* ********************************* Interact with consumers ******************************** */

    /**
     * Set up an image source from which image tile are loaded
     */
    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        imageKey = imageSource?.key
        resetTileDecoder("setImageSource")
        return true
    }

    /**
     * Set the lifecycle, which automatically controls pause and resume, which is obtained from [LocalLifecycleOwner] by default,
     * and can be set by this method if the default acquisition method is not applicable
     */
    fun setLifecycle(lifecycle: Lifecycle?) {
        if (this.lifecycle != lifecycle) {
            unregisterLifecycleObserver()
            this.lifecycle = lifecycle
            registerLifecycleObserver()
            resetPaused("setLifecycle")
        }
    }


    /* *************************************** Internal ***************************************** */

    @Composable
    internal fun BindZoomableState(zoomableState: ZoomableState) {
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.containerSize }.collect {
                // Changes in containerSize cause a large chain reaction that can cause large memory fluctuations.
                // Size animations cause frequent changes in containerSize, so a delayed reset avoids this problem
                if (it.isNotEmpty()) {
                    delay(60)
                }
                containerSize = it
                resetTileManager("containerSizeChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.contentSize }.collect {
                contentSize = it
                resetTileDecoder("contentSizeChanged")
            }
        }

        val refreshTiles: (caller: String) -> Unit = { caller ->
            refreshTiles(
                contentVisibleRect = zoomableState.contentVisibleRect,
                scale = zoomableState.transform.scaleX,
                rotation = zoomableState.transform.rotation.roundToInt(),
                transforming = zoomableState.transforming,
                caller = caller
            )
        }
        LaunchedEffect(Unit) {
            snapshotFlow { ready }.collect {
                val imageInfo = imageInfo
                zoomableState.contentOriginSize = if (it && imageInfo != null) {
                    IntSize(imageInfo.width, imageInfo.height)
                } else {
                    IntSize.Zero
                }
                refreshTiles("readyChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.transform }.collect {
                refreshTiles("transformChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { zoomableState.transforming }.collect {
                refreshTiles("transformingChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { paused }.collect {
                refreshTiles(if (it) "paused" else "resumed")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { ignoreExifOrientation }.collect {
                resetTileDecoder("ignoreExifOrientationChanged")
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { tileMemoryCache }.collect {
                tileMemoryCacheHelper.tileMemoryCache = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { disableMemoryCache }.collect {
                tileMemoryCacheHelper.disableMemoryCache = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { tileBitmapPool }.collect {
                tileBitmapPoolHelper.tileBitmapPool = it
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { disallowReuseBitmap }.collect {
                tileBitmapPoolHelper.disallowReuseBitmap = it
            }
        }
    }

    private fun resetTileDecoder(caller: String) {
        cleanTileManager("resetTileDecoder:$caller")
        cleanTileDecoder("resetTileDecoder:$caller")

        val imageSource = imageSource ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val ignoreExifOrientation = ignoreExifOrientation

        lastResetTileDecoderJob = coroutineScope.launch(Dispatchers.Main) {
            val imageInfoResult = imageSource.readImageInfo(ignoreExifOrientation)
            val imageInfo = imageInfoResult.getOrNull()
            this@SubsamplingState.imageInfo = imageInfo
            val canUseSubsamplingResult =
                imageInfo?.let { canUseSubsampling(it, contentSize.toCompat()) }
            if (imageInfo != null && canUseSubsamplingResult == 0) {
                logger.d {
                    "resetTileDecoder:$caller. success. " +
                            "contentSize=${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo=${imageInfo.toShortString()}. " +
                            "'${imageSource.key}'"
                }
                this@SubsamplingState.tileDecoder = TileDecoder(
                    logger = logger,
                    imageSource = imageSource,
                    tileBitmapPoolHelper = tileBitmapPoolHelper,
                    imageInfo = imageInfo,
                )
                resetTileManager(caller)
            } else {
                val cause = when {
                    imageInfo == null -> imageInfoResult.exceptionOrNull()!!.message
                    canUseSubsamplingResult == -1 -> "The content size is greater than or equal to the original image"
                    canUseSubsamplingResult == -2 -> "The content aspect ratio is different with the original image"
                    canUseSubsamplingResult == -3 -> "Image type not support subsampling"
                    else -> "Unknown canUseSubsamplingResult: $canUseSubsamplingResult"
                }
                val level = if (canUseSubsamplingResult == -1) Logger.DEBUG else Logger.ERROR
                val type = if (canUseSubsamplingResult == -1) "skipped" else "failed"
                logger.log(level) {
                    "resetTileDecoder:$caller. $type, $cause. " +
                            "contentSize: ${contentSize.toShortString()}, " +
                            "ignoreExifOrientation=${ignoreExifOrientation}. " +
                            "imageInfo: ${imageInfo?.toShortString()}. " +
                            "'${imageSource.key}'"
                }
            }
            lastResetTileDecoderJob = null
        }
    }

    private fun resetTileManager(caller: String) {
        cleanTileManager("resetTileManager:$caller")

        val imageSource = imageSource ?: return
        val tileDecoder = tileDecoder ?: return
        val imageInfo = imageInfo ?: return
        val containerSize = containerSize.takeIf { !it.isEmpty() } ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return

        val tileManager = TileManager(
            logger = logger,
            tileDecoder = tileDecoder,
            imageSource = imageSource,
            containerSize = containerSize.toCompat(),
            contentSize = contentSize.toCompat(),
            tileMemoryCacheHelper = tileMemoryCacheHelper,
            tileBitmapPoolHelper = tileBitmapPoolHelper,
            imageInfo = imageInfo,
            onTileChanged = { manager ->
                tileList = manager.tileList.map { tile ->
                    TileSnapshot(
                        tile.srcRect.toPlatform(),
                        tile.inSampleSize,
                        tile.bitmap,
                        tile.state
                    )
                }
            },
            onImageLoadRectChanged = {
                imageLoadRect = it.imageLoadRect.toPlatform()
            }
        )
        logger.d {
            val tileMaxSize = tileManager.tileMaxSize
            val tileMap = tileManager.tileMap
            val tileMapInfoList = tileMap.keys.sortedDescending()
                .map { "${it}:${tileMap[it]?.size}" }
            "resetTileManager:$caller. success. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "imageInfo=${imageInfo.toShortString()}. " +
                    "tileMaxSize=${tileMaxSize.toShortString()}, " +
                    "tileMap=$tileMapInfoList, " +
                    "'${imageSource.key}'"
        }
        this@SubsamplingState.tileManager = tileManager
        refreshReadyState()
    }

    private fun refreshTiles(
        contentVisibleRect: IntRect,
        scale: Float,
        rotation: Int,
        transforming: Boolean,
        caller: String,
    ) {
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        if (paused) {
            logger.d { "refreshTiles:$caller. interrupted, paused. '${imageSource.key}'" }
            tileManager.clean("refreshTiles:paused")
            return
        }
        tileManager.refreshTiles(
            contentVisibleRect = contentVisibleRect.toCompat(),
            scale = scale,
            rotation = rotation,
            transforming = transforming,
            caller = caller
        )
    }

    override fun onRemembered() {
    }

    override fun onForgotten() {
        destroy("onForgotten")
    }

    override fun onAbandoned() {
        destroy("onAbandoned")
    }

    private fun refreshReadyState() {
        ready = imageInfo != null && tileDecoder != null && tileManager != null
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
            refreshReadyState()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingState.tileManager
        if (tileManager != null) {
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingState.tileManager = null
            logger.d { "cleanTileManager:$caller. '${imageKey}'" }
            refreshReadyState()
        }
    }

    private fun destroy(caller: String) {
        cleanTileManager("destroy:$caller")
        cleanTileDecoder("destroy:$caller")
        unregisterLifecycleObserver()
    }

    private fun resetPaused(caller: String) {
        val lifecycleStarted = lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) != false
        val paused = !lifecycleStarted
        logger.d {
            "resetPaused:$caller. $paused. lifecycleStarted=$lifecycleStarted. '${imageKey}'"
        }
        this.paused = paused
    }

    private fun registerLifecycleObserver() {
        lifecycle?.addObserver(resetPausedLifecycleObserver)
    }

    private fun unregisterLifecycleObserver() {
        lifecycle?.removeObserver(resetPausedLifecycleObserver)
    }
}