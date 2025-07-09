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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImageModelEqualityDelegate
import coil3.compose.AsyncImagePainter.Companion.DefaultTransform
import coil3.compose.AsyncImagePainter.State
import coil3.compose.ConstraintsSizeResolver
import coil3.compose.LocalAsyncImageModelEqualityDelegate
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.NullRequestDataException
import coil3.size.SizeResolver

@Stable
internal fun transformOf(
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
internal fun rememberSizeResolver(contentScale: ContentScale): SizeResolver {
    val isNone = contentScale == ContentScale.None
    return remember(isNone) {
        if (isNone) {
            SizeResolver.ORIGINAL
        } else {
            ConstraintsSizeResolver()
        }
    }
}

@Composable
@NonRestartableComposable
@ReadOnlyComposable
internal inline fun AsyncImageState(
    model: Any?,
    imageLoader: ImageLoader,
) = AsyncImageState(model, LocalAsyncImageModelEqualityDelegate.current, imageLoader)

/** Wrap [AsyncImage]'s unstable arguments to make them stable. */
@Stable
internal class AsyncImageState(
    val model: Any?,
    val modelEqualityDelegate: AsyncImageModelEqualityDelegate,
    val imageLoader: ImageLoader,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is AsyncImageState &&
                modelEqualityDelegate == other.modelEqualityDelegate &&
                modelEqualityDelegate.equals(model, other.model) &&
                imageLoader == other.imageLoader
    }

    override fun hashCode(): Int {
        var result = modelEqualityDelegate.hashCode()
        result = 31 * result + modelEqualityDelegate.hashCode(model)
        result = 31 * result + imageLoader.hashCode()
        return result
    }
}

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