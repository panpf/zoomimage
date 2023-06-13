package com.github.panpf.zoomimage

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

@Composable
fun MyZoomImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    state: MyZoomState = rememberMyZoomState(),
    scaleAnimationConfig: ScaleAnimationConfig = ScaleAnimationConfig(),
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.then(
            Modifier.createZoomModifier(
                state = state,
                painter = painter,
                contentScale = contentScale,
                alignment = alignment,
                scaleAnimationConfig = scaleAnimationConfig
            )
        ),
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

@Composable
fun rememberMyZoomState(
    @FloatRange(from = 0.0) minScale: Float = 1f,
    @FloatRange(from = 0.0) maxScale: Float = 4f,
    debugMode: Boolean = false
): MyZoomState {
    return rememberSaveable(saver = MyZoomState.Saver) {
        MyZoomState(minScale = minScale, maxScale = maxScale, debugMode = debugMode)
    }
}

private fun Modifier.createZoomModifier(
    state: MyZoomState,
    painter: Painter,
    contentScale: ContentScale,
    alignment: Alignment,
    scaleAnimationConfig: ScaleAnimationConfig = ScaleAnimationConfig()
): Modifier = composed {
    // todo compat viewpager
    val coroutineScope = rememberCoroutineScope()
    val centroidState = remember { mutableStateOf(Offset.Zero) }
//    val transformableEnabledState = remember { mutableStateOf(false) }
    Modifier
        .onSizeChanged {
            state.init(
                containerSize = it.toSize(),
                contentSize = painter.intrinsicSize,
                contentScale = contentScale,
                contentAlignment = alignment
            )
        }
        .pointerInput(scaleAnimationConfig) {
            detectTapGestures(onDoubleTap = { offset ->
                coroutineScope.launch {
                    val newScale = state.getNextStepScale()
                    if (scaleAnimationConfig.animateDoubleTapScale) {
                        state.animateScaleTo(
                            newScale = newScale,
                            touchPosition = offset,
                            animationDurationMillis = scaleAnimationConfig.animationDurationMillis,
                            animationEasing = scaleAnimationConfig.animationEasing,
                            initialVelocity = scaleAnimationConfig.initialVelocity
                        )
                    } else {
                        state.snapScaleTo(newScale = newScale, touchPosition = offset)
                    }
                }
            })
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    coroutineScope.launch {
                        state.dragStart()
                    }
                },
                onDrag = { change, dragAmount ->
                    coroutineScope.launch {
                        state.drag(change, dragAmount)
                    }
                },
                onDragEnd = {
                    coroutineScope.launch {
                        state.dragEnd()
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        state.dragCancel()
                    }
                }
            )
        }
        .pointerInputCentroid {
            centroidState.value = it
        }
//        .pointerInput(Unit) {
//            detectTwoDowns {
//                transformableEnabledState.value = it
//            }
//        }
//        .transformable(
//            state = rememberTransformableState { zoomChange: Float, panChange: Offset, rotationChange: Float ->
//                coroutineScope.launch {
//                    state.transform(
//                        zoomChange = zoomChange,
//                        panChange = panChange,
//                        rotationChange = rotationChange,
//                        touchCentroid = centroidState.value
//                    )
//                }
//            },
//            lockRotationOnZoomPan = true,
//            enabled = transformableEnabledState.value
////            enabled = true
//        )
        .transformableTwoDowns(
            state = rememberTransformableState { zoomChange: Float, panChange: Offset, rotationChange: Float ->
                coroutineScope.launch {
                    state.transform(
                        zoomChange = zoomChange,
                        touchCentroid = centroidState.value
                    )
                }
            },
            lockRotationOnZoomPan = true,
//            enabled = true
        )
        .clipToBounds()
        .graphicsLayer {
            scaleX = state.scale
            scaleY = state.scale
            translationX = state.translation.x
            translationY = state.translation.y
            transformOrigin = state.transformOrigin
        }
//        .rotate(0f)// todo rotation
}