package com.github.panpf.zoomimage.sample.ui.test

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastRoundToInt
import com.githb.panpf.zoomimage.images.ResourceImages
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.sketch.asPainter
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.execute
import com.github.panpf.zoomimage.compose.util.toCompat
import com.github.panpf.zoomimage.compose.util.toPlatform
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.base.ToolbarScaffold
import com.github.panpf.zoomimage.sample.ui.util.mypaint
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.zoom.ReadMode
import com.github.panpf.zoomimage.zoom.internal.calculateBaseTransform
import com.github.panpf.zoomimage.zoom.internal.calculateReadModeTransform
import com.github.panpf.zoomimage.zoom.internal.calculateRestoreContentBaseTransformTransform

class RestoreContentTestScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        ToolbarScaffold("RestoreContent") {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val width = with(density) { maxWidth.roundToPx() }
                val height = with(density) { maxHeight.roundToPx() }
                val context = LocalPlatformContext.current
                var painter: Painter? by remember { mutableStateOf(null) }
                LaunchedEffect(Unit) {
                    val result = ImageRequest(context, ResourceImages.hugeLongQmsht.uri).execute()
                    if (result is ImageResult.Success) {
                        painter = result.image.asPainter()
                    }
                }
                val painter1 = painter
                if (painter1 != null) {
                    val contentScale = ContentScale.Fit
//                    val contentScale = ContentScale.FillHeight
                    val alignment = Alignment.TopStart
                    ClippableImage(
                        painter = painter1,
                        contentDescription = null,
                        contentScale = contentScale,
                        alignment = alignment,
                        clipToBounds = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
//                                val transform = calculateBaseTransform(
//                                    containerSize = IntSizeCompat(width, height),
//                                    contentSize = IntSizeCompat(
//                                        painter1.intrinsicSize.width.fastRoundToInt(),
//                                        painter1.intrinsicSize.height.fastRoundToInt()
//                                    ),
//                                    contentScale = contentScale.toCompat(),
//                                    alignment = alignment.toCompat(),
//                                    rtlLayoutDirection = false,
//                                    rotation = 0,
//                                )
                                val transform = calculateReadModeTransform(
                                    containerSize = IntSizeCompat(width, height),
                                    contentSize = IntSizeCompat(
                                        painter1.intrinsicSize.width.fastRoundToInt(),
                                        painter1.intrinsicSize.height.fastRoundToInt()
                                    ),
                                    contentScale = contentScale.toCompat(),
                                    alignment = alignment.toCompat(),
                                    rtlLayoutDirection = false,
                                    rotation = 0,
                                    readMode = ReadMode.Default,
                                )!!
                                val scale = transform.scale
                                println("TempTestScreen. graphicsLayer. scale=${scale.scaleX}x${scale.scaleY}")
                                scaleX = transform.scaleX
                                scaleY = transform.scaleY
                                translationX = transform.offsetX
                                translationY = transform.offsetY
                                transformOrigin = transform.scaleOrigin.toPlatform()
                            }
                            .graphicsLayer {
                                val baseTransform = calculateBaseTransform(
                                    containerSize = IntSizeCompat(width, height),
                                    contentSize = IntSizeCompat(
                                        painter1.intrinsicSize.width.fastRoundToInt(),
                                        painter1.intrinsicSize.height.fastRoundToInt()
                                    ),
                                    contentScale = contentScale.toCompat(),
                                    alignment = alignment.toCompat(),
                                    rtlLayoutDirection = false,
                                    rotation = 0,
                                )
                                val restoreTransform =
                                    calculateRestoreContentBaseTransformTransform(
                                        containerSize = IntSizeCompat(width, height),
                                        contentSize = IntSizeCompat(
                                            painter1.intrinsicSize.width.fastRoundToInt(),
                                            painter1.intrinsicSize.height.fastRoundToInt()
                                        ),
                                        contentScale = contentScale.toCompat(),
                                        alignment = alignment.toCompat(),
                                        rtlLayoutDirection = false,
                                    )
                                val baseScale = baseTransform.scale
                                val restoreScale = restoreTransform.scale
                                println("TempTestScreen. graphicsLayer2. baseScale=${baseScale.scaleX}x${baseScale.scaleY}, restoreScale=${restoreScale.scaleX}x${restoreScale.scaleY}")
                                scaleX = restoreTransform.scaleX
                                scaleY = restoreTransform.scaleY
                                translationX = restoreTransform.offsetX
                                translationY = restoreTransform.offsetY
                                transformOrigin = restoreTransform.scaleOrigin.toPlatform()
                            }
                    )
                }
            }
        }
    }

    @Composable
    private fun ClippableImage(
        painter: Painter,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        alignment: Alignment = Alignment.Center,
        contentScale: ContentScale = ContentScale.Fit,
        alpha: Float = DefaultAlpha,
        colorFilter: ColorFilter? = null,
        clipToBounds: Boolean = true,
    ) {
        val semantics = if (contentDescription != null) {
            Modifier.semantics {
                this.contentDescription = contentDescription
                this.role = Role.Image
            }
        } else {
            Modifier
        }

        // Explicitly use a simple Layout implementation here as Spacer squashes any non fixed
        // constraint with zero
        Layout(
            modifier
                .then(semantics)
                .let { if (clipToBounds) it.clipToBounds() else it }
                .mypaint(
                    painter = painter,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
        ) { _, constraints ->
            layout(constraints.minWidth, constraints.minHeight) {}
        }
    }
}
