package com.github.panpf.zoomimage.sample.ui.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.times
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.util.fastIsFinite
import androidx.compose.ui.util.fastRoundToInt
import kotlin.math.max

/**
 * Paint the content using [painter].
 *
 * @param painter [Painter] to be drawn by this [Modifier]
 * @param sizeToIntrinsics `true` to size the element relative to [Painter.intrinsicSize]
 * @param alignment specifies alignment of the [painter] relative to content
 * @param contentScale strategy for scaling [painter] if its size does not match the content size
 * @param alpha opacity of [painter]
 * @param colorFilter optional [ColorFilter] to apply to [painter]
 * @sample androidx.compose.ui.samples.PainterModifierSample
 */
fun Modifier.mypaint(
    painter: Painter,
    sizeToIntrinsics: Boolean = true,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) =
    this then
            PainterElement(
                painter = painter,
                sizeToIntrinsics = sizeToIntrinsics,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )

/**
 * Customized [ModifierNodeElement] for painting content using [painter].
 *
 * @param painter used to paint content
 * @param sizeToIntrinsics `true` to size the element relative to [Painter.intrinsicSize]
 * @param alignment specifies alignment of the [painter] relative to content
 * @param contentScale strategy for scaling [painter] if its size does not match the content size
 * @param alpha opacity of [painter]
 * @param colorFilter optional [ColorFilter] to apply to [painter]
 * @sample androidx.compose.ui.samples.PainterModifierSample
 */
private data class PainterElement(
    val painter: Painter,
    val sizeToIntrinsics: Boolean,
    val alignment: Alignment,
    val contentScale: ContentScale,
    val alpha: Float,
    val colorFilter: ColorFilter?
) : ModifierNodeElement<PainterNode>() {
    override fun create(): PainterNode {
        return PainterNode(
            painter = painter,
            sizeToIntrinsics = sizeToIntrinsics,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
        )
    }

    override fun update(node: PainterNode) {
        val intrinsicsChanged =
            node.sizeToIntrinsics != sizeToIntrinsics ||
                    (sizeToIntrinsics && node.painter.intrinsicSize != painter.intrinsicSize)

        node.painter = painter
        node.sizeToIntrinsics = sizeToIntrinsics
        node.alignment = alignment
        node.contentScale = contentScale
        node.alpha = alpha
        node.colorFilter = colorFilter

        // Only remeasure if intrinsics have changed.
        if (intrinsicsChanged) {
            node.invalidateMeasurement()
        }
        // redraw because one of the node properties has changed.
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "paint"
        properties["painter"] = painter
        properties["sizeToIntrinsics"] = sizeToIntrinsics
        properties["alignment"] = alignment
        properties["contentScale"] = contentScale
        properties["alpha"] = alpha
        properties["colorFilter"] = colorFilter
    }
}

/**
 * [DrawModifier] used to draw the provided [Painter] followed by the contents of the component
 * itself
 *
 * IMPORTANT NOTE: This class sets [Modifier.Node.shouldAutoInvalidate] to false
 * which means it MUST invalidate both draw and the layout. It invalidates both in the
 * [PainterElement.update] method through [LayoutModifierNode.invalidateLayer] (invalidates draw)
 * and [LayoutModifierNode.invalidateLayout] (invalidates layout).
 */
private class PainterNode(
    var painter: Painter,
    var sizeToIntrinsics: Boolean,
    var alignment: Alignment = Alignment.Center,
    var contentScale: ContentScale = ContentScale.Inside,
    var alpha: Float = DefaultAlpha,
    var colorFilter: ColorFilter? = null
) : LayoutModifierNode, Modifier.Node(), DrawModifierNode {

    /**
     * Helper property to determine if we should size content to the intrinsic size of the Painter
     * or not. This is only done if [sizeToIntrinsics] is true and the Painter has an intrinsic size
     */
    private val useIntrinsicSize: Boolean
        get() = sizeToIntrinsics && painter.intrinsicSize.isSpecified

    override val shouldAutoInvalidate: Boolean
        get() = false

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(modifyConstraints(constraints))
        return layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = modifyConstraints(Constraints(maxHeight = height))
            val layoutWidth = measurable.minIntrinsicWidth(height)
            max(constraints.minWidth, layoutWidth)
        } else {
            measurable.minIntrinsicWidth(height)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = modifyConstraints(Constraints(maxHeight = height))
            val layoutWidth = measurable.maxIntrinsicWidth(height)
            max(constraints.minWidth, layoutWidth)
        } else {
            measurable.maxIntrinsicWidth(height)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = modifyConstraints(Constraints(maxWidth = width))
            val layoutHeight = measurable.minIntrinsicHeight(width)
            max(constraints.minHeight, layoutHeight)
        } else {
            measurable.minIntrinsicHeight(width)
        }
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int {
        return if (useIntrinsicSize) {
            val constraints = modifyConstraints(Constraints(maxWidth = width))
            val layoutHeight = measurable.maxIntrinsicHeight(width)
            max(constraints.minHeight, layoutHeight)
        } else {
            measurable.maxIntrinsicHeight(width)
        }
    }

    private fun calculateScaledSize(dstSize: Size): Size {
        return if (!useIntrinsicSize) {
            dstSize
        } else {
            val srcWidth =
                if (!painter.intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                    dstSize.width
                } else {
                    painter.intrinsicSize.width
                }

            val srcHeight =
                if (!painter.intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                    dstSize.height
                } else {
                    painter.intrinsicSize.height
                }

            val srcSize = Size(srcWidth, srcHeight)
            if (dstSize.width != 0f && dstSize.height != 0f) {
                srcSize * contentScale.computeScaleFactor(srcSize, dstSize)
            } else {
                Size.Zero
            }
        }
    }

    private fun modifyConstraints(constraints: Constraints): Constraints {
        val hasBoundedDimens = constraints.hasBoundedWidth && constraints.hasBoundedHeight
        val hasFixedDimens = constraints.hasFixedWidth && constraints.hasFixedHeight
        if ((!useIntrinsicSize && hasBoundedDimens) || hasFixedDimens) {
            // If we have fixed constraints or we are not attempting to size the
            // composable based on the size of the Painter, do not attempt to
            // modify them. Otherwise rely on Alignment and ContentScale
            // to determine how to position the drawing contents of the Painter within
            // the provided bounds
            return constraints.copy(
                minWidth = constraints.maxWidth,
                minHeight = constraints.maxHeight
            )
        }

        val intrinsicSize = painter.intrinsicSize
        val intrinsicWidth =
            if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                intrinsicSize.width.fastRoundToInt()
            } else {
                constraints.minWidth
            }

        val intrinsicHeight =
            if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                intrinsicSize.height.fastRoundToInt()
            } else {
                constraints.minHeight
            }

        // Scale the width and height appropriately based on the given constraints
        // and ContentScale
        val constrainedWidth = constraints.constrainWidth(intrinsicWidth)
        val constrainedHeight = constraints.constrainHeight(intrinsicHeight)
        val scaledSize =
            calculateScaledSize(Size(constrainedWidth.toFloat(), constrainedHeight.toFloat()))

        // For both width and height constraints, consume the minimum of the scaled width
        // and the maximum constraint as some scale types can scale larger than the maximum
        // available size (ex ContentScale.Crop)
        // In this case the larger of the 2 dimensions is used and the aspect ratio is
        // maintained. Even if the size of the composable is smaller, the painter will
        // draw its content clipped
        val minWidth = constraints.constrainWidth(scaledSize.width.fastRoundToInt())
        val minHeight = constraints.constrainHeight(scaledSize.height.fastRoundToInt())
        return constraints.copy(minWidth = minWidth, minHeight = minHeight)
    }

    override fun ContentDrawScope.draw() {
        val intrinsicSize = painter.intrinsicSize
        val srcWidth =
            if (intrinsicSize.hasSpecifiedAndFiniteWidth()) {
                intrinsicSize.width
            } else {
                size.width
            }

        val srcHeight =
            if (intrinsicSize.hasSpecifiedAndFiniteHeight()) {
                intrinsicSize.height
            } else {
                size.height
            }

        val srcSize = Size(srcWidth, srcHeight)

        // Compute the offset to translate the content based on the given alignment
        // and size to draw based on the ContentScale parameter
        val computeScaleFactor = contentScale.computeScaleFactor(srcSize, size)
        val scaledSize =
            if (size.width != 0f && size.height != 0f) {
                srcSize * computeScaleFactor
            } else {
                Size.Zero
            }

        val alignedPosition =
            alignment.align(
                IntSize(scaledSize.width.fastRoundToInt(), scaledSize.height.fastRoundToInt()),
                IntSize(size.width.fastRoundToInt(), size.height.fastRoundToInt()),
                layoutDirection
            )

        val dx = alignedPosition.x.toFloat()
        val dy = alignedPosition.y.toFloat()

        println(
            "PainterModifier. " +
                    "size=${size.width}x${size.height}, " +
                    "srcSize=${srcSize.width}x${srcSize.height}, " +
                    "scaleFactor=${computeScaleFactor.scaleX}x${computeScaleFactor.scaleY}, " +
                    "scaledSize=${scaledSize.width}x${scaledSize.height}, " +
                    "translate=${dx}x${dy}" +
                    ""
        )

        // Only translate the current drawing position while delegating the Painter to draw
        // with scaled size.
        // Individual Painter implementations should be responsible for scaling their drawing
        // content accordingly to fit within the drawing area.
        translate(dx, dy) {
            with(painter) { draw(size = scaledSize, alpha = alpha, colorFilter = colorFilter) }
        }

        // Maintain the same pattern as Modifier.drawBehind to allow chaining of DrawModifiers
        drawContent()
    }

    private fun Size.hasSpecifiedAndFiniteWidth() = this != Size.Unspecified && width.fastIsFinite()

    private fun Size.hasSpecifiedAndFiniteHeight() =
        this != Size.Unspecified && height.fastIsFinite()

    override fun toString(): String =
        "PainterModifier(" +
                "painter=$painter, " +
                "sizeToIntrinsics=$sizeToIntrinsics, " +
                "alignment=$alignment, " +
                "alpha=$alpha, " +
                "colorFilter=$colorFilter)"
}