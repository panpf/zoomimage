package com.github.panpf.zoomimage

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.Placeholder
import com.bumptech.glide.integration.compose.RequestBuilderTransform
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.subsampling.BindZoomableStateAndSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.SubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.rememberSubsamplingState
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.rememberZoomableState
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.compose.zoom.zoomable
import com.github.panpf.zoomimage.glide.internal.GlideTileBitmapPool
import com.github.panpf.zoomimage.glide.internal.GlideTileMemoryCache
import com.github.panpf.zoomimage.glide.internal.newGlideImageSource


@Composable
fun rememberGlideZoomAsyncImageLogger(
    tag: String = "GlideZoomAsyncImage",
    level: Int = Logger.INFO
): Logger = remember {
    Logger(tag = tag).apply { this.level = level }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    logger: Logger = rememberGlideZoomAsyncImageLogger(),
    zoomableState: ZoomableState = rememberZoomableState(logger),
    subsamplingState: SubsamplingState = rememberSubsamplingState(logger),
    scrollBarSpec: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
    // from glide: TODO(judds): Consider using separate GlideImage* methods instead of sealed classes.
    // See http://shortn/_x79pjkMZIH for an internal discussion.
    loading: Placeholder? = null,
    failure: Placeholder? = null,
    // from glide: TODO(judds): Consider defaulting to load the model here instead of always doing so below.
    requestBuilderTransform: RequestBuilderTransform<Drawable> = { it },
) {
    if (zoomableState.contentAlignment != alignment) {
        zoomableState.contentAlignment = alignment
    }
    if (zoomableState.contentScale != contentScale) {
        zoomableState.contentScale = contentScale
    }

    BindZoomableStateAndSubsamplingState(zoomableState, subsamplingState)

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val glide = Glide.get(context)
        subsamplingState.tileBitmapPool = GlideTileBitmapPool(glide)
        subsamplingState.tileMemoryCache = GlideTileMemoryCache(glide)
    }

    val baseTransform = zoomableState.baseTransform
    val userTransform = zoomableState.userTransform
    val modifier1 = modifier
        .clipToBounds()
        .let { if (scrollBarSpec != null) it.zoomScrollBar(zoomableState, scrollBarSpec) else it }
        .zoomable(state = zoomableState, onLongPress = onLongPress, onTap = onTap)
        .graphicsLayer {
            scaleX = userTransform.scaleX
            scaleY = userTransform.scaleY
            translationX = userTransform.offsetX
            translationY = userTransform.offsetY
            transformOrigin = userTransform.origin
        }
        .graphicsLayer {
            rotationZ = baseTransform.rotation
            transformOrigin = TransformOrigin.Center
        }
        .subsampling(zoomableState = zoomableState, subsamplingState = subsamplingState)

    // todo NoClip
    GlideImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier1,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        loading = loading,
        failure = failure,
        requestBuilderTransform = { requestBuilder ->
            requestBuilderTransform(requestBuilder)
            requestBuilder.addListener(
                ResetListener(
                    context = context,
                    logger = logger,
                    zoomableState = zoomableState,
                    subsamplingState = subsamplingState,
                    requestBuilder = requestBuilder,
                    model = model,
                )
            )
        },
    )
}

private class ResetListener(
    private val context: Context,
    private val logger: Logger,
    private val zoomableState: ZoomableState,
    private val subsamplingState: SubsamplingState,
    private val requestBuilder: RequestBuilder<Drawable>,
    private val model: Any?,
) : RequestListener<Drawable> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        logger.d("ResetListener. onLoadFailed. model: $model")
        reset(resource = null)
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        logger.d("ResetListener. onResourceReady. model: $model, resource: $resource")
        reset(resource = resource)
        return false
    }

    private fun reset(
        resource: Drawable?,
    ) {
        val drawableSize = resource
            ?.let { IntSize(it.intrinsicWidth, it.intrinsicHeight) }
            ?.takeIf { it.isNotEmpty() }
        val containerSize = zoomableState.containerSize
        val newContentSize = when {
            drawableSize != null -> drawableSize
            containerSize.isNotEmpty() -> containerSize
            else -> IntSize.Zero
        }
        if (zoomableState.contentSize != newContentSize) {
            zoomableState.contentSize = newContentSize
        }

        if (resource != null) {
            subsamplingState.disableMemoryCache = !requestBuilder.isMemoryCacheable
            val imageSource = newGlideImageSource(context, model)
            if (imageSource == null) {
                logger.w { "GlideZoomAsyncImage. Can't use Subsampling, unsupported model: '$model'" }
            }
            subsamplingState.setImageSource(imageSource)
        }
    }
}