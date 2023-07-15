package com.github.panpf.zoomimage

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toSize
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.compose.ZoomAnimationSpec
import com.github.panpf.zoomimage.compose.concat
import com.github.panpf.zoomimage.compose.div
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.computeContainerOriginByTouchPosition
import com.github.panpf.zoomimage.compose.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.internal.computeContentInContainerVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeLocationOffset
import com.github.panpf.zoomimage.compose.internal.computeOffsetBounds
import com.github.panpf.zoomimage.compose.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.internal.computeScaleOffsetByCentroid
import com.github.panpf.zoomimage.compose.internal.computeTransform
import com.github.panpf.zoomimage.compose.internal.containerOriginToContentOrigin
import com.github.panpf.zoomimage.compose.internal.contentOriginToContainerOrigin
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isEmpty
import com.github.panpf.zoomimage.compose.internal.isNotEmpty
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.supportReadMode
import com.github.panpf.zoomimage.compose.internal.toCompatIntRect
import com.github.panpf.zoomimage.compose.internal.toCompatIntSize
import com.github.panpf.zoomimage.compose.internal.toCompatScaleFactor
import com.github.panpf.zoomimage.compose.internal.toScaleMode
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.toShortString
import com.github.panpf.zoomimage.core.Origin
import com.github.panpf.zoomimage.core.internal.DEFAULT_MEDIUM_SCALE_MULTIPLE
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.canScroll
import com.github.panpf.zoomimage.core.internal.computeScrollEdge
import com.github.panpf.zoomimage.core.internal.computeSupportScales
import com.github.panpf.zoomimage.core.internal.limitScaleWithRubberBand
import com.github.panpf.zoomimage.core.toShortString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun rememberZoomableState(
    defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE,
    threeStepScale: Boolean = false,
    rubberBandScale: Boolean = true,
    animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default,
    readMode: ReadMode? = null,
    debugMode: Boolean = false,
): ZoomableState {
    val zoomableState = rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState()
    }
    zoomableState.defaultMediumScaleMultiple = defaultMediumScaleMultiple
    zoomableState.threeStepScale = threeStepScale
    zoomableState.rubberBandScale = rubberBandScale
    zoomableState.animationSpec = animationSpec
    zoomableState.readMode = readMode
    zoomableState.debugMode = debugMode
    LaunchedEffect(
        zoomableState.containerSize,
        zoomableState.contentSize,
        zoomableState.contentOriginSize,
        zoomableState.contentScale,
        zoomableState.contentAlignment,
        readMode,
        defaultMediumScaleMultiple,
    ) {
        if (!zoomableState.contentSize.isEmpty() && zoomableState.containerSize.isEmpty()) {
            zoomableState.contentSize = zoomableState.containerSize
        }
        zoomableState.reset()
    }
    return zoomableState
}

class ZoomableState(
    @FloatRange(from = 0.0) initialScale: Float = 1f,
    @FloatRange(from = 0.0) initialTranslateX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslateY: Float = 0f,
    @FloatRange(from = 0.0) initialRotation: Float = 0f,
) {
    private var lastAnimatable: Animatable<*, *>? = null

    var containerSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentOriginSize: IntSize by mutableStateOf(IntSize.Zero)
    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
    var contentAlignment: Alignment by mutableStateOf(Alignment.Center)
    var threeStepScale: Boolean = false
    var rubberBandScale: Boolean = true
    var animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default
    var readMode: ReadMode? = null
    var debugMode: Boolean = false
    var defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE

    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set

    // todo transform 和 displayTransform 表达的意思要换一下
    var transform: Transform by mutableStateOf(   // todo support rotation
        Transform(
            scale = ScaleFactor(scaleX = initialScale, scaleY = initialScale),
            offset = Offset(x = initialTranslateX, y = initialTranslateY),
            rotation = initialRotation,
        )
    )
        private set
    var baseTransform: Transform by mutableStateOf(Transform.Origin)    // todo 使用 Compat 版本
        private set
    val displayTransform: Transform by derivedStateOf {
        baseTransform.concat(transform)
    }
    val transformOrigin = TransformOrigin(0f, 0f)

    val offsetBounds: IntRect by derivedStateOf {    // todo 使用 Compat 版本
        computeOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            scale = transform.scaleX,
        ).roundToIntRect()
    }

    val contentInContainerRect: IntRect by derivedStateOf {    // todo 使用 Compat 版本
        computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }
    val contentInContainerVisibleRect: IntRect by derivedStateOf {    // todo 使用 Compat 版本
        computeContentInContainerVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }

    val containerVisibleRect: IntRect by derivedStateOf {    // todo 使用 Compat 版本
        computeContainerVisibleRect(
            containerSize = containerSize,
            scale = transform.scaleX,
            offset = transform.offset
        ).roundToIntRect()
    }
    val contentVisibleRect: IntRect by derivedStateOf {    // todo 使用 Compat 版本
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            scale = transform.scaleX,
            offset = transform.offset,
        ).roundToIntRect()
    }
    val scrollEdge: ScrollEdge by derivedStateOf {
        computeScrollEdge(
            contentInContainerVisibleRect = contentInContainerVisibleRect.toCompatIntRect(),
            contentVisibleRect = contentVisibleRect.toCompatIntRect(),
        )
    }

    internal suspend fun reset() {
        stopAnimation("reset")

        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val containerSize = containerSize
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val initialTransform: Transform
        if (containerSize.isEmpty() || contentSize.isEmpty()) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            baseTransform = Transform.Origin
            initialTransform = Transform.Origin
        } else {
            val rotatedContentSize = contentSize.rotate(transform.rotation.roundToInt())
            val rotatedContentOriginSize = contentOriginSize.rotate(transform.rotation.roundToInt())
            val scales = computeSupportScales(
                contentSize = rotatedContentSize.toCompatIntSize(),
                contentOriginSize = rotatedContentOriginSize.toCompatIntSize(),
                containerSize = containerSize.toCompatIntSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(
                    srcSize = rotatedContentSize.toSize(),
                    dstSize = containerSize.toSize()
                ).toCompatScaleFactor(),
                defaultMediumScaleMultiple = defaultMediumScaleMultiple,
            )
            minScale = scales[0]
            // todo 清明上河图图片示例，垂直方向上，没有充满屏幕，貌似是基础 Image 的缩放比例跟预想的不一样，导致计算出来的 mediumScale 应用后图片显示没有充满屏幕
            mediumScale = scales[1]
            maxScale = scales[2]
            baseTransform = computeTransform(
                contentSize = rotatedContentSize,
                containerSize = containerSize,
                contentScale = contentScale,
                alignment = contentAlignment,
            )
            val readModeResult = contentScale.supportReadMode() &&
                    readMode?.should(
                        srcSize = rotatedContentSize.toCompatIntSize(),
                        dstSize = containerSize.toCompatIntSize()
                    ) == true
            initialTransform = if (readModeResult) {
                val readModeTransform = computeReadModeTransform(
                    contentSize = rotatedContentSize,
                    containerSize = containerSize,
                    contentScale = contentScale,
                    alignment = contentAlignment,
                )
                readModeTransform.div(baseTransform.scale)
            } else {
                Transform.Origin
            }
        }
        val limitedInitialTransform = limitTransform(initialTransform)
        log {
            "reset. containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "contentAlignment=${contentAlignment.name}, " +
                    "readMode=${readMode}, " +
                    "minScale=${minScale.format(4)}, " +
                    "mediumScale=${mediumScale.format(4)}, " +
                    "maxScale=${maxScale.format(4)}, " +
                    "baseTransform=${baseTransform.toShortString()}, " +
                    "initialTransform=${initialTransform.toShortString()}, " +
                    "limitedInitialTransform=${limitedInitialTransform.toShortString()}"
        }

        updateTransform(
            targetTransform = limitedInitialTransform,
            animated = false,
            caller = "reset"
        )
    }


    suspend fun scale(
        targetScale: Float,
        centroid: Offset = Offset(x = containerSize.width / 2f, y = containerSize.height / 2f),
        animated: Boolean = false,
        rubberBandScale: Boolean = false,
    ) {
        stopAnimation("scale")

        val limitedTargetScale = if (rubberBandScale && this@ZoomableState.rubberBandScale) {
            limitScaleWithRubberBand(targetScale)
        } else {
            limitScale(targetScale)
        }
        val currentTransform = transform
        val currentScale = currentTransform.scaleX
        val currentOffset = currentTransform.offset
        val targetOffset = computeScaleOffsetByCentroid(
            currentScale = currentScale,
            currentOffset = currentOffset,
            targetScale = limitedTargetScale,
            centroid = centroid,
            gestureRotate = 0f,
        )
        val limitedTargetOffset = limitOffset(targetOffset, limitedTargetScale)
        val limitedTargetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = limitedTargetOffset
        )
        log {
            val targetAddScale = targetScale - currentScale
            val limitedAddScale = limitedTargetScale - currentScale
            val targetAddOffset = targetOffset - currentOffset
            val limitedTargetAddOffset = limitedTargetOffset - currentOffset
            "scale. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "centroid=${centroid.toShortString()}, " +
                    "animated=${animated}, " +
                    "addScale=${targetAddScale.format(4)} -> ${limitedAddScale.format(4)}, " +
                    "addOffset=${targetAddOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "transform=${currentTransform.toShortString()} -> ${limitedTargetTransform.toShortString()}"
        }

        updateTransform(
            targetTransform = limitedTargetTransform,
            animated = animated,
            caller = "scale"
        )
    }

    suspend fun offset(targetOffset: Offset, animated: Boolean = false) {
        stopAnimation("offset")

        val currentTransform = transform
        val currentScale = currentTransform.scaleX
        val limitedTargetOffset = limitOffset(targetOffset, currentScale)
        val limitedTargetTransform = currentTransform.copy(offset = limitedTargetOffset)
        log {
            val currentOffset = currentTransform.offset
            val targetAddOffset = targetOffset - currentOffset
            val limitedTargetAddOffset = limitedTargetOffset - currentOffset
            "offset. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "animated=${animated}, " +
                    "currentScale=${currentScale.format(4)}, " +
                    "addOffset=${targetAddOffset.toShortString()} -> ${limitedTargetAddOffset}, " +
                    "transform=${currentTransform.toShortString()} -> ${limitedTargetTransform.toShortString()}"
        }

        updateTransform(
            targetTransform = limitedTargetTransform,
            animated = animated,
            caller = "offset"
        )
    }

    suspend fun location(
        contentOrigin: Origin,
        targetScale: Float = transform.scaleX,
        animated: Boolean = false,
    ) {
        stopAnimation("location")

        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentTransform = transform
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val limitedTargetScale = limitScale(targetScale)
        val targetOffset = computeLocationOffset(
            containerSize = containerSize,
            scale = limitedTargetScale,
            containerOrigin = containerOrigin,
        )
        val limitedTargetOffset = limitOffset(targetOffset, limitedTargetScale)
        val limitedTargetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = limitedTargetOffset
        )
        log {
            val currentScale = currentTransform.scaleX
            val currentOffset = currentTransform.offset
            val targetAddScale = targetScale - currentScale
            val limitedTargetAddScale = limitedTargetScale - currentScale
            val targetAddOffset = targetOffset - currentOffset
            val limitedTargetAddOffset = limitedTargetOffset - currentOffset
            "location. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "animated=${animated}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "addScale=${targetAddScale.format(4)} -> ${limitedTargetAddScale.format(4)}, " +
                    "addOffset=${targetAddOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "transform=${currentTransform.toShortString()} -> ${limitedTargetTransform.toShortString()}"
        }

        updateTransform(
            targetTransform = limitedTargetTransform,
            animated = animated,
            caller = "location"
        )
    }

    suspend fun fling(velocity: Velocity, density: Density) {
        stopAnimation("fling")

        val currentTransform = transform
        val startOffset = currentTransform.offset
        val flingAnimatable = Animatable(
            initialValue = startOffset,
            typeConverter = Offset.VectorConverter,
        )
        this.lastAnimatable = flingAnimatable
        coroutineScope {
            launch {
                val initialVelocity = Offset.VectorConverter
                    .convertFromVector(AnimationVector(velocity.x, velocity.y))
                flingAnimatable.animateDecay(
                    initialVelocity = initialVelocity,
                    animationSpec = splineBasedDecay(density)
                ) {
                    val currentTransform2 = transform
                    val targetOffset = this.value
                    val limitedTargetOffset = limitOffset(targetOffset, currentTransform2.scaleX)
                    val distance = limitedTargetOffset - startOffset
                    log {
                        "fling. running. " +
                                "velocity=$velocity, " +
                                "startOffset=${startOffset.toShortString()}, " +
                                "currentOffset=${limitedTargetOffset.toShortString()}, " +
                                "distance=$distance"
                    }
                    transform = currentTransform2.copy(offset = limitedTargetOffset)
                }
            }
        }
    }

    suspend fun switchScale(
        contentOrigin: Origin = Origin(0.5f, 0.5f),
        animated: Boolean = true
    ): Float {
        val nextScale = getNextStepScale()
        location(contentOrigin = contentOrigin, targetScale = nextScale, animated = animated)
        return nextScale
    }

    suspend fun reboundScale(centroid: Offset) {
        val minScale = minScale
        val maxScale = maxScale
        val currentScale = transform.scaleX
        val targetScale = when {
            currentScale.format(2) > maxScale.format(2) -> maxScale
            currentScale.format(2) < minScale.format(2) -> minScale
            else -> null
        }
        if (targetScale != null) {
            scale(
                targetScale = targetScale,
                centroid = centroid,
                animated = true,
                rubberBandScale = false
            )
        }
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    suspend fun stopAnimation(caller: String) {
        val lastAnimatable = lastAnimatable
        if (lastAnimatable?.isRunning == true) {
            lastAnimatable.stop()
            log { "stopAnimation:$caller" }
        }
    }

    fun canDrag(horizontal: Boolean, direction: Int): Boolean =
        canScroll(horizontal, direction * -1, scrollEdge)

    fun touchOffsetToContentOrigin(touch: Offset): Origin {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return Origin.Zero
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return Origin.Zero
        val currentTransform = transform
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            scale = currentTransform.scaleX,
            offset = currentTransform.offset,
            touch = touch
        )
        return containerOriginToContentOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerOrigin = containerOrigin
        )
    }

    private fun limitScale(targetScale: Float): Float {
        return targetScale.coerceIn(minimumValue = minScale, maximumValue = maxScale)
    }

    private fun limitScaleWithRubberBand(targetScale: Float): Float {
        return limitScaleWithRubberBand(
            currentScale = transform.scaleX,
            targetScale = targetScale,
            minScale = minScale,
            maxScale = maxScale
        )
    }

    private fun limitOffset(offset: Offset, scale: Float): Offset {
        val offsetBounds = computeOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            scale = scale
        )
        if (offset.x >= offsetBounds.left
            && offset.x <= offsetBounds.right
            && offset.y >= offsetBounds.top
            && offset.y <= offsetBounds.bottom
        ) {
            return offset
        }
        return Offset(
            x = offset.x.coerceIn(
                minimumValue = offsetBounds.left,
                maximumValue = offsetBounds.right
            ),
            y = offset.y.coerceIn(
                minimumValue = offsetBounds.top,
                maximumValue = offsetBounds.bottom
            ),
        )
    }

    private fun limitTransform(transform: Transform): Transform {
        val limitedScale = limitScale(transform.scaleX)
        val limitedOffset = limitOffset(transform.offset, limitedScale)
        return if (limitedScale != transform.scaleX || limitedOffset != transform.offset) {
            transform.copy(
                scale = ScaleFactor(limitedScale),
                offset = limitedOffset,
            )
        } else {
            transform
        }
    }

    private suspend fun updateTransform(
        targetTransform: Transform,
        animated: Boolean,
        caller: String
    ) {
        stopAnimation(caller)

        if (animated) {
            val currentTransform = transform
            val updateAnimatable = Animatable(0f)
            this.lastAnimatable = updateAnimatable
            coroutineScope {
                launch {
                    updateAnimatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = animationSpec.durationMillis,
                            easing = animationSpec.easing
                        ),
                        initialVelocity = animationSpec.initialVelocity,
                    ) {
                        val transform = com.github.panpf.zoomimage.compose.lerp(
                            start = currentTransform,
                            stop = targetTransform,
                            fraction = value
                        )
                        log {
                            "$caller. animated running. transform=${transform.toShortString()}"
                        }
                        this@ZoomableState.transform = transform
                    }
                }
            }
        } else {
            this.transform = targetTransform
        }
    }

    private fun log(message: () -> String) {
        if (debugMode) {
            Log.d("ZoomableState", message())
        }
    }

    override fun toString(): String =
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "contentAlignment=${contentAlignment.name}, " +
                "minScale=${minScale.format(4)}, " +
                "mediumScale=${mediumScale.format(4)}, " +
                "maxScale=${maxScale.format(4)}, " +
                "transform=${transform.toShortString()}" +
                ")"

    companion object {

        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = mapSaver(
            save = {
                mapOf(
                    "scale" to it.transform.scaleX,
                    "offsetX" to it.transform.offset.x,
                    "offsetY" to it.transform.offset.y,
                    "rotation" to it.transform.rotation,
                )
            },
            restore = {
                ZoomableState(
                    initialScale = it["scale"] as Float,
                    initialTranslateX = it["offsetX"] as Float,
                    initialTranslateY = it["offsetY"] as Float,
                    initialRotation = it["rotation"] as Float,
                )
            }
        )
    }
}