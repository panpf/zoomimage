/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 * Copyright 2023 Coil Contributors
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

package com.github.panpf.zoomimage

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.roundToIntSize
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePainter.Companion.DefaultTransform
import coil3.compose.AsyncImagePainter.State
import coil3.compose.ConstraintsSizeResolver
import coil3.compose.CrossfadePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.NullRequestDataException
import coil3.request.crossfadeMillis
import coil3.size.SizeResolver
import com.github.panpf.zoomimage.coil.CoilTileImageCache
import com.github.panpf.zoomimage.compose.internal.BaseZoomImage
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.mouseZoom
import com.github.panpf.zoomimage.compose.zoom.zoom
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.compose.zoom.zooming
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * An image component that integrates the Coil image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * CoilZoomAsyncImage(
 *     model = ImageRequest.Builder(LocalContext.current).apply {
 *         data("https://sample.com/sample.jpeg")
 *         placeholder(R.drawable.placeholder)
 *         crossfade(true)
 *     }.build(),
 *     contentDescription = "view image",
 *     imageLoader = context.imageLoader,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param fallback A [Painter] that is displayed when the request's [ImageRequest.data] is null.
 * @param onLoading Called when the image request begins loading.
 * @param onSuccess Called when the image request completes successfully.
 * @param onError Called when the image request completes unsuccessfully.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param zoomState The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 * @see com.github.panpf.zoomimage.compose.coil3.core.test.CoilZoomAsyncImageTest.testCoilZoomAsyncImage1
 */
@Composable
@NonRestartableComposable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: CoilZoomState = rememberCoilZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = CoilZoomAsyncImage(
    model = model,
    contentDescription = contentDescription,
    imageLoader = imageLoader,
    modifier = modifier,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    zoomState = zoomState,
    scrollBar = scrollBar,
    onLongPress = onLongPress,
    onTap = onTap,
)

/**
 * An image component that integrates the Coil image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * CoilZoomAsyncImage(
 *     model = ImageRequest.Builder(LocalContext.current).apply {
 *         data("https://sample.com/sample.jpeg")
 *         placeholder(R.drawable.placeholder)
 *         crossfade(true)
 *     }.build(),
 *     contentDescription = "view image",
 *     imageLoader = context.imageLoader,
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [State] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onState Called when the state of this painter changes.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param zoomState The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 * @see com.github.panpf.zoomimage.compose.coil3.core.test.CoilZoomAsyncImageTest.testCoilZoomAsyncImage2
 */
@Composable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: CoilZoomState = rememberCoilZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    zoomState.zoomable.contentScale = contentScale
    zoomState.zoomable.alignment = alignment
    zoomState.zoomable.layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(zoomState.subsampling) {
        zoomState.subsampling.tileImageCache = CoilTileImageCache(imageLoader)
    }

    val loadingPainterState: MutableState<Painter?> = remember { mutableStateOf(null) }

    // moseZoom directly acts on ZoomAsyncImage, causing the zoom center to be abnormal.
    Box(modifier = modifier.mouseZoom(zoomState.zoomable)) {
        val context = LocalPlatformContext.current
        val coroutineScope = rememberCoroutineScope()
        val request = requestOfWithSizeResolver(model, contentScale)
        val painter = rememberAsyncImagePainter(
            model = request,
            imageLoader = imageLoader,
            transform = transform,
            contentScale = contentScale,
            filterQuality = filterQuality,
            onState = { loadState ->
                onState(
                    context = context,
                    coroutineScope = coroutineScope,
                    imageLoader = imageLoader,
                    zoomState = zoomState,
                    model = model,
                    loadState = loadState,
                    loadingPainterState = loadingPainterState,
                )
                onState?.invoke(loadState)
            }
        )

        BaseZoomImage(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            clipToBounds = false,
            keepContentNoneStartOnDraw = true,
            modifier = Modifier
                .matchParentSize()
                .let {
                    val sizeResolver = request.defined.sizeResolver
                    if (sizeResolver is ConstraintsSizeResolver) {
                        it.onSizeChanged { size ->
                            sizeResolver.setConstraints(
                                Constraints(maxWidth = size.width, maxHeight = size.height)
                            )
                        }
                    } else {
                        it
                    }
                }.zoom(
                    zoomable = zoomState.zoomable,
                    userSetupContentSize = true,
                    onLongPress = onLongPress,
                    onTap = onTap
                ),
        )

        Box(
            Modifier
                .matchParentSize()
                .zooming(zoomable = zoomState.zoomable)
                .subsampling(zoomState.zoomable, zoomState.subsampling)
        )

        if (scrollBar != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .zoomScrollBar(zoomState.zoomable, scrollBar)
            )
        }
    }
}

private fun onState(
    context: PlatformContext,
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    zoomState: CoilZoomState,
    model: Any?,
    loadState: State,
    loadingPainterState: MutableState<Painter?>,
) {
    val finaData = if (model is ImageRequest) model.data else model
    zoomState.zoomable.logger.d {
        val stateName = when (loadState) {
            is State.Loading -> "Loading"
            is State.Success -> "Success"
            is State.Error -> "Error"
            is State.Empty -> "Empty"
        }
        "CoilZoomAsyncImage. onState. state=$stateName. data='${finaData}'"
    }

    val contentSize =
        buildContentSizeWithCrossfade(model, loadState, loadingPainterState).roundToIntSize()
    zoomState.zoomable.contentSize = contentSize

    if (loadState is State.Success) {
        coroutineScope.launch {
            val generateResult = zoomState.subsamplingImageGenerators.firstNotNullOfOrNull {
                it.generateImage(context, imageLoader, loadState.result, loadState.painter)
            }
            if (generateResult is SubsamplingImageGenerateResult.Error) {
                zoomState.subsampling.logger.d {
                    "CoilZoomAsyncImage. ${generateResult.message}. data='${finaData}'"
                }
            }
            if (generateResult is SubsamplingImageGenerateResult.Success) {
                zoomState.setSubsamplingImage(generateResult.subsamplingImage)
            } else {
                zoomState.setSubsamplingImage(null as SubsamplingImage?)
            }
        }
    } else {
        zoomState.setSubsamplingImage(null as SubsamplingImage?)
    }
}

/**
 * If crossfade mode is enabled, the coil will create a CrossfadePainter to display the image with the placeholder and the final image, but it is not CrossfadePainter in State.Success.
 * If the size of the placeholder at this time is larger than the size of the final image, we should use the size of the CrossfadePainter as the content size, so here we simulate a CrossfadePainter.
 */
private fun buildContentSizeWithCrossfade(
    model: Any?,
    loadState: State,
    loadingPainterState: MutableState<Painter?>,
): Size {
    val painter = loadState.painter
    val painterSize = painter
        ?.intrinsicSize
        ?.takeIf { it.isSpecified }
        ?.takeIf { it.width > 0 && it.height > 0 }
        ?: Size.Zero

    if (painter == null || painter is CrossfadePainter) {
        loadingPainterState.value = null
        return painterSize
    }

    if (model !is ImageRequest || model.crossfadeMillis <= 0) {
        loadingPainterState.value = null
        return painterSize
    }

    if (loadState is State.Loading) {
        loadingPainterState.value = painter
        return painterSize
    }

    if (loadState !is State.Success) {
        loadingPainterState.value = null
        return painterSize
    }

    val loadingPainter = loadingPainterState.value
    if (loadingPainter == null) {
        return painterSize
    }

    val crossfadePainterSize = CrossfadePainter(start = loadingPainter, end = painter).intrinsicSize
    loadingPainterState.value = null
    return crossfadePainterSize
}

@Stable
private fun transformOf(
    placeholder: Painter?,
    error: Painter?,
    fallback: Painter?,
): (State) -> State {
    return if (placeholder != null || error != null || fallback != null) {
        { state ->
            when (state) {
                is State.Loading -> {
                    if (placeholder != null) state.copy(painter = placeholder) else state
                }

                is State.Error -> if (state.result.throwable is NullRequestDataException) {
                    if (fallback != null) state.copy(painter = fallback) else state
                } else {
                    if (error != null) state.copy(painter = error) else state
                }

                else -> state
            }
        }
    } else {
        DefaultTransform
    }
}

@Stable
private fun onStateOf(
    onLoading: ((State.Loading) -> Unit)?,
    onSuccess: ((State.Success) -> Unit)?,
    onError: ((State.Error) -> Unit)?,
): ((State) -> Unit)? {
    return if (onLoading != null || onSuccess != null || onError != null) {
        { state ->
            when (state) {
                is State.Loading -> onLoading?.invoke(state)
                is State.Success -> onSuccess?.invoke(state)
                is State.Error -> onError?.invoke(state)
                is State.Empty -> {}
            }
        }
    } else {
        null
    }
}


/** Create an [ImageRequest] with a not-null [SizeResolver] from the [model]. */
@Composable
@NonRestartableComposable
internal fun requestOfWithSizeResolver(
    model: Any?,
    contentScale: ContentScale,
): ImageRequest {
    if (model is ImageRequest) {
        if (model.defined.sizeResolver != null) {
            return model
        } else {
            val sizeResolver = rememberSizeResolver(contentScale)
            return remember(model, sizeResolver) {
                model.newBuilder()
                    .size(sizeResolver)
                    .build()
            }
        }
    } else {
        val context = LocalPlatformContext.current
        val sizeResolver = rememberSizeResolver(contentScale)
        return remember(context, model, sizeResolver) {
            ImageRequest.Builder(context)
                .data(model)
                .size(sizeResolver)
                .build()
        }
    }
}

@Composable
private fun rememberSizeResolver(contentScale: ContentScale): SizeResolver {
    val isNone = contentScale == ContentScale.None
    return remember(isNone) {
        if (isNone) {
            SizeResolver.ORIGINAL
        } else {
            ConstraintsSizeResolver()
        }
    }
}