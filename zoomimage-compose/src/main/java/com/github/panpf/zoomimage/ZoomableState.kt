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
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.util.lerp
import com.github.panpf.zoomimage.compose.Transform
import com.github.panpf.zoomimage.compose.concat
import com.github.panpf.zoomimage.compose.div
import com.github.panpf.zoomimage.compose.internal.ScaleFactor
import com.github.panpf.zoomimage.compose.internal.computeContainerOriginByTouchPosition
import com.github.panpf.zoomimage.compose.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.internal.computeScaleTargetOffset
import com.github.panpf.zoomimage.compose.internal.computeSupportOffsetBounds
import com.github.panpf.zoomimage.compose.internal.computeTransform
import com.github.panpf.zoomimage.compose.internal.containerOriginToContentOrigin
import com.github.panpf.zoomimage.compose.internal.contentOriginToContainerOrigin
import com.github.panpf.zoomimage.compose.internal.format
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
        if (state.contentSize.isUnspecified && state.containerSize.isSpecified) {
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

    var containerSize: Size by mutableStateOf(Size.Unspecified)
    var contentSize: Size by mutableStateOf(Size.Unspecified)
    var contentOriginSize: Size by mutableStateOf(Size.Unspecified)
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

    var offsetBounds: Rect? by mutableStateOf(null)
        private set

    val containerVisibleRect: Rect by derivedStateOf {
        computeContainerVisibleRect(containerSize, transform.scale.scaleX, transform.offset)
    }
    val contentVisibleRect: Rect by derivedStateOf {
        // todo 长微博图片示例，显示区域框框底部没有到底，但是图片已经到底了
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = transform.scale.scaleX,
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
        val supportInitialTransform: Transform
        if (containerSize.isUnspecified || containerSize.isEmpty()
            || contentSize.isUnspecified || contentSize.isEmpty()
        ) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            baseTransform = Transform.Origin
            supportInitialTransform = Transform.Origin
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
            supportInitialTransform = if (readModeResult) {
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
        updateOffsetBounds("reset", supportInitialTransform.scale.scaleX)
        val limitInitialSupportTransform = limitTransform(supportInitialTransform)
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
                    "supportInitialTransform=${supportInitialTransform.toShortString()}, " +
                    "limitInitialSupportTransform=${limitInitialSupportTransform.toShortString()}"
        }
        updateTransform(limitInitialSupportTransform, "reset")
    }


    suspend fun snapScaleBy(addScale: Float) {
        stopAnimation("snapScaleBy")
        val currentScale = transform.scale.scaleX
        val targetScale = currentScale * addScale
        val limitedTargetScale = limitScale(currentScale * addScale)
        // todo 构建新的 Transform，然后 limited，打印日志
        log {
            "snapScaleBy. " +
                    "addScale=${addScale.format(4)}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }
        updateTransform(
            newTransform = transform.copy(scale = ScaleFactor(limitedTargetScale)),
            caller = "snapScaleBy"
        )
    }

    suspend fun snapScaleTo(targetScale: Float) {
        stopAnimation("snapScaleTo")
        val currentScale = transform.scale.scaleX
        val limitedTargetScale = limitScale(targetScale)
        log {
            "snapScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }
        updateTransform(
            newTransform = transform.copy(scale = ScaleFactor(limitedTargetScale)),
            caller = "snapScaleTo"
        )
    }

    suspend fun animateScaleTo(targetScale: Float) {
        stopAnimation("animateScaleTo")
        val currentScale = transform.scale.scaleX
        val limitedTargetScale = limitScale(targetScale)
        val animationSpec = tween<Float>(
            durationMillis = defaultAnimationSpec.durationMillis,
            easing = defaultAnimationSpec.easing
        )
        log {
            "animateScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }

        val scaleAnimatable = Animatable(currentScale)
        this.lastAnimatable = scaleAnimatable
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = limitedTargetScale,
                    animationSpec = animationSpec,
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    updateTransform(
                        newTransform = transform.copy(scale = ScaleFactor(value)),
                        caller = "animateScaleTo"
                    )
                }
            }
        }
    }


    suspend fun snapOffsetBy(addOffset: Offset) {
        stopAnimation("snapOffsetBy")
        val currentOffset = transform.offset
        val targetOffset = currentOffset.plus(addOffset)
        val limitedTargetOffset = limitOffset(targetOffset)
        log {
            "snapOffsetBy. " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        updateTransform(
            newTransform = transform.copy(offset = limitedTargetOffset),
            caller = "snapOffsetBy"
        )
    }

    suspend fun snapOffsetTo(targetOffset: Offset) {
        stopAnimation("snapOffsetTo")
        val currentOffset = transform.offset
        val limitedTargetOffset = limitOffset(targetOffset)
        log {
            "snapOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        updateTransform(
            newTransform = transform.copy(offset = limitedTargetOffset),
            caller = "snapOffsetTo"
        )
    }

    suspend fun animateOffsetTo(targetOffset: Offset) {
        stopAnimation("animateOffsetTo")
        val currentOffset = transform.offset
        val limitedTargetOffset = limitOffset(targetOffset)
        val animationSpec = tween<Float>(
            durationMillis = defaultAnimationSpec.durationMillis,
            easing = defaultAnimationSpec.easing
        )
        log {
            "animateOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        val offsetAnimatable = Animatable(0f)
        this.lastAnimatable = offsetAnimatable
        coroutineScope {
            launch {
                offsetAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec,
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val offset = androidx.compose.ui.geometry.lerp(
                        start = currentOffset,
                        stop = limitedTargetOffset,
                        fraction = value
                    )
                    updateTransform(
                        newTransform = transform.copy(offset = offset),
                        caller = "animateOffsetTo"
                    )
                }
            }
        }
    }


    suspend fun snapLocation(contentOrigin: Origin, targetScale: Float = transform.scale.scaleX) {
        stopAnimation("snapLocation")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = transform.scale.scaleX
        val currentOffset = transform.offset

        val limitedTargetValue = limitScale(targetScale)
        val futureOffsetBounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = limitedTargetValue
        )
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val targetOffset = computeScaleTargetOffset(
            containerSize = containerSize,
            scale = limitedTargetValue,
            containerOrigin = containerOrigin
        )
        val limitedTargetOffset = limitOffset(targetOffset, futureOffsetBounds)

        log {
            "snapLocation. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "futureBounds=${futureOffsetBounds.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetValue.format(4)}, " +
                    "offset: ${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        updateTransform(
            newTransform = transform.copy(
                scale = ScaleFactor(limitedTargetValue, limitedTargetValue),
                offset = limitedTargetOffset
            ),
            caller = "snapLocation"
        )
    }

    suspend fun snapLocation(touch: Offset, targetScale: Float = transform.scale.scaleX) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = transform.scale.scaleX
        val currentOffset = transform.offset
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            offset = currentOffset,
            touch = touch
        )
        val contentOrigin = containerOriginToContentOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerOrigin = containerOrigin
        )
        log {
            "snapLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "contentOrigin=${contentOrigin.toShortString()}"
        }
        snapLocation(
            contentOrigin = contentOrigin,
            targetScale = targetScale
        )
    }

    suspend fun animateLocation(
        contentOrigin: Origin,
        targetScale: Float = transform.scale.scaleX
    ) {
        stopAnimation("animateLocation")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = transform.scale.scaleX
        val currentOffset = transform.offset

        val limitedTargetScale = limitScale(targetScale)
        val animationSpec = tween<Float>(
            durationMillis = defaultAnimationSpec.durationMillis,
            easing = defaultAnimationSpec.easing
        )
        val futureOffsetBounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = limitedTargetScale
        )
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val targetOffset = computeScaleTargetOffset(
            containerSize = containerSize,
            scale = limitedTargetScale,
            containerOrigin = containerOrigin
        )
        val limitedTargetOffset = limitOffset(targetOffset, futureOffsetBounds)
        log {
            "animateLocation. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "futureBounds=${futureOffsetBounds.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, " +
                    "offset: ${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        val locationAnimatable = Animatable(0f)
        this.lastAnimatable = locationAnimatable
        coroutineScope {
            launch {
                locationAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = animationSpec,
                    initialVelocity = defaultAnimationSpec.initialVelocity,
                ) {
                    val scale =
                        lerp(start = currentScale, stop = limitedTargetScale, fraction = value)
                    val offset = androidx.compose.ui.geometry.lerp(
                        start = currentOffset,
                        stop = limitedTargetOffset,
                        fraction = value
                    )
                    // todo 还是得需要 clearOffsetBounds
                    updateTransform(
                        newTransform = transform.copy(scale = ScaleFactor(scale), offset = offset),
                        caller = "animateLocation"
                    )
                }
            }
        }
    }

    suspend fun animateLocation(touch: Offset, targetScale: Float = transform.scale.scaleX) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = transform.scale.scaleX
        val currentOffset = transform.offset
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            offset = currentOffset,
            touch = touch
        )
        val contentOrigin = containerOriginToContentOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerOrigin = containerOrigin
        )
        log {
            "animateLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "contentOrigin=${contentOrigin.toShortString()}"
        }
        animateLocation(
            contentOrigin = contentOrigin,
            targetScale = targetScale,
        )
    }


    suspend fun switchScale(contentOrigin: Origin = Origin(0.5f, 0.5f)): Float {
        val nextScale = getNextStepScale()
        animateLocation(
            contentOrigin = contentOrigin,
            targetScale = nextScale
        )
        return nextScale
    }

    suspend fun switchScale(touch: Offset): Float {
        val nextScale = getNextStepScale()
        animateLocation(
            touch = touch,
            targetScale = nextScale
        )
        return nextScale
    }

    suspend fun transform(
        centroid: Offset,
        zoomChange: Float,
        @Suppress("UNUSED_PARAMETER") rotationChange: Float
    ) {
        // todo 初始是 mediumScale 比例时（阅读模式）放大或缩小时 zoomChange 变化很大，导致中心点偏移很大
        stopAnimation("transform")
        val currentScale = transform.scale.scaleX
        val targetScale = currentScale * zoomChange
        val limitedTargetScale = limitScale(targetScale)
        val addScale = limitedTargetScale - currentScale
        val addOffset = Offset(
            x = addScale * centroid.x * -1,
            y = addScale * centroid.y * -1
        )
        val currentOffset = transform.offset
        val targetOffset = currentOffset + addOffset
        val limitedTargetOffset = limitOffset(targetOffset)
        log {
            "transform. " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "centroid=${centroid.toShortString()}, " +
                    "addScale=${addScale.format(4)}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        updateTransform(
            newTransform = transform.copy(
                scale = ScaleFactor(limitedTargetScale),
                offset = limitedTargetOffset
            ),
            caller = "transform"
        )
    }

    suspend fun fling(velocity: Velocity, density: Density) {
        stopAnimation("fling")
        val currentOffset = transform.offset
        val flingAnimatable = Animatable(
            initialValue = currentOffset,
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
                    val targetOffset = this.value
                    val limitedTargetOffset = limitOffset(targetOffset)
                    val distance = limitedTargetOffset - currentOffset
                    log {
                        "fling. running. " +
                                "velocity=$velocity, " +
                                "startOffset=${currentOffset.toShortString()}, " +
                                "currentOffset=${limitedTargetOffset.toShortString()}, " +
                                "distance=$distance"
                    }
                    updateTransform(
                        newTransform = transform.copy(offset = limitedTargetOffset),
                        caller = "fling"
                    )
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
        return calculateNextStepScale(stepScales, transform.scale.scaleX)
    }

    fun canDrag(horizontal: Boolean, direction: Int): Boolean =
        computeCanDrag(
            contentSize = contentSize.toCompatSize(),
            contentVisibleRect = contentVisibleRect.toCompatRectF(),
            horizontal = horizontal,
            direction = direction
        )

    private fun updateTransform(newTransform: Transform, caller: String) {
        val oldTransform = transform
        if (oldTransform == newTransform) return
        if (oldTransform.scale != newTransform.scale) {
            updateOffsetBounds("updateTransform:$caller", newTransform.scale.scaleX)
        }
        transform = newTransform.copy(offset = limitOffset(newTransform.offset))
    }

    private fun updateOffsetBounds(caller: String, scale: Float = transform.scale.scaleX) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val bounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = scale
        )
        this.offsetBounds = bounds
        log {
            "updateOffsetBounds. " +
                    "$caller. " +
                    "bounds=${bounds.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "scale=$scale"
        }
        // todo 不更改
//        offsetXAnimatable.updateBounds(lowerBound = bounds.left, upperBound = bounds.right)
//        offsetYAnimatable.updateBounds(lowerBound = bounds.top, upperBound = bounds.bottom)
    }

//    private fun clearOffsetBounds(@Suppress("SameParameterValue") caller: String) {
//        log { "updateOffsetBounds. ${caller}. clear" }
//        this.offsetBounds = null
//        offsetXAnimatable.updateBounds(lowerBound = null, upperBound = null)
//        offsetYAnimatable.updateBounds(lowerBound = null, upperBound = null)
//    }

    private fun limitScale(
        scale: Float,
        minimumValue: Float = minScale,
        maximumValue: Float = maxScale
    ): Float {
        return scale.coerceIn(minimumValue = minimumValue, maximumValue = maximumValue)
    }

    private fun limitOffset(offset: Offset, bounds: Rect? = offsetBounds): Offset {
        val offsetBounds = bounds ?: offsetBounds ?: return offset
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

    private fun limitTransform(
        transform: Transform,
        minimumValue: Float = minScale,
        maximumValue: Float = maxScale,
        bounds: Rect? = offsetBounds
    ): Transform {
        return transform.copy(
            scale = ScaleFactor(
                scaleX = limitScale(
                    transform.scale.scaleX,
                    minimumValue = minimumValue,
                    maximumValue = maximumValue
                ),
                scaleY = limitScale(
                    transform.scale.scaleY,
                    minimumValue = minimumValue,
                    maximumValue = maximumValue
                ),
            ),
            offset = limitOffset(transform.offset, bounds)
        )
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
                    "scale" to it.transform.scale.scaleX,
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