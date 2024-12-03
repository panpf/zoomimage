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

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.panpf.zoomimage.compose.glide.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.compose.glide.GlideImage
import com.github.panpf.zoomimage.compose.glide.Placeholder
import com.github.panpf.zoomimage.compose.glide.RequestBuilderTransform
import com.github.panpf.zoomimage.compose.glide.Transition
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.util.rtlFlipped
import com.github.panpf.zoomimage.compose.zoom.ScrollBarSpec
import com.github.panpf.zoomimage.compose.zoom.mouseZoom
import com.github.panpf.zoomimage.compose.zoom.zoom
import com.github.panpf.zoomimage.compose.zoom.zoomScrollBar
import com.github.panpf.zoomimage.glide.GlideTileImageCache
import com.github.panpf.zoomimage.subsampling.SubsamplingImage
import com.github.panpf.zoomimage.subsampling.SubsamplingImageGenerateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * An image component that integrates the Glide image loading framework that zoom and subsampling huge images
 *
 * Example usages:
 *
 * ```kotlin
 * GlideZoomAsyncImage(
 *     model = "https://sample.com/sample.jpeg",
 *     contentDescription = "view image",
 *     modifier = Modifier.fillMaxSize(),
 * ) {
 *     it.placeholder(R.drawable.placeholder)
 * }
 * ```
 *
 * Start a request by passing [model] to [RequestBuilder.load] using the given [requestManager] and
 * then applying the [requestBuilderTransform] function to add options or apply mutations if the
 * caller desires.
 *
 * [alignment], [contentScale], [alpha], [colorFilter] and [contentDescription] have the same
 * defaults (if any) and function identically to the parameters in [Image].
 *
 * If you want to restrict the size of this [Composable], use the given [modifier]. If you'd like to
 * force the size of the pixels you load to be different than the display area, use
 * [RequestBuilder.override]. Often you can get better performance by setting an explicit size so
 * that we do not have to wait for layout to fetch the image. If the size set via the [modifier] is
 * dependent on the content, Glide will probably end up loading the image using
 * [com.bumptech.glide.request.target.Target.SIZE_ORIGINAL]. Avoid `SIZE_ORIGINAL`, implicitly or
 * explicitly if you can. You may end up loading a substantially larger image than you need, which
 * will increase memory usage and may also increase latency.
 *
 * If you provide your own [requestManager] rather than using this method's default, consider using
 * [remember] at a higher level to avoid some amount of overhead of retrieving it each
 * re-composition.
 *
 * This method will inspect [contentScale] and apply a matching transformation if one exists. Any
 * automatically applied transformation can be overridden using [requestBuilderTransform]. Either
 * apply a specific transformation instead, or use [RequestBuilder.dontTransform]]
 *
 * Transitions set via [RequestBuilder.transition] are currently ignored.
 *
 * Note - this method is likely to change while we work on improving the API. Transitions are one
 * significant unexplored area. It's also possible we'll try and remove the [RequestBuilder] from
 * the direct API and instead allow all options to be set directly in the method.
 *
 * [requestBuilderTransform] is overridden by any overlapping parameter defined in this method if
 * that parameter is non-null. For example, [loading] and [failure], if non-null will be used in
 * place of any placeholder set by [requestBuilderTransform] using [RequestBuilder.placeholder] or
 * [RequestBuilder.error].
 *
 * @param loading A [Placeholder] that will be displayed while the request is loading. Specifically
 * it's used if the request is cleared ([com.bumptech.glide.request.target.Target.onLoadCleared]) or
 * loading ([com.bumptech.glide.request.target.Target.onLoadStarted]. There's a subtle difference in
 * behavior depending on which type of [Placeholder] you use. The resource and `Drawable` variants
 * will be displayed if the request fails and no other failure handling is specified, but the
 * `Composable` will not.
 * @param failure A [Placeholder] that will be displayed if the request fails. Specifically it's
 * used when [com.bumptech.glide.request.target.Target.onLoadFailed] is called. If
 * [RequestBuilder.error] is called in [requestBuilderTransform] with a valid [RequestBuilder] (as
 * opposed to resource id or [Drawable]), this [Placeholder] will not be used unless the `error`
 * [RequestBuilder] also fails. This parameter does not override error [RequestBuilder]s, only error
 * resource ids and/or [Drawable]s.
 * @param zoomState The state to control zoom
 * @param scrollBar Controls whether scroll bars are displayed and their style
 * @param onLongPress Called when the user long presses the image
 * @param onTap Called when the user taps the image
 * @see com.github.panpf.zoomimage.compose.glide.test.GlideZoomAsyncImageTest.testGlideZoomAsyncImage
 */
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
    zoomState: GlideZoomState = rememberGlideZoomState(),
    scrollBar: ScrollBarSpec? = ScrollBarSpec.Default,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
    // TODO(judds): Consider using separate GlideImage* methods instead of sealed classes.
    // See http://shortn/_x79pjkMZIH for an internal discussion.
    loading: Placeholder? = null,
    failure: Placeholder? = null,
    transition: Transition.Factory? = null,
    // TODO(judds): Consider defaulting to load the model here instead of always doing so below.
    requestBuilderTransform: RequestBuilderTransform<Drawable> = { it },
) {
    zoomState.zoomable.contentScale = contentScale
    zoomState.zoomable.alignment = alignment

    val context = LocalContext.current
    val glide = Glide.get(context)
    LaunchedEffect(zoomState.subsampling) {
        zoomState.subsampling.tileImageCache = GlideTileImageCache(glide)
    }

    // moseZoom directly acts on ZoomAsyncImage, causing the zoom center to be abnormal.
    Box(modifier = modifier.mouseZoom(zoomState.zoomable)) {
        val coroutineScope = rememberCoroutineScope()
        val layoutDirection = LocalLayoutDirection.current
        GlideImage(
            model = model,
            contentDescription = contentDescription,
            alignment = Alignment.TopStart.rtlFlipped(layoutDirection),
            contentScale = ContentScale.None,
            alpha = alpha,
            colorFilter = colorFilter,
            clipToBounds = false,
            loading = loading,
            failure = failure,
            transition = transition,
            requestBuilderTransform = { requestBuilder ->
                requestBuilderTransform(requestBuilder)
                    .centerInside()
                    .addListener(
                        ResetListener(
                            context = context,
                            coroutineScope = coroutineScope,
                            glide = glide,
                            zoomState = zoomState,
                            model = model
                        )
                    )
            },
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

private class ResetListener(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val glide: Glide,
    private val zoomState: GlideZoomState,
    private val model: Any?,
) : RequestListener<Drawable> {

    private val logger = zoomState.zoomable.logger

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>,
        isFirstResource: Boolean
    ): Boolean {
        logger.d { "GlideZoomAsyncImage. onLoadFailed. model='$model'" }
        reset(ready = false, resource = null)
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>?,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        logger.d { "GlideZoomAsyncImage. onResourceReady. resource=$resource. model='$model'" }
        reset(ready = true, resource = resource)
        return false
    }

    private fun reset(ready: Boolean, resource: Drawable?) {
        val drawableSize = resource
            ?.let { IntSize(it.intrinsicWidth, it.intrinsicHeight) }
            ?.takeIf { it.isNotEmpty() }
        zoomState.zoomable.contentSize = drawableSize ?: IntSize.Zero

        if (ready && model != null && resource != null) {
            coroutineScope.launch {
                val generateResult = zoomState.subsamplingImageGenerators.firstNotNullOfOrNull {
                    it.generateImage(context, glide, model, resource)
                }
                if (generateResult is SubsamplingImageGenerateResult.Error) {
                    logger.d { "GlideZoomAsyncImage. ${generateResult.message}. model='$model'" }
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
}

@Stable
private fun IntSize.isNotEmpty(): Boolean = width > 0 && height > 0