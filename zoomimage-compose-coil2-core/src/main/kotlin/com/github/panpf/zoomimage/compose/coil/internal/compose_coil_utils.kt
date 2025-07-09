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

package com.github.panpf.zoomimage.compose.coil.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State
import coil.compose.EqualityDelegate
import coil.request.ImageRequest
import coil.request.NullRequestDataException
import coil.size.Dimension
import coil.size.SizeResolver

@Stable
internal fun transformOf(
    placeholder: Painter?,
    error: Painter?,
    uriEmpty: Painter?,
): (State) -> State {
    return if (placeholder != null || error != null || uriEmpty != null) {
        { state ->
            when (state) {
                is State.Loading -> {
                    if (placeholder != null) state.copy(painter = placeholder) else state
                }

                is State.Error -> if (state.result.throwable is NullRequestDataException) {
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
internal fun onStateOf(
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

/** Wrap [AsyncImage]'s unstable arguments to make them stable. */
@Stable
internal class AsyncImageState(
    val model: Any?,
    val modelEqualityDelegate: EqualityDelegate,
    val imageLoader: ImageLoader,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is AsyncImageState &&
                modelEqualityDelegate.equals(model, other.model) &&
                imageLoader == other.imageLoader
    }

    override fun hashCode(): Int {
        var result = modelEqualityDelegate.hashCode(model)
        result = 31 * result + imageLoader.hashCode()
        return result
    }
}

/** Create an [ImageRequest] with a not-null [SizeResolver] from the [model]. */
@Composable
@NonRestartableComposable
internal fun requestOfWithSizeResolver(
    model: Any?,
    contentScale: ContentScale,
): ImageRequest {
    if (model is ImageRequest && model.defined.sizeResolver != null) {
        return model
    }

    val sizeResolver = if (contentScale == ContentScale.None) {
        OriginalSizeResolver
    } else {
        remember { ConstraintsSizeResolver() }
    }

    if (model is ImageRequest) {
        return remember(model, sizeResolver) {
            model.newBuilder()
                .size(sizeResolver)
                .build()
        }
    } else {
        val context = LocalContext.current
        return remember(context, model, sizeResolver) {
            ImageRequest.Builder(context)
                .data(model)
                .size(sizeResolver)
                .build()
        }
    }
}

@Stable
internal fun Constraints.toSizeOrNull(): coil.size.Size? {
    if (isZero) {
        return null
    } else {
        val width = if (hasBoundedWidth) Dimension(maxWidth) else Dimension.Undefined
        val height = if (hasBoundedHeight) Dimension(maxHeight) else Dimension.Undefined
        return coil.size.Size(width, height)
    }
}

internal val ZeroConstraints = Constraints.fixed(0, 0)

internal val OriginalSizeResolver = SizeResolver(coil.size.Size.ORIGINAL)

internal fun Modifier.bindConstraintsSizeResolver(sizeResolver: SizeResolver?): Modifier {
    return if (sizeResolver != null && sizeResolver is ConstraintsSizeResolver) {
        this.onSizeChanged { size ->
            val constraints = Constraints(maxWidth = size.width, maxHeight = size.height)
            sizeResolver.setConstraints(constraints)
        }
    } else {
        this
    }
}

internal fun computeIntrinsicSize(
    start: Painter?,
    end: Painter?,
    preferExactIntrinsicSize: Boolean = false
): Size {
    val startSize = start?.intrinsicSize ?: Size.Zero
    val endSize = end?.intrinsicSize ?: Size.Zero

    val isStartSpecified = startSize.isSpecified
    val isEndSpecified = endSize.isSpecified
    if (isStartSpecified && isEndSpecified) {
        return Size(
            width = maxOf(startSize.width, endSize.width),
            height = maxOf(startSize.height, endSize.height),
        )
    }
    if (preferExactIntrinsicSize) {
        if (isStartSpecified) return startSize
        if (isEndSpecified) return endSize
    }
    return Size.Unspecified
}