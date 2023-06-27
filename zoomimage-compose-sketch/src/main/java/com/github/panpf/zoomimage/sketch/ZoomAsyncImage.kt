package com.github.panpf.zoomimage.sketch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.panpf.sketch.compose.AsyncImage
import com.github.panpf.sketch.compose.AsyncImagePainter
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.UriInvalidException
import com.github.panpf.zoomimage.AnimationConfig
import com.github.panpf.zoomimage.ZoomableState
import com.github.panpf.zoomimage.rememberZoomableState
import com.github.panpf.zoomimage.zoomable

@Composable
@NonRestartableComposable
fun ZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) = ZoomAsyncImage(
    request = DisplayRequest(LocalContext.current, imageUri),
    contentDescription = contentDescription,
    modifier = modifier,
    state = state,
    animationConfig = animationConfig,
    transform = transformOf(placeholder, error, uriEmpty),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
)

@Composable
@NonRestartableComposable
fun ZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) = ZoomAsyncImage(
    request = request,
    contentDescription = contentDescription,
    modifier = modifier,
    state = state,
    animationConfig = animationConfig,
    transform = transformOf(placeholder, error, uriEmpty),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
)

@Composable
@NonRestartableComposable
fun ZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) = ZoomAsyncImage(
    request = DisplayRequest(LocalContext.current, imageUri),
    contentDescription = contentDescription,
    modifier = modifier,
    state = state,
    animationConfig = animationConfig,
    transform = transform,
    onState = onState,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
)

@Composable
fun ZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    animationConfig: AnimationConfig = AnimationConfig(),
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    if (state.contentAlignment != alignment) {
        state.contentAlignment = alignment
    }
    if (state.contentScale != contentScale) {
        state.contentScale = contentScale
    }

    val modifier1 = modifier
        .zoomable(state, animationConfig)
        .clipToBounds()
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            rotationZ = state.rotation
            translationX = state.translation.x
            translationY = state.translation.y
            transformOrigin = state.transformOrigin
        }

    AsyncImage(
        request = request,
        contentDescription = contentDescription,
        modifier = modifier1,
        transform = transform,
        onState = {
            val painterSize = it.painter?.intrinsicSize
            if (painterSize != null && state.contentSize != painterSize) {
                state.contentSize = painterSize
            }
            onState?.invoke(it)
        },
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )

    // todo support subsampling

    // todo support scroll bar
}

@Stable
private fun transformOf(
    placeholder: Painter?,
    error: Painter?,
    uriEmpty: Painter?,
): (AsyncImagePainter.State) -> AsyncImagePainter.State {
    return if (placeholder != null || error != null || uriEmpty != null) {
        { state ->
            when (state) {
                is AsyncImagePainter.State.Loading -> {
                    if (placeholder != null) state.copy(painter = placeholder) else state
                }
//                is State.Error -> if (state.result.throwable is NullRequestDataException) {
                is AsyncImagePainter.State.Error -> if (state.result.throwable is UriInvalidException) {
                    if (uriEmpty != null) state.copy(painter = uriEmpty) else state
                } else {
                    if (error != null) state.copy(painter = error) else state
                }

                else -> state
            }
        }
    } else {
        AsyncImagePainter.DefaultTransform
    }
}

@Stable
private fun onStateOf(
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)?,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)?,
    onError: ((AsyncImagePainter.State.Error) -> Unit)?,
): ((AsyncImagePainter.State) -> Unit)? {
    return if (onLoading != null || onSuccess != null || onError != null) {
        { state ->
            when (state) {
                is AsyncImagePainter.State.Loading -> onLoading?.invoke(state)
                is AsyncImagePainter.State.Success -> onSuccess?.invoke(state)
                is AsyncImagePainter.State.Error -> onError?.invoke(state)
                is AsyncImagePainter.State.Empty -> {}
            }
        }
    } else {
        null
    }
}