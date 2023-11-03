package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.fromResource
import com.github.panpf.zoomimage.compose.zoom.ZoomableState
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.compose.widget.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.ImageResource
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation
import com.github.panpf.zoomimage.sample.ui.util.EventBus
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun ViewerScreen(navigation: Navigation, imageResource: ImageResource) {
    Box(Modifier.fillMaxSize()) {
        val zoomState = rememberZoomState()
        LaunchedEffect(Unit) {
            zoomState.logger.level = Logger.DEBUG
            val imageSource = ImageSource.fromResource(imageResource.resourcePath)
            zoomState.subsampling.setImageSource(imageSource)
            zoomState.zoomable.oneFingerScaleSpec = OneFingerScaleSpec.Default
        }

        var lastMoveJob by remember { mutableStateOf<Job?>(null) }
        LaunchedEffect(Unit) {
            EventBus.keyEvent.collect { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    val zoomIn = when {
                        keyEvent.key == Key.Equals && keyEvent.isMetaPressed -> true
                        keyEvent.key == Key.Minus && keyEvent.isMetaPressed -> false
                        keyEvent.key == Key.DirectionUp && !keyEvent.isMetaPressed -> true
                        keyEvent.key == Key.DirectionDown && !keyEvent.isMetaPressed -> false
                        else -> null
                    }
                    if (zoomIn != null) {
                        zoomState.zoomable.scale(
                            targetScale = zoomState.zoomable.transform.scaleX * if (zoomIn) 2f else 0.5f,
                            animated = true,
                        )
                    }
                }

                lastMoveJob?.cancel()
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isMetaPressed) {
                    val direction = when (keyEvent.key) {
                        Key.DirectionLeft -> 1
                        Key.DirectionUp -> 2
                        Key.DirectionRight -> 3
                        Key.DirectionDown -> 4
                        else -> null
                    }
                    if (direction != null) {
                        lastMoveJob = launch {
                            move(zoomState.zoomable, direction)
                        }
                    }
                }
            }
        }

        ZoomImage(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(imageResource.thumbnailResourcePath),
            contentDescription = "Viewer",
            state = zoomState,
        )

        ZoomImageMinimap(
            imageUri = imageResource.thumbnailResourcePath,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
        )

        val infoDialogState = rememberMyDialogState()
        ZoomImageTool(
            imageUri = imageResource.thumbnailResourcePath,
            zoomableState = zoomState.zoomable,
            subsamplingState = zoomState.subsampling,
            infoDialogState = infoDialogState,
        )
    }
}

private suspend fun CoroutineScope.move(zoomable: ZoomableState, direction: Int) {
    val startTime = System.currentTimeMillis()
    while (isActive) {
        val containerSize = zoomable.containerSize
        val maxStep = Offset(
            containerSize.width / 20f,
            containerSize.height / 20f
        )
        val offset = zoomable.transform.offset
        val currentTime = System.currentTimeMillis()
        val time = currentTime - startTime
        val scale = when {
            time < 2000 -> 1f
            time < 4000 -> 2f
            time < 8000 -> 4f
            else -> 8f
        }
        val newOffset = when (direction) {
            1 -> offset.copy(x = offset.x + maxStep.x * scale)  // left
            2 -> offset.copy(y = offset.y + maxStep.y * scale)  // up
            3 -> offset.copy(x = offset.x - maxStep.x * scale)  // right
            4 -> offset.copy(y = offset.y - maxStep.y * scale)  // down
            else -> offset
        }
        zoomable.offset(
            targetOffset = newOffset,
            animated = false
        )
        delay(8)
    }
}