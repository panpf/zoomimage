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
import com.github.panpf.sketch.compose.AsyncImagePainter
import com.github.panpf.sketch.compose.AsyncImageState
import com.github.panpf.sketch.compose.PainterState
import com.github.panpf.sketch.compose.rememberAsyncImageState
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.sketch
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec

/**
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     imageUri = "https://sample.com/sample.jpeg",
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param imageUri [DisplayRequest.uriString] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param uriEmpty A [Painter] that is displayed when the request's [DisplayRequest.uriString] is empty.
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
 * @see com.github.panpf.zoomimage.compose.sketch3.test.SingletonSketchZoomAsyncImageTest.testSketchZoomAsyncImage1
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(),
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((PainterState.Loading) -> Unit)? = null,
    onSuccess: ((PainterState.Success) -> Unit)? = null,
    onError: ((PainterState.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: SketchZoomState = rememberSketchZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    imageUri = imageUri,
    contentDescription = contentDescription,
    sketch = LocalContext.current.sketch,
    modifier = modifier,
    state = state,
    placeholder = placeholder,
    error = error,
    uriEmpty = uriEmpty,
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
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     request = DisplayRequest(LocalContext.current, "https://sample.com/sample.jpeg") {
 *         placeholder(R.drawable.placeholder)
 *         crossfade()
 *     },
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param request [DisplayRequest].
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param uriEmpty A [Painter] that is displayed when the request's [DisplayRequest.uriString] is null.
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
 * @see com.github.panpf.zoomimage.compose.sketch3.test.SingletonSketchZoomAsyncImageTest.testSketchZoomAsyncImage2
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(),
    placeholder: Painter? = null,
    error: Painter? = null,
    uriEmpty: Painter? = error,
    onLoading: ((PainterState.Loading) -> Unit)? = null,
    onSuccess: ((PainterState.Success) -> Unit)? = null,
    onError: ((PainterState.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: SketchZoomState = rememberSketchZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    request = request,
    contentDescription = contentDescription,
    sketch = LocalContext.current.sketch,
    modifier = modifier,
    state = state,
    placeholder = placeholder,
    error = error,
    uriEmpty = uriEmpty,
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
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     imageUri = "https://sample.com/sample.jpeg",
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param imageUri [DisplayRequest.uriString] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [PainterState] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onPainterState Called when the painterState changes.
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
 * @see com.github.panpf.zoomimage.compose.sketch3.test.SingletonSketchZoomAsyncImageTest.testSketchZoomAsyncImage3
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(),
    transform: (PainterState) -> PainterState = AsyncImageState.DefaultTransform,
    onPainterState: ((PainterState) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: SketchZoomState = rememberSketchZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    imageUri = imageUri,
    contentDescription = contentDescription,
    sketch = LocalContext.current.sketch,
    modifier = modifier,
    state = state,
    transform = transform,
    onPainterState = onPainterState,
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
 * An image component that integrates the Sketch image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * SketchZoomAsyncImage(
 *     request = DisplayRequest(LocalContext.current, "https://sample.com/sample.jpeg") {
 *         placeholder(R.drawable.placeholder)
 *         crossfade()
 *     },
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 * )
 * ```
 *
 * @param request [DisplayRequest].
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [PainterState] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onPainterState Called when the painterState changes.
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
 * @see com.github.panpf.zoomimage.compose.sketch3.test.SingletonSketchZoomAsyncImageTest.testSketchZoomAsyncImage4
 */
@Composable
@NonRestartableComposable
fun SketchZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(),
    transform: (PainterState) -> PainterState = AsyncImageState.DefaultTransform,
    onPainterState: ((PainterState) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    zoomState: SketchZoomState = rememberSketchZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = SketchZoomAsyncImage(
    request = request,
    contentDescription = contentDescription,
    sketch = LocalContext.current.sketch,
    modifier = modifier,
    state = state,
    transform = transform,
    onPainterState = onPainterState,
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