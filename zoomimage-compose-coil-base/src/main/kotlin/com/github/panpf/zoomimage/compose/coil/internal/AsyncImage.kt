package com.github.panpf.zoomimage.compose.coil.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Size
import coil.size.SizeResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull


/**
 * A composable that executes an [ImageRequest] asynchronously and renders the result.
 *
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param imageLoader The [ImageLoader] that will be used to execute the request.
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
 * @param clipToBounds Optional controls whether content that is out of scope should be cropped
 */
@Composable
internal fun AsyncImage(
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
    clipToBounds: Boolean = true,
) {
    // Create and execute the image request.
    val request = updateRequest(requestOf(model), contentScale)
    val painter = rememberAsyncImagePainter(
        request, imageLoader, transform, onState, contentScale, filterQuality
    )

    // Draw the content without a parent composable or subcomposition.
    val sizeResolver = request.sizeResolver
    Content(
        modifier = if (sizeResolver is ConstraintsSizeResolver) {
            modifier.then(sizeResolver)
        } else {
            modifier
        },
        painter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        clipToBounds = clipToBounds
    )
}

/** Draws the current image content. */
@Composable
internal fun Content(
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
        .then(
            ContentPainterModifier(
                painter = painter,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        ),
    measurePolicy = { _, constraints ->
        layout(constraints.minWidth, constraints.minHeight) {}
    }
)

@Composable
internal fun updateRequest(request: ImageRequest, contentScale: ContentScale): ImageRequest {
    return if (request.defined.sizeResolver == null) {
        val sizeResolver = if (contentScale == ContentScale.None) {
            SizeResolver(Size.ORIGINAL)
        } else {
            remember { ConstraintsSizeResolver() }
        }
        request.newBuilder().size(sizeResolver).build()
    } else {
        request
    }
}

/** A [SizeResolver] that computes the size from the constrains passed during the layout phase. */
internal class ConstraintsSizeResolver : SizeResolver, LayoutModifier {

    private val _constraints = MutableStateFlow(ZeroConstraints)

    override suspend fun size() = _constraints.mapNotNull(Constraints::toSizeOrNull).first()

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        // Cache the current constraints.
        _constraints.value = constraints

        // Measure and layout the content.
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    fun setConstraints(constraints: Constraints) {
        _constraints.value = constraints
    }
}

@Stable
private fun Modifier.contentDescription(contentDescription: String?): Modifier {
    if (contentDescription != null) {
        return semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        return this
    }
}

@Stable
private fun Constraints.toSizeOrNull() = when {
    isZero -> null
    else -> Size(
        width = if (hasBoundedWidth) Dimension(maxWidth) else Dimension.Undefined,
        height = if (hasBoundedHeight) Dimension(maxHeight) else Dimension.Undefined
    )
}