package com.github.panpf.zoomimage

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
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
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.compose.internal.computeContainerCentroidByTouchPosition
import com.github.panpf.zoomimage.compose.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.compose.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.compose.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.internal.computeScaleFactor
import com.github.panpf.zoomimage.compose.internal.computeScaleOffset
import com.github.panpf.zoomimage.compose.internal.computeScaleTargetOffset
import com.github.panpf.zoomimage.compose.internal.computeSupportOffsetBounds
import com.github.panpf.zoomimage.compose.internal.computeTransform
import com.github.panpf.zoomimage.compose.internal.containerCentroidToContentCentroid
import com.github.panpf.zoomimage.compose.internal.contentCentroidToContainerCentroid
import com.github.panpf.zoomimage.compose.internal.format
import com.github.panpf.zoomimage.compose.internal.name
import com.github.panpf.zoomimage.compose.internal.rotate
import com.github.panpf.zoomimage.compose.internal.supportReadMode
import com.github.panpf.zoomimage.compose.internal.toCompatScaleFactor
import com.github.panpf.zoomimage.compose.internal.toCompatSize
import com.github.panpf.zoomimage.compose.internal.toScaleMode
import com.github.panpf.zoomimage.compose.internal.toShortString
import com.github.panpf.zoomimage.core.Transform
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.computeSupportScales
import com.github.panpf.zoomimage.core.toShortString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun rememberZoomableState(
    threeStepScaleEnabled: Boolean = false,
    scaleAnimationSpec: ScaleAnimationSpec = ScaleAnimationSpec.Default,
    readMode: ReadMode = ReadMode.Default,
    debugMode: Boolean = false,
): ZoomableState {
    val state = rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState()
    }
    val flingAnimationSpec = rememberSplineBasedDecay<Float>()
    state.threeStepScaleEnabled = threeStepScaleEnabled
    state.scaleAnimationSpec = scaleAnimationSpec
    state.flingAnimationSpec = flingAnimationSpec
    state.readMode = readMode
    state.debugMode = debugMode
    LaunchedEffect(
        state.containerSize,
        state.contentSize,
        state.contentOriginSize,
        state.contentScale,
        state.contentAlignment,
        readMode,
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
) {

    // todo support click and long press
    // todo support rubber band effect

    private val scaleAnimatable = Animatable(initialScale)
    private val offsetXAnimatable = Animatable(initialTranslateX)
    private val offsetYAnimatable = Animatable(initialTranslateY)

    /**
     * Initial scale and translate for support
     */
    private var supportInitialTransform: Transform = Transform.Empty

    var containerSize: Size by mutableStateOf(Size.Unspecified)
    var contentSize: Size by mutableStateOf(Size.Unspecified)
    var contentOriginSize: Size by mutableStateOf(Size.Unspecified)
    var contentScale: ContentScale by mutableStateOf(ContentScale.Fit)
    var contentAlignment: Alignment by mutableStateOf(Alignment.Center)
    var threeStepScaleEnabled: Boolean = false
    var scaleAnimationSpec: ScaleAnimationSpec = ScaleAnimationSpec.Default
    var flingAnimationSpec: DecayAnimationSpec<Float>? = null
    var readMode: ReadMode = ReadMode.Default
    var debugMode: Boolean = false

    var minScale: Float by mutableStateOf(1f)
        private set
    var mediumScale: Float by mutableStateOf(1f)
        private set
    var maxScale: Float by mutableStateOf(1f)
        private set

    /**
     * The current scale value for [ZoomImage]
     */
    @get:FloatRange(from = 0.0)
    val scale: Float by derivedStateOf { scaleAnimatable.value }
    val baseScale: ScaleFactor by derivedStateOf {
        computeScaleFactor(
            srcSize = contentSize,
            dstSize = containerSize,
            contentScale = contentScale
        )
    }
    val displayScale: ScaleFactor by derivedStateOf {
        baseScale.times(scale)
    }

    val offset: Offset by derivedStateOf {
        Offset(
            x = offsetXAnimatable.value,
            y = offsetYAnimatable.value
        )
    }
    var offsetBounds: Rect? by mutableStateOf(null)
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    val baseOffset: Offset by derivedStateOf {
        computeScaleOffset(
            srcSize = contentSize,
            dstSize = containerSize,
            scale = contentScale,
            alignment = contentAlignment,
        )
    }

    @Suppress("unused")
    val displayOffset: Offset by derivedStateOf {
        val baseOffset = baseOffset
        val supportOffset = offset
        baseOffset + supportOffset
    }

    val rotation: Float by mutableStateOf(0f)    // todo support rotation
    val transformOrigin = TransformOrigin(0f, 0f)

    val containerVisibleRect: Rect by derivedStateOf {
        computeContainerVisibleRect(containerSize, scale, offset)
    }
    val contentVisibleRect: Rect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = scale,
            offset = offset,
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
        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val containerSize = containerSize
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        if (containerSize.isUnspecified || containerSize.isEmpty()
            || contentSize.isUnspecified || contentSize.isEmpty()
        ) {
            minScale = 1.0f
            mediumScale = 1.0f
            maxScale = 1.0f
            supportInitialTransform = Transform.Empty
        } else {
            val rotatedContentSize = contentSize.rotate(rotation.roundToInt())
            val rotatedContentOriginSize = contentOriginSize.rotate(rotation.roundToInt())
            val scales = computeSupportScales(
                contentSize = rotatedContentSize.toCompatSize(),
                contentOriginSize = rotatedContentOriginSize.toCompatSize(),
                containerSize = containerSize.toCompatSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(rotatedContentSize, containerSize)
                    .toCompatScaleFactor()
            )
            minScale = scales[0]
            mediumScale = scales[1]
            maxScale = scales[2]
            val readModeResult = readMode.enabled
                    && contentScale.supportReadMode()
                    && readMode.decider.should(
                srcSize = rotatedContentSize.toCompatSize(),
                dstSize = containerSize.toCompatSize()
            )
            val baseTransform = computeTransform(
                srcSize = rotatedContentSize,
                dstSize = containerSize,
                scale = contentScale,
                alignment = contentAlignment,
            )
            supportInitialTransform = if (readModeResult) {
                computeReadModeTransform(
                    srcSize = rotatedContentSize,
                    dstSize = containerSize,
                    scale = contentScale,
                    alignment = contentAlignment,
                ).let {
                    Transform(
                        scaleX = it.scaleX / baseTransform.scaleX,
                        scaleY = it.scaleY / baseTransform.scaleY,
                        offsetX = it.offsetX / baseTransform.scaleX,
                        offsetY = it.offsetY / baseTransform.scaleY,
                    )
                }
            } else {
                Transform.Empty
            }
        }
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
                    "supportInitialTransform=${supportInitialTransform.toShortString()}"
        }
        scaleAnimatable.snapTo(supportInitialTransform.scaleX)
        offsetXAnimatable.snapTo(supportInitialTransform.offsetX)
        offsetYAnimatable.snapTo(supportInitialTransform.offsetY)
        updateOffsetBounds("reset")
    }


    suspend fun snapScaleTo(targetScale: Float) {
        stopAllAnimation("snapScaleTo")
        val currentScale = scale
        val limitedTargetScale = limitScale(targetScale)
        log {
            "snapScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }
        coroutineScope {
            launch {
                scaleAnimatable.snapTo(limitedTargetScale)
                updateOffsetBounds("snapScaleTo")
            }
        }
    }

    suspend fun animateScaleTo(targetScale: Float) {
        stopAllAnimation("animateScaleTo")
        val currentScale = scale
        val limitedTargetScale = limitScale(targetScale)
        val animationSpec = tween<Float>(
            durationMillis = scaleAnimationSpec.durationMillis,
            easing = scaleAnimationSpec.easing
        )
        log {
            "animateScaleTo. " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = limitedTargetScale,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
                updateOffsetBounds("animateScaleTo")
            }
        }
    }

    suspend fun snapScaleBy(addScale: Float) {
        stopAllAnimation("snapScaleBy")
        val currentScale = scale
        val targetScale = currentScale * addScale
        val limitedTargetScale = limitScale(currentScale * addScale)
        log {
            "snapScaleBy. " +
                    "addScale=${addScale.format(4)}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, "
        }
        coroutineScope {
            launch {
                scaleAnimatable.snapTo(limitedTargetScale)
                updateOffsetBounds("snapScaleBy")
            }
        }
    }

    suspend fun snapOffsetTo(targetOffset: Offset) {
        stopAllAnimation("snapOffsetTo")
        val currentOffset = offset
        val limitedTargetOffset = limitOffset(targetOffset)
        log {
            "snapOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                offsetXAnimatable.snapTo(limitedTargetOffset.x)
                offsetYAnimatable.snapTo(limitedTargetOffset.y)
            }
        }
    }

    suspend fun animateOffsetTo(targetOffset: Offset) {
        stopAllAnimation("animateOffsetTo")
        val currentOffset = offset
        val limitedTargetOffset = limitOffset(targetOffset)
        val animationSpec = tween<Float>(
            durationMillis = scaleAnimationSpec.durationMillis,
            easing = scaleAnimationSpec.easing
        )
        log {
            "animateOffsetTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                offsetXAnimatable.animateTo(
                    targetValue = limitedTargetOffset.x,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
            launch {
                offsetYAnimatable.animateTo(
                    targetValue = limitedTargetOffset.y,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
        }
    }

    suspend fun snapOffsetBy(addOffset: Offset) {
        stopAllAnimation("snapOffsetBy")
        val currentOffset = offset
        val targetOffset = currentOffset.plus(addOffset)
        val limitedTargetOffset = limitOffset(targetOffset)
        log {
            "snapOffsetBy. " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                offsetXAnimatable.snapTo(limitedTargetOffset.x)
                offsetYAnimatable.snapTo(limitedTargetOffset.y)
            }
        }
    }

    suspend fun animateLocation(contentCentroid: Centroid, targetScale: Float = scale) {
        stopAllAnimation("animateLocation")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentOffset = offset

        val limitedTargetScale = limitScale(targetScale)
        val animationSpec = tween<Float>(
            durationMillis = scaleAnimationSpec.durationMillis,
            easing = scaleAnimationSpec.easing
        )
        val futureOffsetBounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = limitedTargetScale
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = contentCentroid
        )
        val limitedTargetOffset = computeScaleTargetOffset(
            containerSize = containerSize,
            scale = limitedTargetScale,
            containerCentroid = containerCentroid
        ).let { limitOffset(it, futureOffsetBounds) }
        log {
            "animateLocation. " +
                    "contentCentroid=${contentCentroid.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "futureBounds=${futureOffsetBounds.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, " +
                    "Offset: ${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        clearOffsetBounds("animateLocation")
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = limitedTargetScale,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
                updateOffsetBounds("animateLocation")
            }
            launch {
                offsetXAnimatable.animateTo(
                    targetValue = limitedTargetOffset.x,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
            launch {
                offsetYAnimatable.animateTo(
                    targetValue = limitedTargetOffset.y,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
        }
    }

    suspend fun animateLocation(touch: Offset, targetScale: Float = scale) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentOffset = offset
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            offset = currentOffset,
            touch = touch
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        log {
            "animateLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "contentCentroid=${contentCentroid.toShortString()}"
        }
        animateLocation(
            contentCentroid = contentCentroid,
            targetScale = targetScale,
        )
    }

    suspend fun snapLocation(contentCentroid: Centroid, targetScale: Float = scale) {
        stopAllAnimation("snapLocation")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentOffset = offset

        val limitedTargetValue = limitScale(targetScale)
        val futureOffsetBounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = limitedTargetValue
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = contentCentroid
        )
        val limitedTargetOffset = computeScaleTargetOffset(
            containerSize = containerSize,
            scale = limitedTargetValue,
            containerCentroid = containerCentroid
        ).let { limitOffset(it, futureOffsetBounds) }

        log {
            "snapLocation. " +
                    "contentCentroid=${contentCentroid.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "futureBounds=${futureOffsetBounds.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetValue.format(4)}, " +
                    "offset: ${currentOffset.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            scaleAnimatable.snapTo(limitedTargetValue)
            updateOffsetBounds("snapLocation")
            offsetXAnimatable.snapTo(targetValue = limitedTargetOffset.x)
            offsetYAnimatable.snapTo(targetValue = limitedTargetOffset.y)
        }
    }

    suspend fun snapLocation(touch: Offset, targetScale: Float = scale) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentOffset = offset
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            offset = currentOffset,
            touch = touch
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        log {
            "snapLocation. " +
                    "touch=${touch.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "contentCentroid=${contentCentroid.toShortString()}"
        }
        snapLocation(
            contentCentroid = contentCentroid,
            targetScale = targetScale
        )
    }

    suspend fun switchScale(contentCentroid: Centroid = Centroid(0.5f, 0.5f)): Float {
        val nextScale = getNextStepScale()
        animateLocation(
            contentCentroid = contentCentroid,
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
        stopAllAnimation("transform")
        val currentScale = scale
        val newScale = (currentScale * zoomChange)
            .coerceIn(minimumValue = minScale, maximumValue = maxScale)
        val addScale = newScale - currentScale
        val addOffset = Offset(
            x = addScale * centroid.x * -1,
            y = addScale * centroid.y * -1
        )
        val currentOffset = offset
        val targetOffset = currentOffset + addOffset
        log {
            "transform. " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "centroid=${centroid.toShortString()}, " +
                    "addScale=${addScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${newScale.format(4)}, " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "offset=${currentOffset.toShortString()} -> ${targetOffset.toShortString()}"
        }
        coroutineScope {
            scaleAnimatable.snapTo(newScale)
            updateOffsetBounds("snapScaleTo")
            offsetXAnimatable.snapTo(targetValue = targetOffset.x)
            offsetYAnimatable.snapTo(targetValue = targetOffset.y)
        }
    }

    suspend fun fling(velocity: Velocity) {
        stopAllAnimation("fling")
        log { "fling. velocity=$velocity, offset=${offset.toShortString()}" }
        val animationSpec = flingAnimationSpec ?: exponentialDecay()
        coroutineScope {
            launch {
                val startX = offsetXAnimatable.value
                offsetXAnimatable.animateDecay(velocity.x, animationSpec) {
                    val offsetX = this.value
                    val distanceX = offsetX - startX
                    log { "fling. running. velocity=$velocity, startX=$startX, offsetX=$offsetX, distanceX=$distanceX" }
                }
            }
            launch {
                val startY = offsetYAnimatable.value
                offsetYAnimatable.animateDecay(velocity.y, animationSpec) {
                    val offsetY = this.value
                    val distanceY = offsetY - startY
                    log { "fling. running. velocity=$velocity, startY=$startY, offsetY=$offsetY, distanceY=$distanceY" }
                }
            }
        }
    }

    suspend fun stopAllAnimation(caller: String) {
        if (scaleAnimatable.isRunning) {
            scaleAnimatable.stop()
            log { "stopAllAnimation. stop scale animation. scale=${scale.format(4)}" }
            updateOffsetBounds("stopAllAnimation:$caller")
        }
        if (offsetXAnimatable.isRunning || offsetYAnimatable.isRunning) {
            offsetXAnimatable.stop()
            offsetYAnimatable.stop()
            log { "stopAllAnimation. stop offset animation. offset=${offset.toShortString()}" }
        }
    }

    fun getNextStepScale(): Float {
        val stepScales = if (threeStepScaleEnabled) {
            floatArrayOf(minScale, mediumScale, maxScale)
        } else {
            floatArrayOf(minScale, mediumScale)
        }
        return calculateNextStepScale(stepScales, scale)
    }

    override fun toString(): String =
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "minScale=${minScale.format(4)}, " +
                "mediumScale=${mediumScale.format(4)}, " +
                "maxScale=${maxScale.format(4)}, " +
                "scale=${scale.format(4)}, " +
                "offset=${offset.toShortString()}" +
                ")"

    private fun updateOffsetBounds(caller: String) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val bounds = computeSupportOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = currentScale
        )
        this.offsetBounds = bounds
        log {
            "updateOffsetBounds. " +
                    "$caller. " +
                    "bounds=${bounds.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "scale=$currentScale"
        }
        offsetXAnimatable.updateBounds(lowerBound = bounds.left, upperBound = bounds.right)
        offsetYAnimatable.updateBounds(lowerBound = bounds.top, upperBound = bounds.bottom)
    }

    private fun clearOffsetBounds(@Suppress("SameParameterValue") caller: String) {
        log { "updateOffsetBounds. ${caller}. clear" }
        this.offsetBounds = null
        offsetXAnimatable.updateBounds(lowerBound = null, upperBound = null)
        offsetYAnimatable.updateBounds(lowerBound = null, upperBound = null)
    }

    private fun limitScale(
        scale: Float,
        minimumValue: Float = minScale,
        maximumValue: Float = maxScale
    ): Float {
        return scale.coerceIn(minimumValue = minimumValue, maximumValue = maximumValue)
    }

    private fun limitOffset(offset: Offset, bounds: Rect? = offsetBounds): Offset {
        val offsetBounds = bounds ?: offsetBounds ?: return offset
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

    private fun log(message: () -> String) {
        if (debugMode) {
            Log.d("MyZoomState", message())
        }
    }

    companion object {

        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = mapSaver(
            save = {
                mapOf(
                    "scale" to it.scale,
                    "offsetX" to it.offset.x,
                    "offsetY" to it.offset.y,
                )
            },
            restore = {
                ZoomableState(
                    initialScale = it["scale"] as Float,
                    initialTranslateX = it["offsetX"] as Float,
                    initialTranslateY = it["offsetY"] as Float,
                )
            }
        )
    }
}