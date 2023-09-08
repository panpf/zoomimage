/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import com.github.panpf.zoomimage.compose.ZoomState
import com.github.panpf.zoomimage.compose.internal.NoClipImage
import com.github.panpf.zoomimage.compose.internal.round
import com.github.panpf.zoomimage.compose.internal.toPx
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.compose.zoom.internal.NavigationBarHeightState
import com.github.panpf.zoomimage.compose.zoom.internal.UpdateContainerSizeResult
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.compose.zoom.zoomable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A native Image component that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * val state: ZoomState by rememberZoomState()
 * val context = LocalContext.current
 * LaunchedEffect(Unit) {
 *     val imageSource = ImageSource.fromResource(context, R.drawable.huge_image)
 *     state.subsampling.setImageSource(imageSource)
 * }
 * ZoomImage(
 *     painter = painterResource(R.drawable.huge_image_thumbnail),
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 *     state = state,
 * )
 * ```
 *
 * @param contentDescription text used by accessibility services to describe what this image
 * represents. This should always be provided unless this image is used for decorative purposes,
 * and does not represent a meaningful action that a user can take. This text should be
 * localized, such as by using [androidx.compose.ui.res.stringResource] or similar
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content (ex.
 * background)
 * @param alignment Optional alignment parameter used to place the [Painter] in the given
 * bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be used
 * if the bounds are a different size from the intrinsic size of the [Painter]
 * @param alpha Optional opacity to be applied to the [Painter] when it is rendered onscreen
 * the default renders the [Painter] completely opaque
 * @param colorFilter Optional colorFilter to apply for the [Painter] when it is rendered onscreen
 * @param state The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 */
@Composable
fun ZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    state: ZoomState = rememberZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) {
    state.zoomable.contentScale = contentScale
    state.zoomable.alignment = alignment
    state.zoomable.contentSize = painter.intrinsicSize.round()

    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier = modifier) {
        /*
         * Here use BoxWithConstraints and then actively set containerSize and call nowReset(),
         * In order to prepare the transform in advance, so that when the position of the image needs to be adjusted,
         * the position change will not be seen by the user
         */
        val maxWidthPx = maxWidth.toPx().roundToInt()
        val maxHeightPx = maxHeight.toPx().roundToInt()
        val newContainerSize = IntSize(maxWidthPx, maxHeightPx)
        val updateContainerSizeResult = remember { UpdateContainerSizeResult() }
        UpdateContainerSize(state.zoomable, newContainerSize, updateContainerSizeResult)
        if (updateContainerSizeResult.updated) {
            state.zoomable.nowReset(coroutineScope, "BoxWithConstraints")
        }

        val transform = state.zoomable.transform
        val modifier1 = Modifier
            .matchParentSize()
            .clipToBounds()
            .let { if (scrollBar != null) it.zoomScrollBar(state.zoomable, scrollBar) else it }
            .zoomable(state = state.zoomable, onLongPress = onLongPress, onTap = onTap)
            .graphicsLayer {
                scaleX = transform.scaleX
                scaleY = transform.scaleY
                translationX = transform.offsetX
                translationY = transform.offsetY
                transformOrigin = transform.scaleOrigin
            }
            .graphicsLayer {
                rotationZ = transform.rotation
                transformOrigin = transform.rotationOrigin
            }
            .subsampling(state.logger, state.subsampling)
        NoClipImage(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier1,
            alignment = Alignment.TopStart,
            contentScale = ContentScale.None,
            alpha = alpha,
            colorFilter = colorFilter
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun UpdateContainerSize(
    state: ZoomableState,
    newContainerSize: IntSize,
    result: UpdateContainerSizeResult
) {
    val density = LocalDensity.current
    val navigationBarHeightState = remember { NavigationBarHeightState() }
    val navigationBarsInsets = WindowInsets.navigationBarsIgnoringVisibility
    val oldContainerSize = state.containerSize
    result.updated = if (newContainerSize != oldContainerSize) {
        /*
         * In the model MIX4; ROM: 14.0.6.0; on Android 13, when the navigation bar is displayed, the following occurs:
         * 1. When the ZoomImageView is unlocked again after the screen is locked, the height of the ZoomImageView will first increase and then change back to normal, and the difference is exactly the height of the current navigation bar
         * 2. Due to the height of the ZoomImageView, the containerSize of the ZoomableEngine will also change
         * 3. This causes the ZoomableEngine's transform to be reset, so this needs to be blocked here
         */
        val newNavigationBarHeight = navigationBarsInsets.getBottom(density)
        if (newNavigationBarHeight != 0 && newNavigationBarHeight != navigationBarHeightState.navigationBarHeight) {
            navigationBarHeightState.navigationBarHeight = newNavigationBarHeight
        }
        val navigationBarHeight = navigationBarHeightState.navigationBarHeight
        val diffSize = IntSize(
            width = newContainerSize.width - oldContainerSize.width,
            height = newContainerSize.height - oldContainerSize.height
        )
        if (navigationBarHeight == 0 ||
            (abs(diffSize.width) != navigationBarHeight && abs(diffSize.height) != navigationBarHeight)
        ) {
            state.containerSize = newContainerSize
            true
        } else {
            false
        }
    } else {
        false
    }
}

private fun ZoomableState.nowReset(coroutineScope: CoroutineScope, caller: String) {
    coroutineScope.launch(Dispatchers.Main.immediate) {
        reset(caller)
    }
}