package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.github.panpf.sketch.AsyncImagePainter
import com.github.panpf.sketch.AsyncImageState
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.zoomimage.compose.util.rtlFlipped

/**
 * A composable that executes an [ImageRequest] asynchronously and renders the result.
 *
 * @param request [ImageRequest].
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param sketch The [Sketch] that will be used to execute the request.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param state [AsyncImageState] that will be used to store the state of the request.
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
 *  @param clipToBounds Whether to clip the content to the bounds of this layout. Defaults to true.
 *  @param keepContentNoneStartOnDraw Whether to always draw the content as none on the left on drawing, even if LayoutDirection is Rtl.
 *
 * @see com.github.panpf.sketch.compose.core.common.test.AsyncImageTest.testAsyncImage2
 */
@Composable
@Deprecated("Please use an overload function without the keepContentNoneStartOnDraw parameter instead. Will be removed in the future")
fun MyAsyncImage(
    request: ImageRequest,
    sketch: Sketch,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    keepContentNoneStartOnDraw: Boolean = false,
) {
    val painter = rememberAsyncImagePainter(
        request = request,
        sketch = sketch,
        state = state,
        contentScale = contentScale,
        filterQuality = filterQuality
    )
    val drawAlignment = if (keepContentNoneStartOnDraw) {
        Alignment.TopStart.rtlFlipped(LocalLayoutDirection.current)
    } else {
        alignment
    }
    val drawContentScale =
        if (keepContentNoneStartOnDraw) ContentScale.None else contentScale
    MyAsyncImageContent(
        modifier = modifier.onSizeChanged { size ->
            // Ensure images are prepared before content is drawn when in-memory cache exists
            state.setSizeWithLeast(size)
        },
        painter = painter,
        contentDescription = contentDescription,
        alignment = drawAlignment,
        contentScale = drawContentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        clipToBounds = clipToBounds,
    )
}

/**
 * Draws the current image content.
 *
 * @see com.github.panpf.sketch.compose.core.common.test.internal.AsyncImageContentTest
 */
@Composable
fun MyAsyncImageContent(
    modifier: Modifier,
    painter: Painter,
    contentDescription: String?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    clipToBounds: Boolean = true,
) = Layout(
    modifier = modifier
        .contentDescription(contentDescription)
        .let { if (clipToBounds) it.clipToBounds() else it }
        .mypaint(
            painter = painter,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        ),
    measurePolicy = { _, constraints ->
        layout(constraints.minWidth, constraints.minHeight) {}
    }
)

@Stable
private fun Modifier.contentDescription(contentDescription: String?): Modifier {
    @Suppress("LiftReturnOrAssignment")
    if (contentDescription != null) {
        return semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        return this
    }
}