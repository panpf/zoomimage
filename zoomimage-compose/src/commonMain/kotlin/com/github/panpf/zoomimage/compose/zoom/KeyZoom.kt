/*
 * Copyright (C) 2023 panpf <panpfpanpf@outlook.com>
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.compose.util.BaseKeyHandler
import com.github.panpf.zoomimage.compose.util.KeyHandler
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.zoom.GestureType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource


/**
 * Add keyboard zoom support. Note: You must let the current component gain focus to use it
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + meta/ctrl
 * * scale out: Key.ZoomOut, Key.Minus + meta/ctrl
 * * move up: Key.DirectionUp + meta/ctrl
 * * move down: Key.DirectionDown + meta/ctrl
 * * move left: Key.DirectionLeft + meta/ctrl
 * * move right: Key.DirectionRight + meta/ctrl
 */
@Composable
fun Modifier.keyZoom(
    zoomable: ZoomableState,
    keyHandlers: ImmutableList<KeyHandler> = DefaultKeyZoomHandlers
): Modifier {
    val density = LocalDensity.current
    return this.then(KeyZoomElement(zoomable, keyHandlers, density))
}

/**
 * To handle key zoom, you just get the key event and pass it to this function
 *
 * The registered keys are as follows:
 * * scale in: Key.ZoomIn, Key.Equals + meta/ctrl
 * * scale out: Key.ZoomOut, Key.Minus + meta/ctrl
 * * move up: Key.DirectionUp + meta/ctrl
 * * move down: Key.DirectionDown + meta/ctrl
 * * move left: Key.DirectionLeft + meta/ctrl
 * * move right: Key.DirectionRight + meta/ctrl
 */
@Composable
fun keyZoom(
    zoomable: ZoomableState,
    eventFlow: SharedFlow<KeyEvent>,
    keyHandlers: ImmutableList<KeyHandler> = DefaultKeyZoomHandlers,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    LaunchedEffect(zoomable, keyHandlers) {
        eventFlow.collect { event ->
            keyHandlers.any {
                it.handle(coroutineScope, zoomable, density, event)
            }
        }
    }
}


val DefaultScaleInKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomIn),
    KeyMatcher(key = Key.Equals, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultScaleOutKeyMatcher = listOf(
    KeyMatcher(key = Key.ZoomOut),
    KeyMatcher(key = Key.Minus, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultMoveUpKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionUp, assistKey = platformAssistKey()),
).toImmutableList()

val DefaultMoveDownKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionDown, assistKey = platformAssistKey())
).toImmutableList()

val DefaultMoveLeftKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionLeft, assistKey = platformAssistKey())
).toImmutableList()

val DefaultMoveRightKeyMatchers = listOf(
    KeyMatcher(key = Key.DirectionRight, assistKey = platformAssistKey())
).toImmutableList()

val DefaultKeyZoomHandlers = listOf(
    ScaleKeyHandler(keyMatchers = DefaultScaleInKeyMatcher, scaleIn = true),
    ScaleKeyHandler(keyMatchers = DefaultScaleOutKeyMatcher, scaleIn = false),
    MoveKeyHandler(keyMatchers = DefaultMoveUpKeyMatchers, arrow = MoveKeyHandler.Arrow.Up),
    MoveKeyHandler(keyMatchers = DefaultMoveDownKeyMatchers, arrow = MoveKeyHandler.Arrow.Down),
    MoveKeyHandler(keyMatchers = DefaultMoveLeftKeyMatchers, arrow = MoveKeyHandler.Arrow.Left),
    MoveKeyHandler(keyMatchers = DefaultMoveRightKeyMatchers, arrow = MoveKeyHandler.Arrow.Right),
).toImmutableList()


@Stable
data class ScaleKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val scaleIn: Boolean,

    // Normally this is 500, but here for fast response scaling, it is set to 200
    override val longPressThreshold: Long = 200L,

    val shortPressInitAddValue: Float = 0.5f,
    val shortPressMaxAddValue: Float = 2f,
    override var fastShortPressMaxCount: Int = 6,

    // Change from continuousScaleInitScaleFactor to continuousScaleMaxScaleFactor is expected within 3000 milliseconds when scaling continuously
    override val continuousScaleDuration: Long = 6000L,
    val continuousScaleInitAddValue: Float = 0.1f,
    val continuousScaleMaxAddValue: Float = 2f,
    override val continuousScaleInterval: Long = 16,
) : BaseOperateKeyHandler(keyMatchers) {

    override fun getShortPressInitAddValue(density: Density): Float = shortPressInitAddValue

    override fun getShortPressMaxAddValue(density: Density): Float = shortPressMaxAddValue

    override fun getContinuousScaleInitAddValue(density: Density): Float =
        continuousScaleInitAddValue

    override fun getContinuousScaleMaxAddValue(density: Density): Float = continuousScaleMaxAddValue

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_SCALE == 0) {
        super.handle(coroutineScope, zoomableState, density, event)
    } else {
        false
    }

    override suspend fun updateValue(zoomableState: ZoomableState, animated: Boolean, add: Float) {
        zoomableState.scale(
            targetScale = zoomableState.transform.scaleX * addScale(add),
            centroidContentPoint = zoomableState.contentVisibleRect.center,
            animated = animated
        )
    }

    private fun addScale(scaleStep: Float): Float {
        return if (scaleIn) (1 + scaleStep) else (1 - scaleStep)
    }
}

@Stable
data class MoveKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
    val arrow: Arrow,

    // Normally this is 500, but here for fast response offset, it is set to 200
    override val longPressThreshold: Long = 200L,

    val shortPressInitAddValueDp: Dp = 200.dp,
    val shortPressMaxAddValueDp: Dp = 500.dp,
    override var fastShortPressMaxCount: Int = 6,

    // Change from continuousScaleInitScaleFactor to continuousScaleMaxScaleFactor is expected within 3000 milliseconds when scaling continuously
    override val continuousScaleDuration: Long = 6000L,
    val continuousScaleInitAddValueDp: Dp = 100.dp,
    val continuousScaleMaxAddValueDp: Dp = 500.dp,
    override val continuousScaleInterval: Long = 16,
) : BaseOperateKeyHandler(keyMatchers) {

    override fun getShortPressInitAddValue(density: Density): Float =
        with(density) { shortPressInitAddValueDp.toPx() }

    override fun getShortPressMaxAddValue(density: Density): Float =
        with(density) { shortPressMaxAddValueDp.toPx() }

    override fun getContinuousScaleInitAddValue(density: Density): Float =
        with(density) { continuousScaleInitAddValueDp.toPx() }

    override fun getContinuousScaleMaxAddValue(density: Density): Float =
        with(density) { continuousScaleMaxAddValueDp.toPx() }

    override fun handle(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ): Boolean = if (zoomableState.disabledGestureTypes and GestureType.KEYBOARD_DRAG == 0) {
        super.handle(coroutineScope, zoomableState, density, event)
    } else {
        false
    }

    override suspend fun updateValue(zoomableState: ZoomableState, animated: Boolean, add: Float) {
        zoomableState.offset(
            targetOffset = zoomableState.transform.offset + addOffset(add),
            animated = animated
        )
    }

    private fun addOffset(add: Float): Offset = when (arrow) {
        Arrow.Up -> Offset(0f, add)
        Arrow.Down -> Offset(0f, -add)
        Arrow.Left -> Offset(add, 0f)
        Arrow.Right -> Offset(-add, 0f)
    }

    enum class Arrow {
        Up, Down, Left, Right
    }
}

@Stable
abstract class BaseOperateKeyHandler(
    override val keyMatchers: ImmutableList<KeyMatcher>,
) : BaseKeyHandler(keyMatchers) {

    abstract val longPressThreshold: Long

    abstract fun getShortPressInitAddValue(density: Density): Float
    abstract fun getShortPressMaxAddValue(density: Density): Float
    abstract var fastShortPressMaxCount: Int

    abstract fun getContinuousScaleInitAddValue(density: Density): Float
    abstract fun getContinuousScaleMaxAddValue(density: Density): Float
    abstract val continuousScaleDuration: Long
    abstract val continuousScaleInterval: Long

    private var fastShortPressCount: Int = -1
    private var continuousScaleJob: Job? = null
    private var startTimeMark: TimeSource.Monotonic.ValueTimeMark? = null

    override fun onKey(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ) {
        // TODO Both long press and short press are based on the minimum to maximum value.
        //  If you expect to reach the end point 5 times during a short press, then the step is the distance divided by 5
        //  If you expect to reach the end in 5 seconds when long pressing, then divide the time by 5 seconds as the progress, multiply by the distance, and walk step by step from the beginning to the end.
        val startTimeMark = startTimeMark
        val continuousScaleJob = continuousScaleJob
        if (event.type == KeyEventType.KeyDown) {
            val finalStartTimeMark = startTimeMark ?: TimeSource.Monotonic.markNow()
                .apply { this@BaseOperateKeyHandler.startTimeMark = this }
            if (continuousScaleJob == null) {
                this.continuousScaleJob = coroutineScope.launch {
                    delay(longPressThreshold)
                    while (isActive) {
                        val elapsedTime = finalStartTimeMark.elapsedNow().inWholeMilliseconds
                        val progress =
                            (elapsedTime / continuousScaleDuration.toFloat()).coerceAtMost(1f)
                        val progressValue =
                            progress * (getContinuousScaleMaxAddValue(density) - getContinuousScaleInitAddValue(
                                density
                            ))
                        val addScale = getContinuousScaleInitAddValue(density) + progressValue
                        updateValue(zoomableState, animated = false, addScale)
                        delay(continuousScaleInterval)
                    }
                }
            }
        } else {
            continuousScaleJob?.cancel()
            this.continuousScaleJob = null
            this.startTimeMark = null

            // short press up
            val elapsedTime = startTimeMark?.elapsedNow()?.inWholeMilliseconds ?: -1
            if (startTimeMark != null && elapsedTime < longPressThreshold) {
                // If the interval between pressing and lifting is less than 100ms, it is regarded as a quick short press.
                if (elapsedTime < 100) fastShortPressCount++ else fastShortPressCount = -1
                val progress =
                    (fastShortPressCount / fastShortPressMaxCount.toFloat()).coerceAtMost(1f)
                val progressValue =
                    progress * (getShortPressMaxAddValue(density) - getShortPressInitAddValue(
                        density
                    ))
                val addScale = getShortPressInitAddValue(density) + progressValue
                coroutineScope.launch {
                    updateValue(zoomableState, animated = true, addScale)
                }
            } else {
                fastShortPressCount = -1
            }
        }
    }

    override fun onCanceled(
        coroutineScope: CoroutineScope,
        zoomableState: ZoomableState,
        density: Density,
        event: KeyEvent
    ) {
        this.continuousScaleJob?.cancel()
        this.continuousScaleJob = null
        this.startTimeMark = null
        this.fastShortPressCount = -1
    }

    abstract suspend fun updateValue(zoomableState: ZoomableState, animated: Boolean, add: Float)
}


internal data class KeyZoomElement(
    val zoomable: ZoomableState,
    val keyHandlers: ImmutableList<KeyHandler>,
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
    var keyHandlers: ImmutableList<KeyHandler>,
    var density: Density
) : KeyInputModifierNode, Modifier.Node() {

    override fun onPreKeyEvent(event: KeyEvent): Boolean = false

    override fun onKeyEvent(event: KeyEvent): Boolean = keyHandlers.any {
        it.handle(coroutineScope, zoomable, density, event)
    }
}