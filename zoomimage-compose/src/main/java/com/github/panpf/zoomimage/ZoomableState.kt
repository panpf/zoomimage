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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.compose.concat
import com.github.panpf.zoomimage.compose.div
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.computeContainerOriginByTouchPosition
import com.github.panpf.zoomimage.compose.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeOffsetBounds
import com.github.panpf.zoomimage.compose.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.internal.computeScaleTargetOffset
import com.github.panpf.zoomimage.compose.internal.computeTransform
import com.github.panpf.zoomimage.compose.internal.contentOriginToContainerOrigin
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.isAvailable
import com.github.panpf.zoomimage.compose.internal.isNotAvailable
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.supportReadMode
import com.github.panpf.zoomimage.compose.internal.toCompatRectF
import com.github.panpf.zoomimage.compose.internal.toCompatScaleFactor
import com.github.panpf.zoomimage.compose.internal.toCompatSize
import com.github.panpf.zoomimage.compose.internal.toScaleMode
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.compose.internal.toTransform
import com.github.panpf.zoomimage.compose.toShortString
import com.github.panpf.zoomimage.core.Origin
import com.github.panpf.zoomimage.core.internal.DEFAULT_MEDIUM_SCALE_MULTIPLE
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.computeCanDrag
import com.github.panpf.zoomimage.core.internal.computeSupportScales
import com.github.panpf.zoomimage.core.toShortString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun rememberZoomableState(
    threeStepScaleEnabled: Boolean = false,
    animationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default,
    readMode: ReadMode = ReadMode.Default,
    defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE,
    debugMode: Boolean = false,
): ZoomableState {
    val state = rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState()
    }
    state.threeStepScaleEnabled = threeStepScaleEnabled
    state.defaultAnimationSpec = animationSpec
    state.readMode = readMode
    state.defaultMediumScaleMultiple = defaultMediumScaleMultiple
    state.debugMode = debugMode
    LaunchedEffect(
        state.containerSize,
        state.contentSize,
        state.contentOriginSize,
        state.contentScale,
        state.contentAlignment,
        readMode,
        defaultMediumScaleMultiple,
    ) {
        if (!state.contentSize.isAvailable() && state.containerSize.isAvailable()) {
            state.contentSize = state.containerSize
        }
        state.reset()
    }
    return state
}

class ZoomableState(
    @FloatRange(from = 0.0) initialScale: Float = 1f,
    @FloatRange(from = 0.0) initialTranslateX: Float = 0f,
    @FloatRange(from = 0.0) initialTranslateY: Float = 0f,
    @FloatRange(from = 0.0) initialRotation: Float = 0f,
) {

    // todo support click and long press
    // todo support rubber band effect

    private var lastAnimatable: Animatable<*, *>? = null

    var containerSize: Size by mutableStateOf(Size.Zero)
    var contentSize: Size by mutableStateOf(Size.Zero)
    var contentOriginSize: Size by mutableStateOf(Size.Zero)
    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
    var contentAlignment: Alignment by mutableStateOf(Alignment.Center)
    var threeStepScaleEnabled: Boolean = false
    var defaultAnimationSpec: ZoomAnimationSpec = ZoomAnimationSpec.Default
    var readMode: ReadMode = ReadMode.Default
    var debugMode: Boolean = false
    var defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE

    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set

    var transform: Transform by mutableStateOf(   // todo support rotation
        Transform(
            scale = ScaleFactor(scaleX = initialScale, scaleY = initialScale),
            offset = Offset(x = initialTranslateX, y = initialTranslateY),
            rotation = initialRotation,
        )
    )
        private set
    var baseTransform: Transform by mutableStateOf(Transform.Origin)
        private set
    val displayTransform: Transform by derivedStateOf {
        baseTransform.concat(transform)
    }
    val transformOrigin = TransformOrigin(0f, 0f)

    val offsetBounds: Rect by derivedStateOf {
        computeOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = transform.scaleX,
        )
    }

    val containerVisibleRect: Rect by derivedStateOf {
        computeContainerVisibleRect(containerSize, transform.scaleX, transform.offset)
    }
    val contentVisibleRect: Rect by derivedStateOf {
        // todo 长微博图片示例，显示区域框框底部没有到底，但是图片已经到底了
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = transform.scaleX,
            offset = transform.offset,
        )
    }
    val contentInContainerRect: Rect by derivedStateOf {
        computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
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
        if (containerSize.isNotAvailable() || contentSize.isNotAvailable()) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            baseTransform = Transform.Origin
            initialTransform = Transform.Origin
        } else {
            val rotatedContentSize = contentSize.rotate(transform.rotation.roundToInt())
            val rotatedContentOriginSize = contentOriginSize.rotate(transform.rotation.roundToInt())
            val scales = computeSupportScales(
                contentSize = rotatedContentSize.toCompatSize(),
                contentOriginSize = rotatedContentOriginSize.toCompatSize(),
                containerSize = containerSize.toCompatSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(rotatedContentSize, containerSize)
                    .toCompatScaleFactor(),
                defaultMediumScaleMultiple = defaultMediumScaleMultiple,
            )
            minScale = scales[0]
            mediumScale =
                scales[1] // todo 清明上河图图片示例，垂直方向上，没有充满屏幕，貌似是基础 Image 的缩放比例跟预想的不一样，导致计算出来的 mediumScale 应用后图片显示没有充满屏幕
            maxScale = scales[2]
            val readModeResult = readMode.enabled
                    && contentScale.supportReadMode()
                    && readMode.decider.should(
                srcSize = rotatedContentSize.toCompatSize(),
                dstSize = containerSize.toCompatSize()
            )
            baseTransform = computeTransform(
                srcSize = rotatedContentSize,
                dstSize = containerSize,
                scale = contentScale,
                alignment = contentAlignment,
            ).toTransform()
            initialTransform = if (readModeResult) {
                val readModeTransform = computeReadModeTransform(
                    srcSize = rotatedContentSize,
                    dstSize = containerSize,
                    scale = contentScale,
                    alignment = contentAlignment,
                ).toTransform()
                readModeTransform.div(baseTransform.scale)
            } else {
                Transform.Origin
            }
        }
        val limitedInitialTransform = limitTransform(initialTransform)
        log {
            "reset. contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}, " +
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

        transform = limitedInitialTransform
    }


    suspend fun snapScaleBy(addScale: Float) {
        stopAnimation("snapScaleBy")

        val currentTransform = transform
        val targetScale = currentTransform.scaleX * addScale
        val targetTransform = currentTransform.copy(scale = ScaleFactor(targetScale))
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapScaleBy. " +
                    "addScale=${addScale.format(4)}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun snapScaleTo(targetScale: Float) {
        stopAnimation("snapScaleTo")

        val currentTransform = transform
        val targetTransform = currentTransform.copy(scale = ScaleFactor(targetScale))
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun animateScaleTo(targetScale: Float) {
        stopAnimation("animateScaleTo")

        val currentTransform = transform
        val targetTransform = currentTransform.copy(scale = ScaleFactor(targetScale))
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "animateScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        val scaleAnimatable = Animatable(0f)
        this.lastAnimatable = scaleAnimatable
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = defaultAnimationSpec.durationMillis,
                        easing = defaultAnimationSpec.easing
                    ),
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val transform = com.github.panpf.zoomimage.compose.lerp(
                        start = currentTransform,
                        stop = limitedTargetTransform,
                        fraction = value
                    )
                    this@ZoomableState.transform = transform
                }
            }
        }
    }


    suspend fun snapOffsetBy(addOffset: Offset) {
        stopAnimation("snapOffsetBy")

        val currentTransform = transform
        val targetOffset = currentTransform.offset.plus(addOffset)
        val targetTransform = currentTransform.copy(offset = targetOffset)
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapOffsetBy. " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun snapOffsetTo(targetOffset: Offset) {
        stopAnimation("snapOffsetTo")

        val currentTransform = transform
        val targetTransform = currentTransform.copy(offset = targetOffset)
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun animateOffsetTo(targetOffset: Offset) {
        stopAnimation("animateOffsetTo")

        val currentTransform = transform
        val targetTransform = currentTransform.copy(offset = targetOffset)
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "animateOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        val offsetAnimatable = Animatable(0f)
        this.lastAnimatable = offsetAnimatable
        coroutineScope {
            launch {
                offsetAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = defaultAnimationSpec.durationMillis,
                        easing = defaultAnimationSpec.easing
                    ),
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val transform = com.github.panpf.zoomimage.compose.lerp(
                        start = currentTransform,
                        stop = limitedTargetTransform,
                        fraction = value
                    )
                    this@ZoomableState.transform = transform
                }
            }
        }
    }


    suspend fun snapLocation(contentOrigin: Origin, targetScale: Float = transform.scaleX) {
        stopAnimation("snapLocation")

        val containerSize = containerSize.takeIf { it.isAvailable() } ?: return
        val contentSize = contentSize.takeIf { it.isAvailable() } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentTransform = transform
        val limitedTargetScale = limitScale(targetScale)
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val targetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = computeScaleTargetOffset(
                containerSize = containerSize,
                scale = limitedTargetScale,
                containerOrigin = containerOrigin
            )
        )
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapLocation. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun snapLocation(touch: Offset, targetScale: Float = transform.scaleX) {
        stopAnimation("snapLocation")

        val containerSize = containerSize.takeIf { it.isAvailable() } ?: return
        val contentSize = contentSize.takeIf { it.isAvailable() } ?: return
        val currentTransform = transform
        val limitedTargetScale = limitScale(targetScale)
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            scale = currentTransform.scaleX,
            offset = currentTransform.offset,
            touch = touch
        )
        val targetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = computeScaleTargetOffset(
                containerSize = containerSize,
                scale = limitedTargetScale,
                containerOrigin = containerOrigin
            )
        )
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "snapLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
    }

    suspend fun animateLocation(
        contentOrigin: Origin,
        targetScale: Float = transform.scaleX
    ) {
        stopAnimation("animateLocation")

        val containerSize = containerSize.takeIf { it.isAvailable() } ?: return
        val contentSize = contentSize.takeIf { it.isAvailable() } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentTransform = transform
        val limitedTargetScale = limitScale(targetScale)
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val targetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = computeScaleTargetOffset(
                containerSize = containerSize,
                scale = limitedTargetScale,
                containerOrigin = containerOrigin
            )
        )
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "animateLocation. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        val locationAnimatable = Animatable(0f)
        this.lastAnimatable = locationAnimatable
        coroutineScope {
            launch {
                locationAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = defaultAnimationSpec.durationMillis,
                        easing = defaultAnimationSpec.easing
                    ),
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val transform = com.github.panpf.zoomimage.compose.lerp(
                        start = currentTransform,
                        stop = limitedTargetTransform,
                        fraction = value
                    )
                    this@ZoomableState.transform = transform
                }
            }
        }
    }

    suspend fun animateLocation(touch: Offset, targetScale: Float = transform.scaleX) {
        stopAnimation("animateLocation")

        val containerSize = containerSize.takeIf { it.isAvailable() } ?: return
        val contentSize = contentSize.takeIf { it.isAvailable() } ?: return
        val currentTransform = transform
        val limitedTargetScale = limitScale(targetScale)
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            scale = currentTransform.scaleX,
            offset = currentTransform.offset,
            touch = touch
        )
        val targetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = computeScaleTargetOffset(
                containerSize = containerSize,
                scale = limitedTargetScale,
                containerOrigin = containerOrigin
            )
        )
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "animateLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        val locationAnimatable = Animatable(0f)
        this.lastAnimatable = locationAnimatable
        coroutineScope {
            launch {
                locationAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = defaultAnimationSpec.durationMillis,
                        easing = defaultAnimationSpec.easing
                    ),
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val transform = com.github.panpf.zoomimage.compose.lerp(
                        start = currentTransform,
                        stop = limitedTargetTransform,
                        fraction = value
                    )
                    this@ZoomableState.transform = transform
                }
            }
        }
    }


    suspend fun switchScale(contentOrigin: Origin = Origin(0.5f, 0.5f)): Float {
        val nextScale = getNextStepScale()
        animateLocation(contentOrigin = contentOrigin, targetScale = nextScale)
        return nextScale
    }

    suspend fun switchScale(touch: Offset): Float {
        val nextScale = getNextStepScale()
        animateLocation(touch = touch, targetScale = nextScale)
        return nextScale
    }

    // todo 初始是 mediumScale 比例时（阅读模式）放大或缩小时 zoomChange 变化很大，导致中心点偏移很大
    suspend fun transform(centroid: Offset, zoomChange: Float, rotationChange: Float) {
        stopAnimation("transform")

        val currentTransform = transform
        val currentScale = currentTransform.scaleX
        val targetScale = currentScale * zoomChange
        val limitedTargetScale = limitScale(targetScale)
        val addScale = limitedTargetScale - currentScale
        val addOffset = Offset(
            x = addScale * centroid.x * -1,
            y = addScale * centroid.y * -1
        )
        val currentOffset = currentTransform.offset
        val targetOffset = currentOffset + addOffset
        val targetTransform = currentTransform.copy(
            scale = ScaleFactor(limitedTargetScale),
            offset = targetOffset
        )
        val limitedTargetTransform = limitTransform(targetTransform)
        log {
            "transform. " +
                    "centroid=${centroid.toShortString()}, " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "rotationChange=${rotationChange.format(4)}, " +
                    "addScale=${addScale.format(4)}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "currentTransform=${currentTransform.toShortString()}, " +
                    "targetTransform=${targetTransform.toShortString()}, " +
                    "limitedTargetTransform=${limitedTargetTransform.toShortString()}"
        }

        transform = limitedTargetTransform
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

    suspend fun stopAnimation(caller: String) {
        val lastAnimatable = lastAnimatable
        if (lastAnimatable?.isRunning == true) {
            lastAnimatable.stop()
            log { "stopAnimation:$caller" }
        }
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScaleEnabled) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, transform.scaleX)
    }

    fun canDrag(horizontal: Boolean, direction: Int): Boolean =
        computeCanDrag(
            contentSize = contentSize.toCompatSize(),
            contentVisibleRect = contentVisibleRect.toCompatRectF(),
            horizontal = horizontal,
            direction = direction
        )

    private fun limitScale(
        scale: Float,
        minimumValue: Float = minScale,
        maximumValue: Float = maxScale
    ): Float {
        return scale.coerceIn(minimumValue = minimumValue, maximumValue = maximumValue)
    }

    private fun limitOffset(offset: Offset, scale: Float): Offset {
        val offsetBounds = computeOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
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