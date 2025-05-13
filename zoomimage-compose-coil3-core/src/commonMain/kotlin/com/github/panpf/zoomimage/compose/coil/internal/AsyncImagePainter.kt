/*
 * Copyright 2023 Coil Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.coil.internal

import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.util.trace
import coil3.compose.AsyncImagePainter.Companion.DefaultTransform
import coil3.compose.AsyncImagePainter.Input
import coil3.compose.AsyncImagePainter.State
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.CrossfadePainter
import coil3.compose.DrawScopeSizeResolver
import coil3.compose.asPainter
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.size.Precision
import coil3.size.SizeResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A [Painter] that that executes an [ImageRequest] asynchronously and renders the [ImageResult].
 */
@Stable
internal class AsyncImagePainter internal constructor(
    input: Input,
) : Painter(), RememberObserver {
    private var painter: Painter? by mutableStateOf(null)
    private var alpha: Float = DefaultAlpha
    private var colorFilter: ColorFilter? = null

    private var isRemembered = false
    private var rememberJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var drawSizeFlow: MutableSharedFlow<Size>? = null
    private var drawSize = Size.Unspecified
        set(value) {
            if (field != value) {
                field = value
                drawSizeFlow?.tryEmit(value)
            }
        }

    internal lateinit var scope: CoroutineScope
    internal var transform = DefaultTransform
    internal var onState: ((State) -> Unit)? = null
    internal var contentScale = ContentScale.Fit
    internal var filterQuality = DefaultFilterQuality
    internal var previewHandler: AsyncImagePreviewHandler? = null

    internal var _input: Input? = input
        set(value) {
            if (field != value) {
                field = value
                restart()
                if (value != null) {
                    inputFlow.value = value
                }
            }
        }

    private val inputFlow: MutableStateFlow<Input> = MutableStateFlow(input)
    val input: StateFlow<Input> = inputFlow.asStateFlow()

    private val stateFlow: MutableStateFlow<State> = MutableStateFlow(State.Empty)
    val state: StateFlow<State> = stateFlow.asStateFlow()

    override val intrinsicSize: Size
        get() = painter?.intrinsicSize ?: Size.Unspecified

    override fun DrawScope.onDraw() {
        drawSize = size
        painter?.apply { draw(size, alpha, colorFilter) }
    }

    override fun applyAlpha(alpha: Float): Boolean {
        this.alpha = alpha
        return true
    }

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun onRemembered() = trace("AsyncImagePainter.onRemembered") {
        (painter as? RememberObserver)?.onRemembered()
        launchJob()
        isRemembered = true
    }

    private fun launchJob() {
        val input = _input ?: return

        rememberJob = scope.launchWithDeferredDispatch {
            val previewHandler = previewHandler
            val state = if (previewHandler != null) {
                // If we're in inspection mode use the preview renderer.
                val request = updateRequest(input.request, isPreview = true)
                previewHandler.handle(input.imageLoader, request)
            } else {
                // Else, execute the request as normal.
                val request = updateRequest(input.request, isPreview = false)
                input.imageLoader.execute(request).toState()
            }
            updateState(state)
        }
    }

    override fun onForgotten() {
        rememberJob = null
        (painter as? RememberObserver)?.onForgotten()
        isRemembered = false
    }

    override fun onAbandoned() {
        rememberJob = null
        (painter as? RememberObserver)?.onAbandoned()
        isRemembered = false
    }

    /**
     * Launch a new image request with the current [Input]s.
     */
    fun restart() {
        if (_input == null) {
            rememberJob = null
        } else if (isRemembered) {
            launchJob()
        }
    }

    /**
     * Update the [request] to work with [AsyncImagePainter].
     */
    private fun updateRequest(request: ImageRequest, isPreview: Boolean): ImageRequest {
        // Connect the size resolver to the draw scope if necessary.
        val sizeResolver = request.sizeResolver
        if (sizeResolver is DrawScopeSizeResolver) {
            sizeResolver.connect(lazyDrawSizeFlow())
        }

        return request.newBuilder()
            .target(
                onStart = { placeholder ->
                    val painter = placeholder?.asPainter(request.context, filterQuality)
                    updateState(State.Loading(painter))
                },
            )
            .apply {
                if (request.defined.sizeResolver == null) {
                    // If the size resolver isn't set, use the original size.
                    size(SizeResolver.ORIGINAL)
                }
                if (request.defined.scale == null) {
                    // If the scale isn't set, use the content scale.
                    scale(contentScale.toScale())
                }
                if (request.defined.precision == null) {
                    // AsyncImagePainter scales the image to fit the canvas size at draw time.
                    precision(Precision.INEXACT)
                }
                if (isPreview) {
                    // The request must be executed synchronously in the preview environment.
                    coroutineContext(EmptyCoroutineContext)
                }
            }
            .build()
    }

    private fun updateState(state: State) {
        val previous = stateFlow.value
        val current = transform(state)
        stateFlow.value = current
        painter = maybeNewCrossfadePainter(previous, current, contentScale) ?: current.painter

        // Manually forget and remember the old/new painters.
        if (previous.painter !== current.painter) {
            (previous.painter as? RememberObserver)?.onForgotten()
            (current.painter as? RememberObserver)?.onRemembered()
        }

        // Notify the state listener.
        onState?.invoke(current)
    }

    private fun ImageResult.toState() = when (this) {
        is SuccessResult -> State.Success(
            painter = image.asPainter(request.context, filterQuality),
            result = this,
        )

        is ErrorResult -> State.Error(
            painter = image?.asPainter(request.context, filterQuality),
            result = this,
        )
    }

    private fun lazyDrawSizeFlow(): Flow<Size> {
        var drawSizeFlow = drawSizeFlow
        if (drawSizeFlow == null) {
            drawSizeFlow = MutableSharedFlow(
                replay = 1,
                onBufferOverflow = DROP_OLDEST,
            )
            val drawSize = drawSize
            if (drawSize.isSpecified) {
                drawSizeFlow.tryEmit(drawSize)
            }
            this.drawSizeFlow = drawSizeFlow
        }
        return drawSizeFlow
    }
}

/** Create and return a [CrossfadePainter] if requested. */
internal expect fun maybeNewCrossfadePainter(
    previous: State,
    current: State,
    contentScale: ContentScale,
): CrossfadePainter?
