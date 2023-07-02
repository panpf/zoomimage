package com.github.panpf.zoomimage

import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
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
import com.github.panpf.zoomimage.internal.computeScrollEdge
import com.github.panpf.zoomimage.internal.computeSupportTranslationBounds
import com.github.panpf.zoomimage.internal.computeTransform
import com.github.panpf.zoomimage.internal.containerCentroidToContentCentroid
import com.github.panpf.zoomimage.internal.contentCentroidToContainerCentroid
import com.github.panpf.zoomimage.internal.format
import com.github.panpf.zoomimage.internal.name
import com.github.panpf.zoomimage.internal.rotate
import com.github.panpf.zoomimage.internal.supportReadMode
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
    readModeEnabled: Boolean = false,
    readModeDecider: ReadModeDecider = ReadModeDecider.Default,
    debugMode: Boolean = false,
): ZoomableState {
    val state = rememberSaveable(saver = ZoomableState.Saver) {
        ZoomableState()
    }
    state.threeStepScaleEnabled = threeStepScaleEnabled
    state.readModeEnabled = readModeEnabled
    state.readModeDecider = readModeDecider
    state.debugMode = debugMode
    LaunchedEffect(
        state.containerSize,
        state.contentSize,
        state.contentOriginSize,
        state.contentScale,
        state.contentAlignment,
        readModeEnabled,
        readModeDecider,
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
    var debugMode: Boolean = false
    var readModeEnabled: Boolean = false
    var readModeDecider: ReadModeDecider = ReadModeDecider.Default

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
    val displayScale: ScaleFactor by derivedStateOf {
        baseScale.times(scale)
    }

    /**
     * The current translation value for [ZoomImage]
     */
    val translation: Translation by derivedStateOf {
        Translation(
            translationX = translationXAnimatable.value,
            translationY = translationYAnimatable.value
        )
    }

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

    var translationBounds: Rect? by mutableStateOf(null)
        private set
    val horizontalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = true)
    }
    val verticalScrollEdge: Edge by derivedStateOf {
        computeScrollEdge(contentSize, contentVisibleRect, horizontal = false)
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
            val readMode = readModeEnabled
                    && contentScale.supportReadMode()
                    && readModeDecider
                .should(srcSize = rotatedContentSize.toSize(), dstSize = containerSize.toSize())
            val baseTransform = computeTransform(
                srcSize = rotatedContentSize,
                dstSize = containerSize,
                scale = contentScale,
                alignment = contentAlignment,
            )
            supportInitialTransform = if (readMode) {
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
                    "minScale=${minScale.format(2)}, " +
                    "mediumScale=${mediumScale.format(2)}, " +
                    "maxScale=${maxScale.format(2)}, " +
                    "supportInitialTransform=${supportInitialTransform.toShortString()}"
        }
        scaleAnimatable.snapTo(supportInitialTransform.scaleX)
        translationXAnimatable.snapTo(supportInitialTransform.translationX)
        translationYAnimatable.snapTo(supportInitialTransform.translationY)
        updateTranslationBounds("reset")
    }

    /**
     * Animates scale of [ZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        newScaleContentCentroid: Centroid = Centroid(0.5f, 0.5f),
        animationDurationMillis: Int = AnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = AnimationConfig.DefaultEasing,
        initialVelocity: Float = AnimationConfig.DefaultInitialVelocity,
    ) {
        stopAllAnimation("animateScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation

        val animationSpec = tween<Float>(
            durationMillis = animationDurationMillis,
            easing = animationEasing
        )
        val futureTranslationBounds = computeSupportTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = newScale
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = newScaleContentCentroid
        )
        val targetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = newScale,
            containerCentroid = containerCentroid
        ).let {
            it.copy(
                translationX = it.translationX.coerceIn(
                    futureTranslationBounds.left,
                    futureTranslationBounds.right
                ),
                translationY = it.translationY.coerceIn(
                    futureTranslationBounds.top,
                    futureTranslationBounds.bottom
                ),
            )
        }
        log {
            "animateScaleTo. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "scale: ${currentScale.format(2)} -> ${newScale.format(2)}, " +
                    "contentCentroid=${newScaleContentCentroid.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "translation: ${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}, " +
                    "bounds=${futureTranslationBounds.toShortString()}"
        }
        clearTranslationBounds("animateScaleTo. before")
        coroutineScope {
            launch {
                scaleAnimatable.animateTo(
                    targetValue = newScale.coerceIn(minScale, maxScale),
                    animationSpec = animationSpec,
                    initialVelocity = initialVelocity,
                ) {
                    log { "animateScaleTo. running. scale=${this.value.format(2)}, translation=${translation.toShortString()}" }
                }
                updateTranslationBounds("animateScaleTo. end")
                log { "animateScaleTo. end. scale=${scale.format(2)}, translation=${translation.toShortString()}" }
            }
            launch {
                translationXAnimatable.animateTo(
                    targetValue = targetTranslation.translationX,
                    animationSpec = animationSpec,
                )
            }
            launch {
                translationYAnimatable.animateTo(
                    targetValue = targetTranslation.translationY,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    /**
     * Animates scale of [ZoomImage] to given [newScale]
     */
    suspend fun animateScaleTo(
        newScale: Float,
        touchPosition: Offset,
        animationDurationMillis: Int = AnimationConfig.DefaultDurationMillis,
        animationEasing: Easing = AnimationConfig.DefaultEasing,
        initialVelocity: Float = AnimationConfig.DefaultInitialVelocity,
    ) {
        stopAllAnimation("animateScaleTo")
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
            touchPosition = touchPosition
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        log {
            "animateScaleTo. " +
                    "newScale=${newScale.format(2)}, " +
                    "touchPosition=${touchPosition.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "contentCentroid=${contentCentroid.toShortString()}"
        }
        animateScaleTo(
            newScale = newScale,
            newScaleContentCentroid = contentCentroid,
            animationDurationMillis = animationDurationMillis,
            animationEasing = animationEasing,
            initialVelocity = initialVelocity,
        )
    }

    /**
     * Instantly sets scale of [ZoomImage] to given [newScale]
     */
    suspend fun snapScaleTo(
        newScale: Float,
        newScaleContentCentroid: Centroid = Centroid(0.5f, 0.5f)
    ) {
        stopAllAnimation("snapScaleTo")
        val containerSize = containerSize.takeIf { it.isSpecified } ?: return
        val contentSize = contentSize.takeIf { it.isSpecified } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentScale = scale
        val currentTranslation = translation

        val futureTranslationBounds = computeSupportTranslationBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            supportScale = newScale
        )
        val containerCentroid = contentCentroidToContainerCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentCentroid = newScaleContentCentroid
        )
        val targetTranslation = computeScaleTargetTranslation(
            containerSize = containerSize,
            scale = newScale,
            containerCentroid = containerCentroid
        ).let {
            it.copy(
                translationX = it.translationX.coerceIn(
                    futureTranslationBounds.left,
                    futureTranslationBounds.right
                ),
                translationY = it.translationY.coerceIn(
                    futureTranslationBounds.top,
                    futureTranslationBounds.bottom
                ),
            )
        }
        log {
            "snapScaleTo. " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "scale: ${currentScale.format(2)} -> ${newScale.format(2)}, " +
                    "contentCentroid=${newScaleContentCentroid.toShortString()}, " +
                    "containerCentroid=${containerCentroid.toShortString()}, " +
                    "translation: ${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}, " +
                    "bounds=${futureTranslationBounds.toShortString()}"
        }
        coroutineScope {
            scaleAnimatable.snapTo(
                newScale.coerceIn(
                    minimumValue = minScale,
                    maximumValue = maxScale
                )
            )
            updateTranslationBounds("snapScaleTo")
            translationXAnimatable.snapTo(targetValue = targetTranslation.translationX)
            translationYAnimatable.snapTo(targetValue = targetTranslation.translationY)
        }
    }

    /**
     * Instantly sets scale of [ZoomImage] to given [newScale]
     */
    suspend fun snapScaleTo(newScale: Float, touchPosition: Offset) {
        stopAllAnimation("snapScaleTo")
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
            touchPosition = touchPosition
        )
        val contentCentroid = containerCentroidToContentCentroid(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            containerCentroid = containerCentroid
        )
        log {
            "snapScaleTo. " +
                    "newScale=${newScale.format(2)}, " +
                    "touchPosition=${touchPosition.toShortString()}, " +
                    "contentCentroid=${contentCentroid.toShortString()}"
        }
        snapScaleTo(
            newScale = newScale,
            newScaleContentCentroid = contentCentroid
        )
    }

    suspend fun snapTranslationBy(add: Offset) {
        stopAllAnimation("snapTranslationBy")
        val currentTranslation = translation
        val targetTranslation = Offset(
            x = currentTranslation.translationX + add.x,
            y = currentTranslation.translationY + add.y
        )
        log {
            "snapTranslationBy. " +
                    "add=${add.toShortString()}, " +
                    "translation=${currentTranslation.toShortString()} -> ${targetTranslation.toShortString()}"
        }
        coroutineScope {
            launch {
                translationXAnimatable.snapTo(targetTranslation.x)
                translationYAnimatable.snapTo(targetTranslation.y)
            }
        }
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

    suspend fun fling(velocity: Velocity, animationSpec: DecayAnimationSpec<Float>) {
        stopAllAnimation("fling")
        log { "fling. velocity=$velocity, translation=${translation.toShortString()}" }
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
            log { "stopAllAnimation. stop scale animation. scale=${scale.format(2)}" }
            updateTranslationBounds(caller)
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
                "minScale=${minScale.format(2)}, " +
                "mediumScale=${mediumScale.format(2)}, " +
                "maxScale=${maxScale.format(2)}, " +
                "scale=${scale.format(2)}, " +
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