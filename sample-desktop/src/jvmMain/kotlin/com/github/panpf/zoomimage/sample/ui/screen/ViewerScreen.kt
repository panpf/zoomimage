package com.github.panpf.zoomimage.sample.ui.screen

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import com.github.panpf.zoomimage.ZoomImage
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.fromResource
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageMinimap
import com.github.panpf.zoomimage.sample.compose.widget.ZoomImageTool
import com.github.panpf.zoomimage.sample.compose.widget.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.ImageResource
import com.github.panpf.zoomimage.sample.ui.navigation.Navigation
import com.github.panpf.zoomimage.sample.ui.util.EventBus
import com.github.panpf.zoomimage.subsampling.ImageSource
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.OneFingerScaleSpec

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

//        var lastMoveJob by remember { mutableStateOf<Job?>(null) }
//        val moveKeyboardState = rememberMoveKeyboardState()
        LaunchedEffect(Unit) {
            EventBus.keyEvent.collect { keyEvent ->
                when (keyEvent.key) {
                    Key.DirectionUp -> {
//                        lastMoveJob?.cancel()
                        if (!keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyUp) {
                            zoomState.zoomable.scale(
                                targetScale = zoomState.zoomable.transform.scaleX * 2f,
                                animated = true,
                            )
//                        } else if (keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyDown) {
//                            lastMoveJob = launch {
//                                while (isActive) {
//                                    zoomState.zoomable.offset(
//                                        targetOffset = zoomState.zoomable.transform.offset.copy(y = zoomState.zoomable.transform.offsetY + 100f),
//                                        animated = true
//                                    )
//                                    delay(100)
//                                }
//                            }
                        }
                    }

                    Key.DirectionDown -> {
//                        lastMoveJob?.cancel()
                        if (!keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyUp) {
                            zoomState.zoomable.scale(
                                targetScale = zoomState.zoomable.transform.scaleX * 0.5f,
                                animated = true,
                            )
//                        } else if (keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyDown) {
//                            lastMoveJob = launch {
//                                while (isActive) {
//                                    zoomState.zoomable.offset(
//                                        targetOffset = zoomState.zoomable.transform.offset.copy(y = zoomState.zoomable.transform.offsetY - 100f),
//                                        animated = true
//                                    )
//                                    delay(100)
//                                }
//                            }
                        }
                    }

//                    Key.DirectionLeft -> {
////                        lastMoveJob?.cancel()
//                        if (keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyDown) {
////                            lastMoveJob = launch {
////                                while (isActive) {
////                                    zoomState.zoomable.offset(
////                                        targetOffset = zoomState.zoomable.transform.offset.copy(x = zoomState.zoomable.transform.offsetX + 100f),
////                                        animated = true
////                                    )
////                                    delay(100)
////                                }
////                            }
//                            moveKeyboardState.move(Offset(1000f, 0f))
//                        } else if(keyEvent.type == KeyEventType.KeyUp) {
//                            moveKeyboardState.move(Offset(0f, 0f))
//                        }
//                    }
//
//                    Key.DirectionRight -> {
////                        lastMoveJob?.cancel()
//                        if (keyEvent.isMetaPressed && keyEvent.type == KeyEventType.KeyDown) {
////                            lastMoveJob = launch {
////                                while (isActive) {
////                                    zoomState.zoomable.offset(
////                                        targetOffset = zoomState.zoomable.transform.offset.copy(x = zoomState.zoomable.transform.offsetX - 100f),
////                                        animated = true
////                                    )
////                                    delay(100)
////                                }
////                            }
//                            moveKeyboardState.move(Offset(-1000f, 0f))
//                        } else if(keyEvent.type == KeyEventType.KeyUp) {
//                            moveKeyboardState.move(Offset(0f, 0f))
//                        }
//                    }
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
//            moveKeyboardState = moveKeyboardState,
            infoDialogState = infoDialogState,
        )
    }
}