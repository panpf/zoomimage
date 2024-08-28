/*
 * Copyright (C) 2024 panpf <panpfpanpf@outlook.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.panpf.zoomimage.compose.zoom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyInputModifierNode
import androidx.compose.ui.input.key.type
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.compose.zoom.internal.MatcherZoomKeyHandler
import com.github.panpf.zoomimage.compose.zoom.internal.ZoomKeyHandler
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark

/**
 * Add keyboard zoom support. Note: You must let the current component gain focus to use it
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + (meta/ctrl)/alt, Key.DirectionUp + (meta/ctrl)/alt
 * * scale out: Key.ZoomOut, Key.Minus + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt
 * * move up: Key.DirectionUp
 * * move down: Key.DirectionDown
 * * move left: Key.DirectionLeft
 * * move right: Key.DirectionRight
 */
@Composable
fun Modifier.keyZoom(
    zoomable: ZoomableState,
    keyHandlers: ImmutableList<ZoomKeyHandler> = DefaultZoomKeyHandlers
): Modifier {
    val density = LocalDensity.current
    return this.then(KeyZoomElement(zoomable, keyHandlers, density))
}

/**
 * Observe the specified key flow and perform zoom operations
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + (meta/ctrl)/alt, Key.DirectionUp + (meta/ctrl)/alt
 * * scale out: Key.ZoomOut, Key.Minus + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt
 * * move up: Key.DirectionUp
 * * move down: Key.DirectionDown
 * * move left: Key.DirectionLeft
 * * move right: Key.DirectionRight
 */
@Composable
fun bindKeyZoomWithKeyEventFlow(
    eventFlow: SharedFlow<KeyEvent>,
    zoomable: ZoomableState,
    keyHandlers: ImmutableList<ZoomKeyHandler> = DefaultZoomKeyHandlers,
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(zoomable, keyHandlers) {
        eventFlow.collect { event ->
            keyHandlers.any {
                it.handle(coroutineScope, zoomable, event)
            }
        }
    }
}


/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultScaleInKeyMatchers
 */
val DefaultScaleInKeyMatchers = listOf(
    KeyMatcher(key = Key.ZoomIn),
    KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
    KeyMatcher(key = Key.Equals, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionUp, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultScaleOutKeyMatchers
 */
val DefaultScaleOutKeyMatchers = listOf(
    KeyMatcher(key = Key.ZoomOut),
    KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
    KeyMatcher(key = Key.Minus, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionDown, assistKey = AssistKey.Alt),
    KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey()),
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultMoveUpKeyMatchers
 */
val DefaultMoveUpKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionUp),
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultMoveDownKeyMatchers
 */
val DefaultMoveDownKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionDown)
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultMoveLeftKeyMatchers
 */
val DefaultMoveLeftKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionLeft)
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultMoveRightKeyMatchers
 */
val DefaultMoveRightKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionRight)
).toImmutableList()

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.KeyZoomTest.testDefaultZoomKeyHandlers
 */
val DefaultZoomKeyHandlers = listOf(
    ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatchers, scaleIn = true),
    ScaleKeyHandler(keyMatchers = DefaultScaleOutKeyMatchers, scaleIn = false),
    MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, moveArrow = MoveArrow.Up),
    MoveKeyHandler(keyMatchers = DefaultMoveDownKeyMatchers, moveArrow = MoveArrow.Down),
    MoveKeyHandler(keyMatchers = DefaultMoveLeftKeyMatchers, moveArrow = MoveArrow.Left),
    MoveKeyHandler(keyMatchers = DefaultMoveRightKeyMatchers, moveArrow = MoveArrow.Right),
).toImmutableList()


enum class MoveArrow {
    Up, Down, Left, Right
}

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.ScaleKeyHandlerTest
 */
@Stable
open class ScaleKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val scaleIn: Boolean,
    val shortPressStepScaleFactor: Float = 2f,
    val longPressStep: Float = 0.25f,
    override val longPressAccelerateBase: Float = 0.5f,
    override val longPressAccelerateInterval: Int = 500
) : StepMatcherZoomKeyHandler(keyMatchers) {

    override fun getShortPressStep(zoomableState: ZoomableState): Float {
        val currentScale = zoomableState.transform.scaleX
        val nextScale = if (scaleIn) {
            currentScale * shortPressStepScaleFactor
        } else {
            currentScale / shortPressStepScaleFactor
        }
        val step = nextScale - currentScale
        return step
    }

    override fun getLongPressStep(zoomableState: ZoomableState): Float {
        return if (scaleIn) longPressStep else -longPressStep
    }

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_SCALE == 0) {
        super.handle(coroutineScope, zoomableState, event)
    } else {
        false
    }

    override suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec?,
        add: Float
    ) {
        val newScale = zoomableState.transform.scaleX + add
        val centroidContentPoint = zoomableState.contentVisibleRect.center
        zoomableState.scale(
            targetScale = newScale,
            centroidContentPoint = centroidContentPoint,
            animated = animationSpec != null,
            animationSpec = animationSpec
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ScaleKeyHandler
        if (keyMatchers != other.keyMatchers) return false
        if (scaleIn != other.scaleIn) return false
        if (shortPressStepScaleFactor != other.shortPressStepScaleFactor) return false
        if (longPressStep != other.longPressStep) return false
        if (longPressAccelerateBase != other.longPressAccelerateBase) return false
        if (longPressAccelerateInterval != other.longPressAccelerateInterval) return false
        return true
    }

    override fun hashCode(): Int {
        var result = keyMatchers.hashCode()
        result = 31 * result + scaleIn.hashCode()
        result = 31 * result + shortPressStepScaleFactor.hashCode()
        result = 31 * result + longPressStep.hashCode()
        result = 31 * result + longPressAccelerateBase.hashCode()
        result = 31 * result + longPressAccelerateInterval
        return result
    }

    override fun toString(): String {
        return "ScaleKeyHandler(" +
                "keyMatchers=$keyMatchers, " +
                "scaleIn=$scaleIn, " +
                "shortPressStepScaleFactor=$shortPressStepScaleFactor, " +
                "longPressStep=$longPressStep, " +
                "longPressAccelerateBase=$longPressAccelerateBase, " +
                "longPressAccelerateInterval=$longPressAccelerateInterval)"
    }
}

/**
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.MoveKeyHandlerTest
 */
@Stable
open class MoveKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val moveArrow: MoveArrow,
    val shortPressStepWithContainerPercentage: Float = 0.33f,
    val longPressStepWithContainerPercentage: Float = 0.075f,
    override val longPressAccelerateBase: Float = 0.5f,
    override val longPressAccelerateInterval: Int = 500,
) : StepMatcherZoomKeyHandler(keyMatchers) {

    override fun getShortPressStep(zoomableState: ZoomableState): Float {
        val step = if (moveArrow == MoveArrow.Left || moveArrow == MoveArrow.Right) {
            zoomableState.containerSize.width * shortPressStepWithContainerPercentage
        } else {
            zoomableState.containerSize.height * shortPressStepWithContainerPercentage
        }
        val arrowStep = if (moveArrow == MoveArrow.Up || moveArrow == MoveArrow.Left)
            step else -step
        return arrowStep
    }

    override fun getLongPressStep(zoomableState: ZoomableState): Float {
        val step = if (moveArrow == MoveArrow.Left || moveArrow == MoveArrow.Right) {
            zoomableState.containerSize.width * longPressStepWithContainerPercentage
        } else {
            zoomableState.containerSize.height * longPressStepWithContainerPercentage
        }
        val arrowStep = if (moveArrow == MoveArrow.Up || moveArrow == MoveArrow.Left)
            step else -step
        return arrowStep
    }

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_DRAG == 0) {
        super.handle(coroutineScope, zoomableState, event)
    } else {
        false
    }

    override suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec?,
        add: Float
    ) {
        val addOffset = when (moveArrow) {
            MoveArrow.Up, MoveArrow.Down -> Offset(0f, add)
            MoveArrow.Left, MoveArrow.Right -> Offset(add, 0f)
        }
        val newOffset = zoomableState.transform.offset + addOffset
        zoomableState.offset(
            targetOffset = newOffset,
            animated = animationSpec != null,
            animationSpec = animationSpec
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as MoveKeyHandler
        if (keyMatchers != other.keyMatchers) return false
        if (moveArrow != other.moveArrow) return false
        if (shortPressStepWithContainerPercentage != other.shortPressStepWithContainerPercentage) return false
        if (longPressStepWithContainerPercentage != other.longPressStepWithContainerPercentage) return false
        if (longPressAccelerateBase != other.longPressAccelerateBase) return false
        if (longPressAccelerateInterval != other.longPressAccelerateInterval) return false
        return true
    }

    override fun hashCode(): Int {
        var result = keyMatchers.hashCode()
        result = 31 * result + moveArrow.hashCode()
        result = 31 * result + shortPressStepWithContainerPercentage.hashCode()
        result = 31 * result + longPressStepWithContainerPercentage.hashCode()
        result = 31 * result + longPressAccelerateBase.hashCode()
        result = 31 * result + longPressAccelerateInterval
        return result
    }

    override fun toString(): String {
        return "MoveKeyHandler(" +
                "keyMatchers=$keyMatchers, " +
                "moveArrow=$moveArrow, shortPressStepWithContainerPercentage=$shortPressStepWithContainerPercentage, " +
                "longPressStepWithContainerPercentage=$longPressStepWithContainerPercentage, " +
                "longPressAccelerateBase=$longPressAccelerateBase, " +
                "longPressAccelerateInterval=$longPressAccelerateInterval)"
    }
}

/**
 * Advance with fixed step length
 *
 * @see com.github.panpf.zoomimage.compose.common.test.zoom.StepMatcherZoomKeyHandler
 */
@Stable
abstract class StepMatcherZoomKeyHandler(
    keyMatchers: ImmutableList<KeyMatcher>,
) : BaseMatcherZoomKeyHandler(keyMatchers) {

    abstract val longPressAccelerateBase: Float

    abstract val longPressAccelerateInterval: Int

    abstract fun getShortPressStep(zoomableState: ZoomableState): Float

    abstract fun getLongPressStep(zoomableState: ZoomableState): Float

    override fun calculateShortPressAddValue(zoomableState: ZoomableState): Float {
        val step = getShortPressStep(zoomableState)
        return step
    }

    override fun calculateLongPressAddValue(
        zoomableState: ZoomableState,
        lastElapsedTime: Long?,
        elapsedTime: Long,
    ): Float {
        val step = getLongPressStep(zoomableState)
        val acceleratedStep = accelerateLongPress(step, elapsedTime)
        return acceleratedStep
    }

    private fun accelerateLongPress(value: Float, elapsedTime: Long): Float {
        val accelerateMultiple = (elapsedTime / longPressAccelerateInterval.toDouble()).toFloat()
        val accelerate = longPressAccelerateBase * accelerateMultiple
        val acceleratedValue = value + (accelerate * value)
        return acceleratedValue
    }
}

/**
 * Encapsulates the main logic of button zoom, supporting short press for single zoom and long press for continuous zoom.
 */
@Stable
abstract class BaseMatcherZoomKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
) : MatcherZoomKeyHandler(keyMatchers) {

    private var longPressJob: Job? = null
    private var lastShortPressTimeMark: ValueTimeMark? = null

    override fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ) {
        if (event.type == KeyEventType.KeyDown) {
            if (longPressJob?.isActive != true) {
                performShortPress(coroutineScope, zoomableState)
            }
            startLongPress(coroutineScope, zoomableState)
        } else {
            // KeyEventType.KeyUp
            cancelLongPress()
        }
    }

    abstract fun calculateShortPressAddValue(zoomableState: ZoomableState): Float

    private fun calculateShortPressAnimationDuration(zoomableState: ZoomableState): Int {
        val twoShortPressInterval = lastShortPressTimeMark?.elapsedNow()?.inWholeMilliseconds ?: -1
        lastShortPressTimeMark = TimeSource.Monotonic.markNow()
        val animationDuration = if (twoShortPressInterval > 0) {
            min(twoShortPressInterval.toInt(), zoomableState.animationSpec.durationMillis)
        } else {
            zoomableState.animationSpec.durationMillis
        }
        return animationDuration
    }

    private fun performShortPress(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
    ) {
        val addValue = calculateShortPressAddValue(zoomableState)
        val animationDuration = calculateShortPressAnimationDuration(zoomableState)
        zoomableState.logger.d {
            "BaseMatcherZoomKeyHandler. onKey. short press. addValue=$addValue, animationDuration=$animationDuration"
        }
        coroutineScope.launch {
            updateValue(
                zoomableState = zoomableState,
                animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = animationDuration),
                add = addValue
            )
        }
    }

    abstract fun calculateLongPressAddValue(
        zoomableState: ZoomableState,
        lastElapsedTime: Long?,
        elapsedTime: Long,
    ): Float

    private fun startLongPress(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
    ) {
        if (longPressJob?.isActive == true) {
            return
        }

        this.longPressJob = coroutineScope.launch {
            // Normally this is 500, but here for fast response, it is set to 200
            delay(200)

            val startTimeMark = TimeSource.Monotonic.markNow()
            var lastElapsedTime: Long? = null
            while (isActive) {
                val elapsedTime = startTimeMark.elapsedNow().inWholeMilliseconds
                val addValue = calculateLongPressAddValue(
                    zoomableState = zoomableState,
                    lastElapsedTime = lastElapsedTime,
                    elapsedTime = elapsedTime,
                )
                lastElapsedTime = elapsedTime
                zoomableState.logger.d {
                    "BaseMatcherZoomKeyHandler. onKey. long press running. " +
                            "addValue=$addValue, " +
                            "elapsedTime=$elapsedTime, " +
                            "lastElapsedTime=$lastElapsedTime"
                }
                updateValue(zoomableState = zoomableState, animationSpec = null, add = addValue)

                // Usually, on a device with a refresh rate of 60 frames, it can be refreshed once every 16 milliseconds,
                // but considering that most mobile devices already have a refresh rate of 120 frames, so 8
                delay(8)
            }
        }
    }

    private fun cancelLongPress() {
        this.longPressJob?.cancel()
        this.longPressJob = null
    }

    override fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        event: KeyEvent
    ) {
        cancelLongPress()
        zoomableState.logger.d {
            "BaseMatcherZoomKeyHandler. onCanceled"
        }
    }

    abstract suspend fun updateValue(
        zoomableState: ZoomableState,
        animationSpec: ZoomAnimationSpec? = null,
        add: Float
    )
}


internal data class KeyZoomElement(
    val zoomable: ZoomableState,
    val keyHandlers: ImmutableList<ZoomKeyHandler>,
    val density: Density
) : ModifierNodeElement<KeyZoomNode>() {

    override fun create(): KeyZoomNode {
        return KeyZoomNode(zoomable, keyHandlers, density)
    }

    override fun update(node: KeyZoomNode) {
        node.zoomable = zoomable
        node.keyHandlers = keyHandlers
        node.density = density
    }
}

internal data class KeyZoomNode(
    var zoomable: ZoomableState,
    var keyHandlers: ImmutableList<ZoomKeyHandler>,
    var density: Density
) : KeyInputModifierNode, Modifier.Node() {

    override fun onPreKeyEvent(event: KeyEvent): Boolean = false

    override fun onKeyEvent(event: KeyEvent): Boolean = keyHandlers.any {
        it.handle(coroutineScope, zoomable, event)
    }
}