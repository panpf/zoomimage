package com.github.panpf.zoomimage.compose.sketch.internal

import com.github.panpf.sketch.util.Size as SketchSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
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
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.compose.AsyncImagePainter.Companion.DefaultTransform
import com.github.panpf.sketch.compose.AsyncImagePainter.State
import com.github.panpf.sketch.compose.internal.AsyncImageScaleDecider
import com.github.panpf.sketch.compose.rememberAsyncImagePainter
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.resize.FixedScaleDecider
import com.github.panpf.sketch.resize.SizeResolver
import com.github.panpf.sketch.util.ifOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull

/**
 * 1. Disabled clipToBounds
 * 2. alignment = Alignment.TopStart
 * 3. contentScale = ContentScale.None
 */
@Composable
// todo Adapt Sketch’s new AsyncImage and display progress and status on the demo page
internal fun BaseZoomAsyncImage(
    request: DisplayRequest,
    contentDescription: String?,
    sketch: Sketch,
    modifier: Modifier = Modifier,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
) {
    // Create and execute the image request.
    val newRequest = updateRequest(request, contentScale)
    val painter = rememberAsyncImagePainter(
        newRequest, sketch, transform, onState, contentScale, filterQuality
    )

    // Draw the content without a parent composable or subcomposition.
    val sizeResolver = newRequest.resizeSizeResolver
    Content(
        modifier = if (sizeResolver is ConstraintsSizeResolver) {
            modifier.then(sizeResolver)
        } else {
            modifier
        },
        painter = painter,
        contentDescription = contentDescription,
        alignment = Alignment.TopStart,
        contentScale = ContentScale.None,
        alpha = alpha,
        colorFilter = colorFilter,
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
) = Layout(
    modifier = modifier
        .contentDescription(contentDescription)
//        .clipToBounds()
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
internal fun updateRequest(request: DisplayRequest, contentScale: ContentScale): DisplayRequest {
//    return if (request.defined.sizeResolver == null) {
//        val sizeResolver = if (contentScale == ContentScale.None) {
//            SizeResolver(SketchSize.ORIGINAL)
//        } else {
//            remember { ConstraintsSizeResolver() }
//        }
//        request.newBuilder().size(sizeResolver).build()
//    } else {
//        request
//    }
    val noSizeResolver = request.definedOptions.resizeSizeResolver == null
    val noResetScale = request.definedOptions.resizeScaleDecider == null
    return if (noSizeResolver || noResetScale) {
        val sizeResolver = ifOrNull(noSizeResolver) {
            remember { ConstraintsSizeResolver() }
        }
        request.newDisplayRequest {
            // If no other size resolver is set, pauses until the layout size is positive.
            if (noSizeResolver && sizeResolver != null) {
                resizeSize(sizeResolver)
            }
            // If no other scale resolver is set, use the content scale.
            if (noResetScale) {
                resizeScale(AsyncImageScaleDecider(FixedScaleDecider(contentScale.toScale())))
            }
        }
    } else {
        request
    }
}

/** A [SizeResolver] that computes the size from the constrains passed during the layout phase. */
internal class ConstraintsSizeResolver : SizeResolver, LayoutModifier {

    private val _constraints = MutableStateFlow(ZeroConstraints)

    override suspend fun size(): SketchSize =
        _constraints.mapNotNull(Constraints::toSizeOrNull).first()

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

    // Equals and hashCode cannot be implemented because they are used in remember
}

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

//@Stable
//private fun Constraints.toSizeOrNull() = when {
//    isZero -> null
//    else -> SketchSize(
//        width = if (hasBoundedWidth) Dimension(maxWidth) else Dimension.Undefined,
//        height = if (hasBoundedHeight) Dimension(maxHeight) else Dimension.Undefined
//    )
//}

@Stable
private fun Constraints.toSizeOrNull() = when {
    isZero -> null
    hasBoundedWidth && hasBoundedHeight -> SketchSize(maxWidth, maxHeight)
    else -> null
}
