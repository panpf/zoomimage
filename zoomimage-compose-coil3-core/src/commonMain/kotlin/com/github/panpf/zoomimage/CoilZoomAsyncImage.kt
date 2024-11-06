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
import androidx.compose.runtime.NonRestartableComposable
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
import androidx.compose.ui.unit.IntSize
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePainter.State
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.github.panpf.zoomimage.coil.CoilTileImageCache
import com.github.panpf.zoomimage.compose.coil.internal.BaseZoomAsyncImage
import com.github.panpf.zoomimage.compose.coil.internal.ConstraintsSizeResolver
import com.github.panpf.zoomimage.compose.coil.internal.onStateOf
import com.github.panpf.zoomimage.compose.coil.internal.requestOf
import com.github.panpf.zoomimage.compose.coil.internal.toScale
import com.github.panpf.zoomimage.compose.coil.internal.transformOf
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.mouseZoom
import com.github.panpf.zoomimage.compose.zoom.zoom
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


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
    modifier = modifier,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    imageLoader = imageLoader,
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
    transform: (State) -> State = AsyncImagePainter.DefaultTransform,
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

    LaunchedEffect(zoomState.subsampling) {
        zoomState.subsampling.tileImageCache = CoilTileImageCache(imageLoader)
    }

    // moseZoom directly acts on ZoomAsyncImage, causing the zoom center to be abnormal.
    Box(modifier = modifier.mouseZoom(zoomState.zoomable)) {
        val context = LocalPlatformContext.current
        val coroutineScope = rememberCoroutineScope()
        val request = updateRequest(requestOf(model), contentScale)
        BaseZoomAsyncImage(
            model = request,
            contentDescription = contentDescription,
            imageLoader = imageLoader,
            transform = transform,
            onState = { loadState ->
                onState(context, coroutineScope, imageLoader, zoomState, request, loadState)
                onState?.invoke(loadState)
            },
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            modifier = Modifier
                .matchParentSize()
                .zoom(
                    zoomable = zoomState.zoomable,
                    userSetupContentSize = true,
                    onLongPress = onLongPress,
                    onTap = onTap
                )
                .subsampling(zoomState.zoomable, zoomState.subsampling),
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

@Composable
internal fun updateRequest(request: ImageRequest, contentScale: ContentScale): ImageRequest {
    return request.let {
        if (it.defined.sizeResolver == null) {
            val sizeResolver = remember { ConstraintsSizeResolver() }
            it.newBuilder().size(sizeResolver).build()
        } else {
            it
        }
    }.let {
        if (it.defined.scale == null) {
            it.newBuilder().scale(contentScale.toScale()).build()
        } else {
            it
        }
    }
}

private fun onState(
    context: PlatformContext,
    coroutineScope: CoroutineScope,
    imageLoader: ImageLoader,
    zoomState: CoilZoomState,
    request: ImageRequest,
    loadState: State,
) {
    zoomState.zoomable.logger.d {
        "CoilZoomAsyncImage. onState. state=${loadState.name}. data='${request.data}'"
    }
    val painterSize = loadState.painter
        ?.intrinsicSize
        ?.takeIf { it.isSpecified }
        ?.roundToIntSize()
        ?.takeIf { it.isNotEmpty() }
    zoomState.zoomable.contentSize = painterSize ?: IntSize.Zero

    if (loadState is State.Success) {
        coroutineScope.launch {
            val generateResult = zoomState.subsamplingImageGenerators.firstNotNullOfOrNull {
                it.generateImage(context, imageLoader, loadState.result, loadState.painter)
            }
            if (generateResult is SubsamplingImageGenerateResult.Error) {
                zoomState.subsampling.logger.d {
                    "CoilZoomAsyncImage. ${generateResult.message}. data='${request.data}'"
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

private val State.name: String
    get() = when (this) {
        is State.Loading -> "Loading"
        is State.Success -> "Success"
        is State.Error -> "Error"
        is State.Empty -> "Empty"
    }

private fun Size.roundToIntSize(): IntSize {
    return IntSize(width.roundToInt(), height.roundToInt())
}

private fun IntSize.isNotEmpty(): Boolean = width > 0 && height > 0