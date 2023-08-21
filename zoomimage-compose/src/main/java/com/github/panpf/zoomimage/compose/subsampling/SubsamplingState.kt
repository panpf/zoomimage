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
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
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

@Composable
fun rememberSubsamplingState(
    showTileBounds: Boolean = false,
    logger: Logger = rememberZoomImageLogger(),
): SubsamplingState {
    val subsamplingState = remember { SubsamplingState(logger) }
    subsamplingState.showTileBounds = showTileBounds
    return subsamplingState
}

@Stable
class SubsamplingState(logger: Logger) : RememberObserver {

    internal val logger: Logger = logger.newLogger(module = "SubsamplingState")
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    internal var tileMemoryCacheHelper = TileMemoryCacheHelper(logger)
    internal var tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var imageInfo: ImageInfo? by mutableStateOf(null)
    var showTileBounds: Boolean by mutableStateOf(false)    // todo 从这里移出

    var ignoreExifOrientation by mutableStateOf(false)  // todo 挪到 rememberSubsamplingState 中
    var tileMemoryCache: TileMemoryCache? by mutableStateOf(null)
    var disableMemoryCache: Boolean by mutableStateOf(false)
    var tileBitmapPool: TileBitmapPool? by mutableStateOf(null)
    var disallowReuseBitmap: Boolean by mutableStateOf(false)

    var imageKey: String? by mutableStateOf(null)
        private set
    var ready by mutableStateOf(false)
        private set
    var tileList: List<TileSnapshot> by mutableStateOf(emptyList())
        private set
    var imageLoadRect: IntRect by mutableStateOf(IntRect.Zero)
        private set
    var paused by mutableStateOf(false)

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

    fun resetTileDecoder(caller: String) {
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

    fun resetTileManager(caller: String) {
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

    fun refreshTiles(
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

    private fun destroy(caller: String) {
        cleanTileManager("destroy:$caller")
        cleanTileDecoder("destroy:$caller")
    }
}

@Composable
fun BindZoomableStateAndSubsamplingState(
    zoomableState: ZoomableState,
    subsamplingState: SubsamplingState
) {
    LaunchedEffect(Unit) {
        snapshotFlow { zoomableState.containerSize }.collect {
            // Changes in containerSize cause a large chain reaction that can cause large memory fluctuations.
            // Size animations cause frequent changes in containerSize, so a delayed reset avoids this problem
            if (it.isNotEmpty()) {
                delay(60)
            }
            subsamplingState.containerSize = it
            subsamplingState.resetTileManager("containerSizeChanged")
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { zoomableState.contentSize }.collect {
            subsamplingState.contentSize = it
            subsamplingState.resetTileDecoder("contentSizeChanged")
        }
    }

    val refreshTiles: (caller: String) -> Unit = { caller ->
        subsamplingState.refreshTiles(
            contentVisibleRect = zoomableState.contentVisibleRect,
            scale = zoomableState.transform.scaleX,
            rotation = zoomableState.transform.rotation.roundToInt(),
            transforming = zoomableState.transforming,
            caller = caller
        )
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.ready }.collect {
            val imageInfo = subsamplingState.imageInfo
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
        snapshotFlow { subsamplingState.paused }.collect {
            refreshTiles(if (it) "paused" else "resumed")
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.ignoreExifOrientation }.collect {
            subsamplingState.resetTileDecoder("ignoreExifOrientationChanged")
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.tileMemoryCache }.collect {
            subsamplingState.tileMemoryCacheHelper.tileMemoryCache = it
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.disableMemoryCache }.collect {
            subsamplingState.tileMemoryCacheHelper.disableMemoryCache = it
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.tileBitmapPool }.collect {
            subsamplingState.tileBitmapPoolHelper.tileBitmapPool = it
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.disallowReuseBitmap }.collect {
            subsamplingState.tileBitmapPoolHelper.disallowReuseBitmap = it
        }
    }
}