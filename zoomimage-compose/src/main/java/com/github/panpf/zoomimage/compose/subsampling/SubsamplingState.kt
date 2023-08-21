package com.github.panpf.zoomimage.compose.subsampling

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.toCompat
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.zoom.Transform
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.subsampling.ImageInfo
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.subsampling.Tile
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import com.github.panpf.zoomimage.subsampling.TileDecoder
import com.github.panpf.zoomimage.subsampling.TileManager
import com.github.panpf.zoomimage.subsampling.TileMemoryCache
import com.github.panpf.zoomimage.subsampling.internal.TileBitmapPoolHelper
import com.github.panpf.zoomimage.subsampling.internal.TileMemoryCacheHelper
import com.github.panpf.zoomimage.subsampling.internal.canUseSubsampling
import com.github.panpf.zoomimage.subsampling.internal.readImageInfo
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.toShortString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun rememberSubsamplingState(
    logger: Logger,
    showTileBounds: Boolean = false,
): SubsamplingState {
    val subsamplingState = remember { SubsamplingState(logger) }
    subsamplingState.showTileBounds = showTileBounds
    return subsamplingState
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
        if (!zoomableState.transforming && zoomableState.transform.rotation.roundToInt() % 90 == 0) {
            subsamplingState.refreshTiles(
                displayScale = zoomableState.transform.scaleX,
                displayMinScale = zoomableState.minScale,
                contentVisibleRect = zoomableState.contentVisibleRect,
                caller = caller
            )
        } else {
            subsamplingState.resetVisibleAndLoadRect(
                contentVisibleRect = zoomableState.contentVisibleRect,
                caller = caller
            )
        }
    }
    LaunchedEffect(Unit) {
        snapshotFlow { subsamplingState.ready }.collect {
            val imageInfo = subsamplingState.imageInfo
            zoomableState.contentOriginSize = if (it && imageInfo != null) {
                IntSize(imageInfo.width, imageInfo.height)
            } else {
                IntSize.Zero
            }
            refreshTiles("ready")
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
}

class SubsamplingState(logger: Logger) : RememberObserver {

    private var logger: Logger = logger.newLogger(module = "SubsamplingState")
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tileMemoryCacheHelper = TileMemoryCacheHelper(logger)
    private var tileBitmapPoolHelper = TileBitmapPoolHelper(logger)
    private var imageSource: ImageSource? = null
    private var tileManager: TileManager? = null
    private var tileDecoder: TileDecoder? = null
    private var lastResetTileDecoderJob: Job? = null
    private var lastDisplayScale: Float? = null
    private var lastDisplayMinScale: Float? = null
    private var lastContentVisibleRect: IntRect? = null

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var imageInfo: ImageInfo? by mutableStateOf(null)
    var showTileBounds: Boolean by mutableStateOf(false)

    var ignoreExifOrientation: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                resetTileDecoder("ignoreExifOrientationChanged")
            }
        }
    var tileMemoryCache: TileMemoryCache?
        get() = tileMemoryCacheHelper.tileMemoryCache
        set(value) {
            tileMemoryCacheHelper.tileMemoryCache = value
        }
    var disableMemoryCache: Boolean
        get() = tileMemoryCacheHelper.disableMemoryCache
        set(value) {
            tileMemoryCacheHelper.disableMemoryCache = value
        }
    var tileBitmapPool: TileBitmapPool?
        get() = tileBitmapPoolHelper.tileBitmapPool
        set(value) {
            tileBitmapPoolHelper.tileBitmapPool = value
        }
    var disallowReuseBitmap: Boolean
        get() = tileBitmapPoolHelper.disallowReuseBitmap
        set(value) {
            tileBitmapPoolHelper.disallowReuseBitmap = value
        }
    var paused = false
        set(value) {
            if (field != value) {
                field = value
                if (ready) {
                    if (value) {
                        imageSource?.run { logger.d { "pause. '$key'" } }
                        tileManager?.clean("paused")
                    } else {
                        imageSource?.run { logger.d { "resume. '$key'" } }
                        refreshTiles("resume")
                    }
                }
            }
        }

    var ready by mutableStateOf(false)
        private set
    var tilesChanged by mutableStateOf(0)
        private set
    val tileList: List<Tile>
        get() = tileManager?.tileList ?: emptyList()
    val imageLoadRect: IntRectCompat
        get() = tileManager?.imageLoadRect ?: IntRectCompat.Zero
    val imageVisibleRect: IntRectCompat
        get() = tileManager?.imageVisibleRect ?: IntRectCompat.Zero

    fun setImageSource(imageSource: ImageSource?): Boolean {
        if (this.imageSource == imageSource) return false
        logger.d { "setImageSource. '${imageSource?.key}'" }
        cleanTileManager("setImageSource")
        cleanTileDecoder("setImageSource")
        this.imageSource = imageSource
        resetTileDecoder("setImageSource")
        return true
    }

    private fun notifyTileChange() {
        if (tilesChanged < Int.MAX_VALUE) {
            tilesChanged++
        } else {
            tilesChanged = 0
        }
    }

    private fun notifyReadyChange() {
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
            logger.d { "cleanTileDecoder:$caller. '${imageSource?.key}'" }
            notifyReadyChange()
        }
        imageInfo = null
    }

    private fun cleanTileManager(caller: String) {
        val tileManager = this@SubsamplingState.tileManager
        if (tileManager != null) {
            tileManager.clean("cleanTileManager:$caller")
            this@SubsamplingState.tileManager = null
            logger.d { "cleanTileManager:$caller. '${imageSource?.key}'" }
            notifyReadyChange()
            notifyTileChange()
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
            onTileChanged = { notifyTileChange() }
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
        notifyReadyChange()
        notifyTileChange()
    }

    fun resetVisibleAndLoadRect(contentVisibleRect: IntRect, caller: String) {
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        tileManager.resetVisibleAndLoadRect(contentVisibleRect.toCompat())
        val imageVisibleRect = tileManager.imageVisibleRect
        val imageLoadRect = tileManager.imageLoadRect
        logger.d {
            "resetVisibleAndLoadRect:$caller. " +
                    "contentVisibleRect=${contentVisibleRect.toShortString()}. " +
                    "imageVisibleRect=${imageVisibleRect.toShortString()}, " +
                    "imageLoadRect=${imageLoadRect.toShortString()}. " +
                    "'${imageSource.key}'"
        }
    }

    fun refreshTiles(
        displayScale: Float,
        displayMinScale: Float,
        contentVisibleRect: IntRect,
        caller: String,
    ) {
        this.lastDisplayScale = displayScale
        this.lastDisplayMinScale = displayMinScale
        this.lastContentVisibleRect = contentVisibleRect
        val imageSource = imageSource ?: return
        val tileManager = tileManager ?: return
        if (paused) {
            logger.d { "refreshTiles:$caller. interrupted, paused. '${imageSource.key}'" }
            return
        }
        if (contentVisibleRect.isEmpty) {
            logger.d {
                "refreshTiles:$caller. interrupted, contentVisibleRect is empty. " +
                        "contentVisibleRect=${contentVisibleRect}. '${imageSource.key}'"
            }
            tileManager.clean("refreshTiles:contentVisibleRectEmpty")
            return
        }
        if (displayScale.format(2) <= 1.0f) {
            logger.d { "refreshTiles:$caller. interrupted, zoom is less than or equal to 1f. '${imageSource.key}'" }
            tileManager.clean("refreshTiles:scale1f")
            return
        }
        tileManager.refreshTiles(
            contentVisibleRect = contentVisibleRect.toCompat(),
            scale = displayScale,
            caller = caller
        )
    }

    fun drawTiles(drawScope: DrawScope, baseTransform: Transform?) {
        val imageSource = imageSource ?: return
        val imageInfo = imageInfo ?: return
        val tileList = tileList.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { !it.isEmpty() } ?: return
        val imageLoadRect = imageLoadRect.takeIf { !it.isEmpty } ?: return
        val widthScale: Float
        val heightScale: Float
        if (baseTransform != null) {
            widthScale = imageInfo.width / (contentSize.width * baseTransform.scaleX)
            heightScale = imageInfo.height / (contentSize.height * baseTransform.scaleY)
        } else {
            widthScale = imageInfo.width / (contentSize.width.toFloat())
            heightScale = imageInfo.height / (contentSize.height.toFloat())
        }
        var insideLoadCount = 0
        var outsideLoadCount = 0
        var realDrawCount = 0
        tileList.forEach { tile ->
            if (tile.srcRect.overlaps(imageLoadRect)) {
                insideLoadCount++
                val tileBitmap = tile.bitmap
                val tileSrcRect = tile.srcRect
                val tileDrawRect = IntRect(
                    left = floor(tileSrcRect.left / widthScale).toInt(),
                    top = floor(tileSrcRect.top / heightScale).toInt(),
                    right = floor(tileSrcRect.right / widthScale).toInt(),
                    bottom = floor(tileSrcRect.bottom / heightScale).toInt()
                ).let {
                    if (baseTransform != null) {
                        it.translate(baseTransform.offset.round())
                    } else {
                        it
                    }
                }
                if (tileBitmap != null) {
                    realDrawCount++
                    val srcRect = IntRect(0, 0, tileBitmap.width, tileBitmap.height)
                    drawScope.drawImage(
                        image = tileBitmap.asImageBitmap(),
                        srcOffset = srcRect.topLeft,
                        srcSize = srcRect.size,
                        dstOffset = tileDrawRect.topLeft,
                        dstSize = tileDrawRect.size,
//                        alpha = 0.5f,
                    )
                }

                if (showTileBounds) {
                    val boundsColor = when {
                        tileBitmap != null -> Color.Green
                        tile.loadJob?.isActive == true -> Color.Yellow
                        else -> Color.Red
                    }
                    val boundsStrokeWidth = 1f * Resources.getSystem().displayMetrics.density
                    val boundsStrokeHalfWidth = boundsStrokeWidth / 2
                    val tileBoundsRect = Rect(
                        left = floor(tileDrawRect.left + boundsStrokeHalfWidth),
                        top = floor(tileDrawRect.top + boundsStrokeHalfWidth),
                        right = ceil(tileDrawRect.right - boundsStrokeHalfWidth),
                        bottom = ceil(tileDrawRect.bottom - boundsStrokeHalfWidth)
                    )
                    drawScope.drawRect(
                        color = boundsColor,
                        topLeft = tileBoundsRect.topLeft,
                        size = tileBoundsRect.size,
                        style = Stroke(width = boundsStrokeWidth),
                        alpha = 0.5f,
                    )
                }
            } else {
                outsideLoadCount++
            }
        }
        logger.d {
            "drawTiles. tiles=${tileList.size}, " +
                    "insideLoadCount=${insideLoadCount}, " +
                    "outsideLoadCount=${outsideLoadCount}, " +
                    "realDrawCount=${realDrawCount}. " +
                    "'${imageSource.key}'"
        }
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

    private fun refreshTiles(@Suppress("SameParameterValue") caller: String) {
        val displayScale = lastDisplayScale
        val lastDisplayMinScale = lastDisplayMinScale
        val drawableVisibleRect = lastContentVisibleRect
        if (displayScale != null && lastDisplayMinScale != null && drawableVisibleRect != null) {
            refreshTiles(
                displayScale = displayScale,
                displayMinScale = lastDisplayMinScale,
                contentVisibleRect = drawableVisibleRect,
                caller = caller
            )
        }
    }
}