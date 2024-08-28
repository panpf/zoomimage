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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State
import coil.imageLoader
import coil.request.ImageRequest
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec

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
 * @see com.github.panpf.zoomimage.compose.coil2.test.SingletonCoil2ZoomAsyncImageTest.testCoilZoomAsyncImage1
 */
@Composable
@NonRestartableComposable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
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
    imageLoader = LocalContext.current.imageLoader,
    modifier = modifier,
    placeholder = placeholder,
    error = error,
    fallback = fallback,
    onLoading = onLoading,
    onSuccess = onSuccess,
    onError = onError,
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
 * @see com.github.panpf.zoomimage.compose.coil2.test.SingletonCoil2ZoomAsyncImageTest.testCoilZoomAsyncImage2
 */
@Composable
fun CoilZoomAsyncImage(
    model: Any?,
    contentDescription: String?,
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
) = CoilZoomAsyncImage(
    model = model,
    contentDescription = contentDescription,
    imageLoader = LocalContext.current.imageLoader,
    modifier = modifier,
    transform = transform,
    onState = onState,
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