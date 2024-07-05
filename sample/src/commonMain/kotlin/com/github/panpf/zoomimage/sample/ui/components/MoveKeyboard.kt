package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_gamepad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.time.TimeSource

@Composable
fun rememberMoveKeyboardState(stepInterval: Long = 8): MoveKeyboardState {
    val coroutineScope = rememberCoroutineScope()
    return remember(stepInterval) { MoveKeyboardState(coroutineScope, stepInterval) }
}

class MoveKeyboardState(
    private val coroutineScope: CoroutineScope,
    private val stepInterval: Long
) : RememberObserver {

    private var lastJob: Job? = null
    private val _moveFlow = MutableSharedFlow<Offset>()
    private var lastRollbackAnimatable: Animatable<*, *>? = null

    var maxStep by mutableStateOf(Offset(100f, 100f))
        internal set
    var containerSize by mutableStateOf(IntSize.Zero)
        internal set
    var contentSize by mutableStateOf(IntSize.Zero)
        internal set
    var gamePadOffset by mutableStateOf(Offset.Zero)
        private set

    val moveFlow: SharedFlow<Offset> = _moveFlow

    fun move(dragAmount: Offset) {
        val limitedOffset = limitedOffsetByCenter(
            offset = gamePadOffset + dragAmount,
            containerSize = containerSize,
            contentSize = contentSize
        )
        if (this.gamePadOffset != limitedOffset) {
            this.gamePadOffset = limitedOffset
            startMove()
        }
    }

    fun rollback() {
        val rollbackAnimatable = Animatable(0f)
        lastRollbackAnimatable = rollbackAnimatable
        val startOffset = gamePadOffset
        val endOffset = Offset.Zero
        coroutineScope.launch {
            rollbackAnimatable.animateTo(1f) {
                gamePadOffset = androidx.compose.ui.geometry.lerp(startOffset, endOffset, value)
            }
        }

        lastJob?.cancel("rollback")
    }

    fun stopAllAnimation() {
        coroutineScope.launch {
            lastRollbackAnimatable?.stop()
        }
    }

    private fun startMove() {
        lastJob?.cancel("startMove")
        val gamePadOffset = gamePadOffset.takeIf { it.x != 0f && it.y != 0f } ?: return
        val xSpace = (containerSize.width - contentSize.width) / 2f
        val ySpace = (containerSize.height - contentSize.height) / 2f
        val move = Offset(
            x = (gamePadOffset.x / xSpace) * maxStep.x,
            y = (gamePadOffset.y / ySpace) * maxStep.y
        )
        lastJob = coroutineScope.launch {
            val startTime = TimeSource.Monotonic.markNow()
            while (isActive) {
                val time = startTime.elapsedNow().inWholeMilliseconds
                val scale = when {
                    time < 2000 -> 1f
                    time < 4000 -> 2f
                    time < 8000 -> 4f
                    else -> 8f
                }
                _moveFlow.emit(move * scale)
                delay(stepInterval)
            }
        }
    }

    private fun limitedOffsetByCenter(
        offset: Offset,
        containerSize: IntSize,
        contentSize: IntSize
    ): Offset {
        val xSpace = (containerSize.width - contentSize.width) / 2f
        val ySpace = (containerSize.height - contentSize.height) / 2f
        val x = offset.x.coerceIn(-xSpace, xSpace)
        val y = offset.y.coerceIn(-ySpace, ySpace)
        return Offset(x, y)
    }

    override fun onAbandoned() {
        coroutineScope.cancel("onAbandoned")
    }

    override fun onForgotten() {
        coroutineScope.cancel("onForgotten")
    }

    override fun onRemembered() {

    }
}

@Composable
fun MoveKeyboard(
    state: MoveKeyboardState,
    modifier: Modifier = Modifier.fillMaxWidth(),
    iconTint: Color = LocalContentColor.current
) {
    val brush = remember {
        Brush.radialGradient(
            listOf(
                Color.Transparent,
                Color.Transparent,
                Color.Transparent.copy(0.15f),
                Color.White.copy(0.3f)
            )
        )
    }
    Box(
        modifier
            .aspectRatio(1f)
            .background(brush, CircleShape)
            .onSizeChanged { state.containerSize = it }
    ) {
        Box(
            Modifier
                .fillMaxSize(0.5f)
                .onSizeChanged { state.contentSize = it }
                .align(Alignment.Center)
                .offset { state.gamePadOffset.round() }
                .background(Color.White.copy(0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { _ -> state.stopAllAnimation() },
                        onDrag = { _, dragAmount -> state.move(dragAmount) },
                        onDragCancel = { state.rollback() },
                        onDragEnd = { state.rollback() },
                    )
                }
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_gamepad),
                contentDescription = "GamePad",
                tint = iconTint,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.7f)
            )
        }
    }
}