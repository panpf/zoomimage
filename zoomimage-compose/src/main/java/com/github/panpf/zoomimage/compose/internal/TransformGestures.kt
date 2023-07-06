//package com.github.panpf.zoomimage.compose.internal
//
//import android.util.Log
//import androidx.compose.foundation.gestures.awaitEachGesture
//import androidx.compose.foundation.gestures.awaitFirstDown
//import androidx.compose.foundation.gestures.calculateCentroid
//import androidx.compose.foundation.gestures.calculateCentroidSize
//import androidx.compose.foundation.gestures.calculatePan
//import androidx.compose.foundation.gestures.calculateRotation
//import androidx.compose.foundation.gestures.calculateZoom
//import androidx.compose.foundation.gestures.detectTransformGestures
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.composed
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.TransformOrigin
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.PointerInputScope
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.input.pointer.positionChanged
//import androidx.compose.ui.layout.onSizeChanged
//import androidx.compose.ui.unit.toSize
//import androidx.compose.ui.util.fastAny
//import androidx.compose.ui.util.fastForEach
//import com.github.panpf.zoomimage.ZoomableState
//import kotlinx.coroutines.launch
//import kotlin.math.PI
//import kotlin.math.abs
//import kotlin.math.cos
//import kotlin.math.sin
//
///**
// * A gesture detector for rotation, panning, and zoom. Once touch slop has been reached, the
// * user can use rotation, panning and zoom gestures. [onGesture] will be called when any of the
// * rotation, zoom or pan occurs, passing the rotation angle in degrees, zoom in scale factor and
// * pan as an offset in pixels. Each of these changes is a difference between the previous call
// * and the current gesture. This will consume all position changes after touch slop has
// * been reached. [onGesture] will also provide centroid of all the pointers that are down.
// *
// * If [panZoomLock] is `true`, rotation is allowed only if touch slop is detected for rotation
// * before pan or zoom motions. If not, pan and zoom gestures will be detected, but rotation
// * gestures will not be. If [panZoomLock] is `false`, once touch slop is reached, all three
// * gestures are detected.
// *
// * Example Usage:
// * @sample androidx.compose.foundation.samples.DetectTransformGestures
// */
//suspend fun PointerInputScope.detectTransformGestures2(
//    panZoomLock: Boolean = false,
//    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
//) {
//    awaitEachGesture {
//        var rotation = 0f
//        var zoom = 1f
//        var pan = Offset.Zero
//        var pastTouchSlop = false
//        val touchSlop = viewConfiguration.touchSlop
//        var lockedToPanZoom = false
//
//        awaitFirstDown(requireUnconsumed = false)
//        do {
//            val event = awaitPointerEvent()
//            val canceled = event.changes.fastAny { it.isConsumed }
//            if (!canceled) {
//                val zoomChange = event.calculateZoom()
//                val rotationChange = event.calculateRotation()
//                val panChange = event.calculatePan()
//
//                if (!pastTouchSlop) {
//                    zoom *= zoomChange
//                    rotation += rotationChange
//                    pan += panChange
//
//                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
//                    val zoomMotion = abs(1 - zoom) * centroidSize
//                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
//                    val panMotion = pan.getDistance()
//
//                    if (zoomMotion > touchSlop ||
//                        rotationMotion > touchSlop ||
//                        panMotion > touchSlop
//                    ) {
//                        pastTouchSlop = true
//                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
//                    }
//                }
//
//                if (pastTouchSlop) {
//                    val centroid = event.calculateCentroid(useCurrent = false)
//                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
//                    if (effectiveRotation != 0f ||
//                        zoomChange != 1f ||
//                        panChange != Offset.Zero
//                    ) {
//                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
//                    }
//                    event.changes.fastForEach {
//                        if (it.positionChanged()) {
//                            it.consume()
//                        }
//                    }
//                }
//            }
//        } while (!canceled && event.changes.fastAny { it.pressed })
//    }
//}
//
//
//fun Modifier.zoomable2(state: ZoomableState): Modifier = composed {
//    val coroutineScope = rememberCoroutineScope()
//    this
//        .onSizeChanged {
//            val newContainerSize = it.toSize()
//            val oldContainerSize = state.containerSize
//            if (newContainerSize != oldContainerSize) {
//                state.containerSize = newContainerSize
//            }
//        }
//        .pointerInput(Unit) {
//            detectTransformGestures { centroid, pan, gestureZoom, gestureRotate ->
//                coroutineScope.launch {
//                    val oldScale = state.scale
//                    val oldOffset = state.offset
//                    val oldOriginOffset = oldOffset
//                        .let { Offset(it.x / oldScale * -1, it.y / oldScale * -1) }
//
//                    val newScale = oldScale * gestureZoom
//                    // For natural zooming and rotating, the centroid of the gesture should
//                    // be the fixed point where zooming and rotating occurs.
//                    // We compute where the centroid was (in the pre-transformed coordinate
//                    // space), and then compute where it will be after this delta.
//                    // We then compute what the new offset should be to keep the centroid
//                    // visually stationary for rotating and zooming, and also apply the pan.
//                    val newOriginOffset = (oldOriginOffset + centroid / oldScale).rotateBy(gestureRotate) -
//                            (centroid / newScale + pan / oldScale)
//                    val newOffset = newOriginOffset
//                        .let { Offset(it.x * newScale * -1, it.y * newScale * -1) }
//                    Log.d("TransformGestures", "zoomable2. detectTransformGestures. " +
//                            "scale: $oldScale -> $newScale, " +
//                            "offset: ${oldOffset.toShortString()} -> ${newOffset.toShortString()}, " +
//                            "originOffset: ${oldOriginOffset.toShortString()} -> ${newOriginOffset.toShortString()}")
//                    state.snapScaleTo(newScale)
//                    state.snapOffsetTo(newOffset)
//                }
//            }
//        }
//        .graphicsLayer {
//            Log.d("TransformGestures", "zoomable2. graphicsLayer. scale: ${state.scale}, translation: ${state.offset.toShortString()}")
//            scaleX = state.scale
//            scaleY = state.scale
//            rotationZ = state.rotation
//            translationX = state.offset.x
//            translationY = state.offset.y
//            transformOrigin = state.transformOrigin
//        }
//}
//
//
//fun Modifier.zoomable3(state1: ZoomableState): Modifier = composed {
//    val coroutineScope = rememberCoroutineScope()
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    var scale by remember { mutableStateOf(1f) }
//    this
//        .pointerInput(Unit) {
//            detectTransformGestures(true) { centroid, pan, gestureZoom, gestureRotate ->
//                val oldScale = scale
//                val oldOffset = offset
//                val oldOriginOffset = oldOffset
//                    .let { Offset(it.x / oldScale * -1, it.y / oldScale * -1) }
//
//                val newScale = oldScale * gestureZoom
//                // For natural zooming and rotating, the centroid of the gesture should
//                // be the fixed point where zooming and rotating occurs.
//                // We compute where the centroid was (in the pre-transformed coordinate
//                // space), and then compute where it will be after this delta.
//                // We then compute what the new offset should be to keep the centroid
//                // visually stationary for rotating and zooming, and also apply the pan.
//                val newOriginOffset = (oldOriginOffset + centroid / oldScale).rotateBy(gestureRotate) -
//                        (centroid / newScale + pan / oldScale)
//                val newOffset = newOriginOffset
//                    .let { Offset(it.x * newScale * -1, it.y * newScale * -1) }
//                Log.d("TransformGestures", "zoomable3. detectTransformGestures. " +
//                        "scale: $oldScale -> $newScale, " +
//                        "offset: ${oldOffset.toShortString()} -> ${newOffset.toShortString()}, " +
//                        "originOffset: ${oldOriginOffset.toShortString()} -> ${newOriginOffset.toShortString()}")
//                scale = newScale
//                offset = newOffset
//            }
//        }
//        .graphicsLayer {
//            Log.d("TransformGestures", "zoomable3. graphicsLayer. scale: ${scale}, translation: ${offset.toShortString()}")
//            scaleX = scale
//            scaleY = scale
//            translationX = offset.x
//            translationY = offset.y
//            transformOrigin = TransformOrigin(0f, 0f)
//        }
//}
//
///**
// * Rotates the given offset around the origin by the given angle in degrees.
// *
// * A positive angle indicates a counterclockwise rotation around the right-handed 2D Cartesian
// * coordinate system.
// *
// * See: [Rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix)
// */
//fun Offset.rotateBy(angle: Float): Offset {
//    val angleInRadians = angle * PI / 180
//    return Offset(
//        (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
//        (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
//    )
//}