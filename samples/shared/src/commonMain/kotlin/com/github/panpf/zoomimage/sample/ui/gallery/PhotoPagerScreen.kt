package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import com.github.panpf.zoomimage.sample.AppEvents
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.components.TurnPageIndicator
import com.github.panpf.zoomimage.sample.util.Platform
import com.github.panpf.zoomimage.sample.util.current
import com.github.panpf.zoomimage.sample.util.isMobile
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PhotoPagerScreen(params: PhotoPagerScreenParams) {
    BaseScreen {
        val appEvents: AppEvents = koinInject()
        val coroutineScope = rememberCoroutineScope()
        val focusRequest = remember { androidx.compose.ui.focus.FocusRequester() }
        Box(
            Modifier.fillMaxSize()
                .focusable()
                .focusRequester(focusRequest)
                .onKeyEvent {
                    coroutineScope.launch {
                        appEvents.keyEvent.emit(it)
                    }
                    true
                }
        ) {
            val initialPage = remember { params.initialPosition - params.startPosition }
            val pagerState = rememberPagerState(initialPage = initialPage) {
                params.photos.size
            }

            val photo = params.photos[pagerState.currentPage]
            val colorScheme = MaterialTheme.colorScheme
            val photoPaletteState = remember { mutableStateOf(PhotoPalette(colorScheme)) }
            PhotoPagerBackground(photo.listThumbnailUrl, photoPaletteState)

            val appSettings: AppSettings = koinInject()
            val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState(initial = true)
            if (horizontalLayout) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val pageSelected by remember {
                        derivedStateOf {
                            pagerState.currentPage == index
                        }
                    }
                    val photo1 = params.photos[index]
                    PhotoDetail(
                        photo = photo1,
                        photoPaletteState = photoPaletteState,
                        pageSelected = pageSelected,
                    )
                }
            } else {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    val pageSelected by remember {
                        derivedStateOf {
                            pagerState.currentPage == index
                        }
                    }
                    val photo1 = params.photos[index]
                    PhotoDetail(
                        photo = photo1,
                        photoPaletteState = photoPaletteState,
                        pageSelected = pageSelected,
                    )
                }
            }

            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                PhotoPagerHeaders(params, pagerState, horizontalLayout, photoPaletteState)

                TurnPageIndicator(pagerState, photoPaletteState)

                if (!Platform.current.isMobile()) {
                    PhotoPagerGesturePromptDialog(appSettings)
                }
            }
        }
        LaunchedEffect(Unit) {
            focusRequest.requestFocus()
        }
    }
}