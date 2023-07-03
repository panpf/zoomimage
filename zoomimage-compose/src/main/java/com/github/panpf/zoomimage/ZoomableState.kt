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
import androidx.compose.ui.unit.Velocity
import com.github.panpf.zoomimage.core.internal.calculateNextStepScale
import com.github.panpf.zoomimage.core.internal.computeSupportScales
import com.github.panpf.zoomimage.internal.ScaleFactor
import com.github.panpf.zoomimage.internal.Transform
import com.github.panpf.zoomimage.internal.Translation
import com.github.panpf.zoomimage.internal.computeContainerCentroidByTouchPosition
import com.github.panpf.zoomimage.internal.computeContainerVisibleRect
import com.github.panpf.zoomimage.internal.computeContentInContainerRect
import com.github.panpf.zoomimage.internal.computeContentVisibleRect
import com.github.panpf.zoomimage.internal.computeReadModeTransform
import com.github.panpf.zoomimage.internal.computeScaleTargetTranslation
import com.github.panpf.zoomimage.internal.computeScaleTranslation
import com.github.panpf.zoomimage.internal.computeSupportTranslationBounds
import com.github.panpf.zoomimage.internal.computeTransform
import com.github.panpf.zoomimage.internal.containerCentroidToContentCentroid
import com.github.panpf.zoomimage.internal.contentCentroidToContainerCentroid
import com.github.panpf.zoomimage.internal.format
import com.github.panpf.zoomimage.internal.name
import com.github.panpf.zoomimage.internal.plus
import com.github.panpf.zoomimage.internal.rotate
import com.github.panpf.zoomimage.internal.supportReadMode
import com.github.panpf.zoomimage.internal.toOffset
import com.github.panpf.zoomimage.internal.toScaleFactor
import com.github.panpf.zoomimage.internal.toScaleMode
import com.github.panpf.zoomimage.internal.toShortString
import com.github.panpf.zoomimage.internal.toSize
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
    private val translationXAnimatable = Animatable(initialTranslateX)
    private val translationYAnimatable = Animatable(initialTranslateY)

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
        val contentSize = contentSize
        val containerSize = containerSize
        if (containerSize.isUnspecified || containerSize.isEmpty()
            || contentSize.isUnspecified || contentSize.isEmpty()
        ) {
            ScaleFactor(1f, 1f)
        } else {
            contentScale.computeScaleFactor(contentSize, containerSize).toScaleFactor()
        }
    }
    val displayScale: ScaleFactor by derivedStateOf {   // todo 换成 compose 的 ScaleFactor
        baseScale.times(scale)
    }

    /**
     * The current translation value for [ZoomImage]
     */
    val translation: Translation by derivedStateOf {    // todo 换成 compose 的 Offset
        Translation(
            translationX = translationXAnimatable.value,
            translationY = translationYAnimatable.value
        )
    }
    var translationBounds: Rect? by mutableStateOf(null)
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    val baseTranslation: Translation by derivedStateOf {
        computeScaleTranslation(
            srcSize = contentSize,
            dstSize = containerSize,
            scale = contentScale,
            alignment = contentAlignment,
        )
    }

    @Suppress("unused")
    val displayTranslation: Translation by derivedStateOf {
        val baseTranslation = baseTranslation
        val translation = translation
        Translation(
            translationX = baseTranslation.translationX + translation.translationX,
            translationY = baseTranslation.translationY + translation.translationY
        )
    }

    val rotation: Float by mutableStateOf(0f)    // todo support rotation
    val transformOrigin = TransformOrigin(0f, 0f)

    val containerVisibleRect: Rect by derivedStateOf {
        computeContainerVisibleRect(containerSize, scale, translation)
    }
    val contentVisibleRect: Rect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            scale = scale,
            translation = translation,
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
                contentSize = rotatedContentSize.toSize(),
                contentOriginSize = rotatedContentOriginSize.toSize(),
                containerSize = containerSize.toSize(),
                scaleMode = contentScale.toScaleMode(),
                baseScale = contentScale.computeScaleFactor(rotatedContentSize, containerSize)
                    .toScaleFactor()
            )
            minScale = scales[0]
            mediumScale = scales[1]
            maxScale = scales[2]
            val readModeResult = readMode.enabled
                    && contentScale.supportReadMode()
                    && readMode.decider.should(
                srcSize = rotatedContentSize.toSize(),
                dstSize = containerSize.toSize()
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
                        translationX = it.translationX / baseTransform.scaleX,
                        translationY = it.translationY / baseTransform.scaleY,
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
        translationXAnimatable.snapTo(supportInitialTransform.translationX)
        translationYAnimatable.snapTo(supportInitialTransform.translationY)
        updateTranslationBounds("reset")
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
                updateTranslationBounds("snapScaleTo")
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
                updateTranslationBounds("animateScaleTo")
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
                updateTranslationBounds("snapScaleBy")
            }
        }
    }

    suspend fun snapTranslationTo(targetOffset: Offset) {
        stopAllAnimation("snapTranslationTo")
        val currentTranslation = translation
        val limitedTargetOffset = limitTranslation(targetOffset)
        log {
            "snapTranslationTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "translation=${currentTranslation.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                translationXAnimatable.snapTo(limitedTargetOffset.x)
                translationYAnimatable.snapTo(limitedTargetOffset.y)
            }
        }
    }

    suspend fun animateTranslationTo(targetOffset: Offset) {
        stopAllAnimation("animateTranslationTo")
        val currentTranslation = translation
        val limitedTargetOffset = limitTranslation(targetOffset)
        val animationSpec = tween<Float>(
            durationMillis = scaleAnimationSpec.durationMillis,
            easing = scaleAnimationSpec.easing
        )
        log {
            "animateTranslationTo. " +
                    "targetOffset=${targetOffset.toShortString()}, " +
                    "translation=${currentTranslation.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                translationXAnimatable.animateTo(
                    targetValue = limitedTargetOffset.x,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
            launch {
                translationYAnimatable.animateTo(
                    targetValue = limitedTargetOffset.y,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
        }
    }

    suspend fun snapTranslationBy(addOffset: Offset) {
        stopAllAnimation("snapTranslationBy")
        val currentTranslation = translation
        val targetTranslation = currentTranslation.plus(addOffset)
        val limitedTargetOffset = limitTranslation(targetTranslation.toOffset())
        log {
            "snapTranslationBy. " +
                    "addOffset=${addOffset.toShortString()}, " +
                    "targetOffset=${targetTranslation.toShortString()}, " +
                    "translation=${currentTranslation.toShortString()} -> ${limitedTargetOffset.toShortString()}"
        }
        coroutineScope {
            launch {
                translationXAnimatable.snapTo(limitedTargetOffset.x)
                translationYAnimatable.snapTo(limitedTargetOffset.y)
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
        val currentTranslation = translation

        val limitedTargetScale = limitScale(targetScale)
        val animationSpec = tween<Float>(
            durationMillis = scaleAnimationSpec.durationMillis,
            easing = scaleAnimationSpec.easing
        )
        val futureTranslationBounds = computeSupportTranslationBounds(
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
        val limitedTargetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = limitedTargetScale,
            containerCentroid = containerCentroid
        ).let { limitTranslation(it.toOffset(), futureTranslationBounds) }
        log {
            "animateLocation. " +
                    "contentCentroid=${contentCentroid.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "futureBounds=${futureTranslationBounds.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetScale.format(4)}, " +
                    "translation: ${currentTranslation.toShortString()} -> ${limitedTargetTranslation.toShortString()}"
        }
        clearTranslationBounds("animateLocation")
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = limitedTargetScale,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
                updateTranslationBounds("animateLocation")
            }
            launch {
                translationXAnimatable.animateTo(
                    targetValue = limitedTargetTranslation.x,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
            launch {
                translationYAnimatable.animateTo(
                    targetValue = limitedTargetTranslation.y,
                    animationSpec = animationSpec,
                    initialVelocity = scaleAnimationSpec.initialVelocity,
                )
            }
        }
    }

    suspend fun animateLocation(touchOffset: Offset, targetScale: Float = scale) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            translation = currentTranslation,
            touchPosition = touchOffset
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
                    "touchOffset=${touchOffset.toShortString()}, " +
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
        val currentTranslation = translation

        val limitedTargetValue = limitScale(targetScale)
        val futureTranslationBounds = computeSupportTranslationBounds(
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
        val limitedTargetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = limitedTargetValue,
            containerCentroid = containerCentroid
        ).let { limitTranslation(it.toOffset(), futureTranslationBounds) }

        log {
            "snapLocation. " +
                    "contentCentroid=${contentCentroid.toShortString()}, " +
                    "targetScale=${targetScale.format(4)}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "futureBounds=${futureTranslationBounds.toShortString()}, " +
                    "scale: ${currentScale.format(4)} -> ${limitedTargetValue.format(4)}, " +
                    "translation: ${currentTranslation.toShortString()} -> ${limitedTargetTranslation.toShortString()}"
        }
        coroutineScope {
            scaleAnimatable.snapTo(limitedTargetValue)
            updateTranslationBounds("snapLocation")
            translationXAnimatable.snapTo(targetValue = limitedTargetTranslation.x)
            translationYAnimatable.snapTo(targetValue = limitedTargetTranslation.y)
        }
    }

    suspend fun snapLocation(touchOffset: Offset, targetScale: Float = scale) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation
        val containerCentroid = computeContainerCentroidByTouchPosition(
            containerSize = containerSize,
            scale = currentScale,
            translation = currentTranslation,
            touchPosition = touchOffset
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
                    "touchOffset=${touchOffset.toShortString()}, " +
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

    suspend fun switchScale(touchOffset: Offset): Float {
        val nextScale = getNextStepScale()
        animateLocation(
            touchOffset = touchOffset,
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
        val addTranslation = Offset(
            x = addScale * centroid.x * -1,
            y = addScale * centroid.y * -1
        )
        val currentTranslation = translation
        val targetTranslation = Translation(
            translationX = currentTranslation.translationX + addTranslation.x,
            translationY = currentTranslation.translationY + addTranslation.y
        )
        log {
            "transform. " +
                    "zoomChange=${zoomChange.format(4)}, " +
                    "touchCentroid=${centroid.toShortString()}, " +
                    "addScale=${addScale.format(4)}, " +
                    "scale=${currentScale.format(4)} -> ${newScale.format(4)}, " +
                    "addTranslation=${addTranslation.toShortString()}, " +
                    "translation=${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}"
        }
        coroutineScope {
            scaleAnimatable.snapTo(newScale)
            updateTranslationBounds("snapScaleTo")
            translationXAnimatable.snapTo(targetValue = targetTranslation.translationX)
            translationYAnimatable.snapTo(targetValue = targetTranslation.translationY)
        }
    }

    suspend fun fling(velocity: Velocity) {
        stopAllAnimation("fling")
        log { "fling. velocity=$velocity, translation=${translation.toShortString()}" }
        val animationSpec = flingAnimationSpec ?: exponentialDecay()
        coroutineScope {
            launch {
                val startX = translationXAnimatable.value
                translationXAnimatable.animateDecay(velocity.x, animationSpec) {
                    val translationX = this.value
                    val distanceX = translationX - startX
                    log { "fling. running. velocity=$velocity, startX=$startX, translationX=$translationX, distanceX=$distanceX" }
                }
            }
            launch {
                val startY = translationYAnimatable.value
                translationYAnimatable.animateDecay(velocity.y, animationSpec) {
                    val translationY = this.value
                    val distanceY = translationY - startY
                    log { "fling. running. velocity=$velocity, startY=$startY, translationY=$translationY, distanceY=$distanceY" }
                }
            }
        }
    }

    suspend fun stopAllAnimation(caller: String) {
        if (scaleAnimatable.isRunning) {
            scaleAnimatable.stop()
            log { "stopAllAnimation. stop scale animation. scale=${scale.format(4)}" }
            updateTranslationBounds("stopAllAnimation:$caller")
        }
        if (translationXAnimatable.isRunning || translationYAnimatable.isRunning) {
            translationXAnimatable.stop()
            translationYAnimatable.stop()
            log { "stopAllAnimation. stop translation animation. translation=${translation.toShortString()}" }
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
                "translation=${translation.toShortString()}" +
                ")"

    private fun updateTranslationBounds(caller: String) {
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val bounds = computeSupportTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = currentScale
        )
        this.translationBounds = bounds
        log {
            "updateTranslationBounds. " +
                    "$caller. " +
                    "bounds=${bounds.toShortString()}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "scale=$currentScale"
        }
        translationXAnimatable.updateBounds(lowerBound = bounds.left, upperBound = bounds.right)
        translationYAnimatable.updateBounds(lowerBound = bounds.top, upperBound = bounds.bottom)
    }

    private fun clearTranslationBounds(@Suppress("SameParameterValue") caller: String) {
        log { "updateTranslationBounds. ${caller}. clear" }
        this.translationBounds = null
        translationXAnimatable.updateBounds(lowerBound = null, upperBound = null)
        translationYAnimatable.updateBounds(lowerBound = null, upperBound = null)
    }

    private fun limitScale(
        scale: Float,
        minimumValue: Float = minScale,
        maximumValue: Float = maxScale
    ): Float {
        return scale.coerceIn(minimumValue = minimumValue, maximumValue = maximumValue)
    }

    private fun limitTranslation(offset: Offset, bounds: Rect? = translationBounds): Offset {
        val translationBounds = bounds ?: translationBounds ?: return offset
        return Offset(
            x = offset.x.coerceIn(
                minimumValue = translationBounds.left,
                maximumValue = translationBounds.right
            ),
            y = offset.y.coerceIn(
                minimumValue = translationBounds.top,
                maximumValue = translationBounds.bottom
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
                    "translationX" to it.translation.translationX,
                    "translationY" to it.translation.translationY,
                )
            },
            restore = {
                ZoomableState(
                    initialScale = it["scale"] as Float,
                    initialTranslateX = it["translationX"] as Float,
                    initialTranslateY = it["translationY"] as Float,
                )
            }
        )
    }
}