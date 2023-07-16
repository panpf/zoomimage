package com.github.panpf.zoomimage

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
import com.github.panpf.zoomimage.compose.internal.computeLocationUserOffset
import com.github.panpf.zoomimage.compose.internal.computeReadModeTransform
import com.github.panpf.zoomimage.compose.internal.computeScaleOffsetByCentroid
import com.github.panpf.zoomimage.compose.internal.computeTransform
import com.github.panpf.zoomimage.compose.internal.computeUserOffsetBounds
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
import com.github.panpf.zoomimage.core.internal.computeUserScales
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

    val logger: Logger = Logger(tag = "ZoomImage", module = "ZoomableState")

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
        set(value) {
            if (field == value) return
            field = value
            logger.level = if (value) Logger.DEBUG else Logger.INFO
        }
    var defaultMediumScaleMultiple: Float = DEFAULT_MEDIUM_SCALE_MULTIPLE

    var minUserScale: Float by mutableStateOf(1f)
        private set
    var mediumUserScale: Float by mutableStateOf(1f)
        private set
    var maxUserScale: Float by mutableStateOf(1f)
        private set

    // todo transform 和 displayTransform 表达的意思要换一下
    var userTransform: Transform by mutableStateOf(   // todo support rotation
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
        baseTransform.concat(userTransform)
    }

    val userOffsetBounds: IntRect by derivedStateOf {
        computeUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userTransform.scaleX,
        ).roundToIntRect()
    }

    val contentInContainerRect: IntRect by derivedStateOf {
        computeContentInContainerRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }
    val contentInContainerVisibleRect: IntRect by derivedStateOf {
        computeContentInContainerVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
        ).roundToIntRect()
    }

    val containerVisibleRect: IntRect by derivedStateOf {
        computeContainerVisibleRect(
            containerSize = containerSize,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset
        ).roundToIntRect()
    }
    val contentVisibleRect: IntRect by derivedStateOf {
        computeContentVisibleRect(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userTransform.scaleX,
            userOffset = userTransform.offset,
        ).roundToIntRect()
    }
    val scrollEdge: ScrollEdge by derivedStateOf {
        computeScrollEdge(
            contentInContainerVisibleRect = contentInContainerVisibleRect.toCompatIntRect(),
            contentVisibleRect = contentVisibleRect.toCompatIntRect(),
        )
    }

    init {
        logger.level = if (debugMode) Logger.DEBUG else Logger.INFO
    }

    internal suspend fun reset() {
        stopAnimation("reset")

        val contentSize = contentSize
        val contentOriginSize = contentOriginSize
        val containerSize = containerSize
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val initialUserTransform: Transform
        if (containerSize.isEmpty() || contentSize.isEmpty()) {
            minUserScale = 1.0f
            mediumUserScale = 1.0f
            maxUserScale = 1.0f
            baseTransform = Transform.Origin
            initialUserTransform = Transform.Origin
        } else {
            val rotatedContentSize = contentSize.rotate(userTransform.rotation.roundToInt())
            val rotatedContentOriginSize =
                contentOriginSize.rotate(userTransform.rotation.roundToInt())
            val userScales = computeUserScales(
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
            minUserScale = userScales[0]
            // todo 清明上河图图片示例，垂直方向上，没有充满屏幕，貌似是基础 Image 的缩放比例跟预想的不一样，导致计算出来的 mediumScale 应用后图片显示没有充满屏幕
            mediumUserScale = userScales[1]
            maxUserScale = userScales[2]
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
            initialUserTransform = if (readModeResult) {
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
        val limitedInitialUserTransform = limitUserTransform(initialUserTransform)
        logger.d {
            "reset. containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "contentOriginSize=${contentOriginSize.toShortString()}, " +
                    "contentScale=${contentScale.name}, " +
                    "contentAlignment=${contentAlignment.name}, " +
                    "readMode=${readMode}, " +
                    "minScale=${minUserScale.format(4)}, " +
                    "mediumScale=${mediumUserScale.format(4)}, " +
                    "maxScale=${maxUserScale.format(4)}, " +
                    "baseTransform=${baseTransform.toShortString()}, " +
                    "initialUserTransform=${initialUserTransform.toShortString()}, " +
                    "limitedInitialUserTransform=${limitedInitialUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedInitialUserTransform,
            animated = false,
            caller = "reset"
        )
    }


    suspend fun scale(
        targetUserScale: Float,
        centroid: Offset = Offset(x = containerSize.width / 2f, y = containerSize.height / 2f),
        animated: Boolean = false,
        rubberBandScale: Boolean = false,
    ) {
        stopAnimation("scale")

        val limitedTargetUserScale = if (rubberBandScale && this@ZoomableState.rubberBandScale) {
            limitUserScaleWithRubberBand(targetUserScale)
        } else {
            limitUserScale(targetUserScale)
        }
        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val currentUserOffset = currentUserTransform.offset
        val targetUserOffset = computeScaleOffsetByCentroid(
            currentScale = currentUserScale,
            currentOffset = currentUserOffset,
            targetScale = limitedTargetUserScale,
            centroid = centroid,
            gestureRotate = 0f,
        )
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddOffset = limitedTargetUserOffset - currentUserOffset
            "scale. " +
                    "targetScale=${targetUserScale.format(4)}, " +
                    "centroid=${centroid.toShortString()}, " +
                    "animated=${animated}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${limitedAddUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "scale"
        )
    }

    suspend fun offset(targetUserOffset: Offset, animated: Boolean = false) {
        stopAnimation("offset")

        val currentUserTransform = userTransform
        val currentUserScale = currentUserTransform.scaleX
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, currentUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(offset = limitedTargetUserOffset)
        logger.d {
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            "offset. " +
                    "targetOffset=${targetUserOffset.toShortString()}, " +
                    "animated=${animated}, " +
                    "currentUserScale=${currentUserScale.format(4)}, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "offset"
        )
    }

    suspend fun location(
        contentOrigin: Origin,
        targetUserScale: Float = userTransform.scaleX,
        animated: Boolean = false,
    ) {
        stopAnimation("location")

        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return
        val contentScale = contentScale
        val contentAlignment = contentAlignment
        val currentUserTransform = userTransform
        val containerOrigin = contentOriginToContainerOrigin(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            contentAlignment = contentAlignment,
            contentOrigin = contentOrigin
        )
        val limitedTargetUserScale = limitUserScale(targetUserScale)
        // todo targetUserOffset 位置貌似不太准，目标是让其位于屏幕正中间，必要时可在屏幕时绘制触摸点来检验效果
        val targetUserOffset = computeLocationUserOffset(
            containerSize = containerSize,
            containerOrigin = containerOrigin,
            userScale = limitedTargetUserScale,
        )
        val limitedTargetUserOffset = limitUserOffset(targetUserOffset, limitedTargetUserScale)
        val limitedTargetUserTransform = currentUserTransform.copy(
            scale = ScaleFactor(limitedTargetUserScale),
            offset = limitedTargetUserOffset
        )
        logger.d {
            val currentUserScale = currentUserTransform.scaleX
            val currentUserOffset = currentUserTransform.offset
            val targetAddUserScale = targetUserScale - currentUserScale
            val limitedTargetAddUserScale = limitedTargetUserScale - currentUserScale
            val targetAddUserOffset = targetUserOffset - currentUserOffset
            val limitedTargetAddUserOffset = limitedTargetUserOffset - currentUserOffset
            "location. " +
                    "contentOrigin=${contentOrigin.toShortString()}, " +
                    "targetScale=${targetUserScale.format(4)}, " +
                    "animated=${animated}, " +
                    "containerSize=${containerSize.toShortString()}, " +
                    "contentSize=${contentSize.toShortString()}, " +
                    "containerOrigin=${containerOrigin.toShortString()}, " +
                    "addUserScale=${targetAddUserScale.format(4)} -> ${
                        limitedTargetAddUserScale.format(
                            4
                        )
                    }, " +
                    "addUserOffset=${targetAddUserOffset.toShortString()} -> ${limitedTargetAddUserOffset.toShortString()}, " +
                    "userTransform=${currentUserTransform.toShortString()} -> ${limitedTargetUserTransform.toShortString()}"
        }

        updateUserTransform(
            targetUserTransform = limitedTargetUserTransform,
            animated = animated,
            caller = "location"
        )
    }

    suspend fun fling(velocity: Velocity, density: Density) {
        stopAnimation("fling")

        val currentUserTransform = userTransform
        val startUserOffset = currentUserTransform.offset
        val flingAnimatable = Animatable(
            initialValue = startUserOffset,
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
                    val currentUserTransform2 = userTransform
                    val targetUserOffset = this.value
                    val limitedTargetUserOffset =
                        limitUserOffset(targetUserOffset, currentUserTransform2.scaleX)
                    val distance = limitedTargetUserOffset - startUserOffset
                    logger.d {
                        "fling. running. " +
                                "velocity=$velocity, " +
                                "startUserOffset=${startUserOffset.toShortString()}, " +
                                "currentUserOffset=${limitedTargetUserOffset.toShortString()}, " +
                                "distance=$distance"
                    }
                    userTransform = currentUserTransform2.copy(offset = limitedTargetUserOffset)
                }
            }
        }
    }

    suspend fun switchUserScale(
        contentOrigin: Origin = Origin(0.5f, 0.5f),
        animated: Boolean = true
    ): Float {
        val nextUserScale = getNextStepUserScale()
        location(
            contentOrigin = contentOrigin,
            targetUserScale = nextUserScale,
            animated = animated
        )
        return nextUserScale
    }

    suspend fun reboundUserScale(centroid: Offset) {
        val minUserScale = minUserScale
        val maxUserScale = maxUserScale
        val currentUserScale = userTransform.scaleX
        val targetUserScale = when {
            currentUserScale.format(2) > maxUserScale.format(2) -> maxUserScale
            currentUserScale.format(2) < minUserScale.format(2) -> minUserScale
            else -> null
        }
        if (targetUserScale != null) {
            scale(
                targetUserScale = targetUserScale,
                centroid = centroid,
                animated = true,
                rubberBandScale = false
            )
        }
    }

    fun getNextStepUserScale(): Float {
        val stepScales = if (threeStepScale) {
            floatArrayOf(minUserScale, mediumUserScale, maxUserScale)
        } else {
            floatArrayOf(minUserScale, mediumUserScale)
        }
        return calculateNextStepScale(stepScales, userTransform.scaleX)
    }

    suspend fun stopAnimation(caller: String) {
        val lastAnimatable = lastAnimatable
        if (lastAnimatable?.isRunning == true) {
            lastAnimatable.stop()
            logger.d { "stopAnimation:$caller" }
        }
    }

    fun canDrag(horizontal: Boolean, direction: Int): Boolean =
        canScroll(horizontal, direction * -1, scrollEdge)

    fun touchOffsetToContentOrigin(touch: Offset): Origin {
        val containerSize = containerSize.takeIf { it.isNotEmpty() } ?: return Origin.TopStart
        val contentSize = contentSize.takeIf { it.isNotEmpty() } ?: return Origin.TopStart
        val currentUserTransform = userTransform
        val containerOrigin = computeContainerOriginByTouchPosition(
            containerSize = containerSize,
            userScale = currentUserTransform.scaleX,
            userOffset = currentUserTransform.offset,
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

    private fun limitUserScale(targetUserScale: Float): Float {
        return targetUserScale.coerceIn(minimumValue = minUserScale, maximumValue = maxUserScale)
    }

    private fun limitUserScaleWithRubberBand(targetUserScale: Float): Float {
        return limitScaleWithRubberBand(
            currentScale = userTransform.scaleX,
            targetScale = targetUserScale,
            minScale = minUserScale,
            maxScale = maxUserScale
        )
    }

    private fun limitUserOffset(userOffset: Offset, userScale: Float): Offset {
        val userOffsetBounds = computeUserOffsetBounds(
            containerSize = containerSize,
            contentSize = contentSize,
            contentScale = contentScale,
            alignment = contentAlignment,
            userScale = userScale
        )
        if (userOffset.x >= userOffsetBounds.left
            && userOffset.x <= userOffsetBounds.right
            && userOffset.y >= userOffsetBounds.top
            && userOffset.y <= userOffsetBounds.bottom
        ) {
            return userOffset
        }
        return Offset(
            x = userOffset.x.coerceIn(userOffsetBounds.left, userOffsetBounds.right),
            y = userOffset.y.coerceIn(userOffsetBounds.top, userOffsetBounds.bottom),
        )
    }

    private fun limitUserTransform(userTransform: Transform): Transform {
        val limitedUserScale = limitUserScale(userTransform.scaleX)
        val limitedUserOffset = limitUserOffset(userTransform.offset, limitedUserScale)
        return if (limitedUserScale != userTransform.scaleX || limitedUserOffset != userTransform.offset) {
            userTransform.copy(
                scale = ScaleFactor(limitedUserScale),
                offset = limitedUserOffset,
            )
        } else {
            userTransform
        }
    }

    private suspend fun updateUserTransform(
        targetUserTransform: Transform,
        animated: Boolean,
        caller: String
    ) {
        stopAnimation(caller)

        if (animated) {
            val currentTransform = userTransform
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
                        val userTransform = com.github.panpf.zoomimage.compose.lerp(
                            start = currentTransform,
                            stop = targetUserTransform,
                            fraction = value
                        )
                        logger.d {
                            "$caller. animated running. transform=${userTransform.toShortString()}"
                        }
                        this@ZoomableState.userTransform = userTransform
                    }
                }
            }
        } else {
            this.userTransform = targetUserTransform
        }
    }

    override fun toString(): String =
        "ZoomableState(" +
                "containerSize=${containerSize.toShortString()}, " +
                "contentSize=${contentSize.toShortString()}, " +
                "contentOriginSize=${contentOriginSize.toShortString()}, " +
                "contentScale=${contentScale.name}, " +
                "contentAlignment=${contentAlignment.name}, " +
                "minScale=${minUserScale.format(4)}, " +
                "mediumScale=${mediumUserScale.format(4)}, " +
                "maxScale=${maxUserScale.format(4)}, " +
                "userTransform=${userTransform.toShortString()}" +
                ")"

    companion object {

        /**
         * The default [Saver] implementation for [ZoomableState].
         */
        val Saver: Saver<ZoomableState, *> = mapSaver(
            save = {
                mapOf(
                    "userScale" to it.userTransform.scaleX,
                    "userOffsetX" to it.userTransform.offset.x,
                    "userOffsetY" to it.userTransform.offset.y,
                    "rotation" to it.userTransform.rotation,
                )
            },
            restore = {
                ZoomableState(
                    initialScale = it["userScale"] as Float,
                    initialTranslateX = it["userOffsetX"] as Float,
                    initialTranslateY = it["userOffsetY"] as Float,
                    initialRotation = it["rotation"] as Float,
                )
            }
        )
    }
}