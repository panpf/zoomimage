package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.compose.util.AssistKey
import com.github.panpf.zoomimage.compose.util.KeyMatcher
import com.github.panpf.zoomimage.compose.util.matcherKeyHandler
import com.github.panpf.zoomimage.compose.util.platformAssistKey
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_arrow_down
import com.github.panpf.zoomimage.sample.resources.ic_arrow_left
import com.github.panpf.zoomimage.sample.resources.ic_arrow_right
import com.github.panpf.zoomimage.sample.resources.ic_arrow_up
import com.github.panpf.zoomimage.sample.util.Platform
import com.github.panpf.zoomimage.sample.util.current
import com.github.panpf.zoomimage.sample.util.isMobile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


@Composable
fun TurnPageIndicator(
    pagerState: PagerState,
    photoPaletteState: MutableState<PhotoPalette>? = null
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val keyHandlers = listOf(
            matcherKeyHandler(
                listOf(
                    KeyMatcher(Key.LeftBracket, AssistKey.Alt, type = KeyEventType.KeyUp),
                    KeyMatcher(Key.LeftBracket, platformAssistKey(), type = KeyEventType.KeyUp),
                    KeyMatcher(Key.DirectionLeft, AssistKey.Alt, type = KeyEventType.KeyUp),
                    KeyMatcher(Key.DirectionLeft, platformAssistKey(), type = KeyEventType.KeyUp),
                )
            ) {
                coroutineScope.launch {
                    pagerState.previousPage()
                }
            },
            matcherKeyHandler(
                listOf(
                    KeyMatcher(Key.RightBracket, AssistKey.Alt, type = KeyEventType.KeyUp),
                    KeyMatcher(Key.RightBracket, platformAssistKey(), type = KeyEventType.KeyUp),
                    KeyMatcher(Key.DirectionRight, AssistKey.Alt, type = KeyEventType.KeyUp),
                    KeyMatcher(Key.DirectionRight, platformAssistKey(), type = KeyEventType.KeyUp),
                )
            ) {
                coroutineScope.launch {
                    pagerState.nextPage()
                }
            }
        )
        EventBus.keyEvent.collect { keyEvent ->
            keyHandlers.any {
                it.handle(keyEvent)
            }
        }
    }
    if (!Platform.current.isMobile()) {
        val turnPageIconModifier = Modifier
            .padding(50.dp)
            .size(50.dp)
            .clip(CircleShape)
        val appSettings = LocalPlatformContext.current.appSettings
        val colorScheme = MaterialTheme.colorScheme
        val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState(initial = true)
        val photoPalette by photoPaletteState
            ?: remember { mutableStateOf(PhotoPalette(colorScheme)) }
        Box(Modifier.fillMaxSize()) {
            if (horizontalLayout) {
                IconButton(
                    onClick = { coroutineScope.launch { pagerState.previousPage() } },
                    modifier = turnPageIconModifier.align(Alignment.CenterStart),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = photoPalette.containerColor,
                        contentColor = photoPalette.contentColor
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_left),
                        contentDescription = "Previous",
                    )
                }
                IconButton(
                    onClick = { coroutineScope.launch { pagerState.nextPage() } },
                    modifier = turnPageIconModifier.align(Alignment.CenterEnd),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = photoPalette.containerColor,
                        contentColor = photoPalette.contentColor
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_right),
                        contentDescription = "Next",
                    )
                }
            } else {
                IconButton(
                    onClick = { coroutineScope.launch { pagerState.previousPage() } },
                    modifier = turnPageIconModifier.align(Alignment.TopCenter),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = photoPalette.containerColor,
                        contentColor = photoPalette.contentColor
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_up),
                        contentDescription = "Previous",
                    )
                }
                IconButton(
                    onClick = { coroutineScope.launch { pagerState.nextPage() } },
                    modifier = turnPageIconModifier.align(Alignment.BottomCenter),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = photoPalette.containerColor,
                        contentColor = photoPalette.contentColor
                    ),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_down),
                        contentDescription = "Next",
                    )
                }
            }
        }
    }
}

suspend fun PagerState.nextPage() {
    val nextPageIndex = (currentPage + 1) % pageCount
    animateScrollToPage(nextPageIndex)
}

suspend fun PagerState.previousPage() {
    val previousPageIndex = (currentPage - 1).let { if (it < 0) pageCount + it else it }
    animateScrollToPage(previousPageIndex)
}