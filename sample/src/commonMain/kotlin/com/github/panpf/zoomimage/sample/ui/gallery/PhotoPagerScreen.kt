package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_arrow_down
import com.github.panpf.zoomimage.sample.resources.ic_arrow_left
import com.github.panpf.zoomimage.sample.resources.ic_arrow_right
import com.github.panpf.zoomimage.sample.resources.ic_arrow_up
import com.github.panpf.zoomimage.sample.resources.ic_settings
import com.github.panpf.zoomimage.sample.resources.ic_swap_hor
import com.github.panpf.zoomimage.sample.resources.ic_swap_ver
import com.github.panpf.zoomimage.sample.ui.ZoomImageOptionsDialog
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.rememberZoomImageOptionsState
import com.github.panpf.zoomimage.sample.util.isMobile
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

class PhotoPagerScreen(private val params: PhotoPagerParams) : BaseScreen() {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawContent() {
        Box(Modifier.fillMaxSize()) {
            val appSettings = LocalPlatformContext.current.appSettings
            val initialPage = remember { params.initialPosition - params.startPosition }
            val pagerState = rememberPagerState(initialPage = initialPage) {
                params.photos.size
            }
            val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState(initial = true)
            if (horizontalLayout) {
                HorizontalPager(
                    state = pagerState,
                    beyondBoundsPageCount = 0,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    SketchZoomAsyncImageSample(params.photos[index].originalUrl)
                }
            } else {
                VerticalPager(
                    state = pagerState,
                    beyondBoundsPageCount = 0,
                    modifier = Modifier.fillMaxSize()
                ) { index ->
                    SketchZoomAsyncImageSample(params.photos[index].originalUrl)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                val navigator = LocalNavigator.current!!
                IconButton(
                    onClick = { navigator.pop() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = colorScheme.onTertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(40.dp).padding(8.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { appSettings.horizontalPagerLayout.value = !horizontalLayout },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = colorScheme.onTertiary
                    )
                ) {
                    val icon = if (horizontalLayout) {
                        painterResource(Res.drawable.ic_swap_ver)
                    } else {
                        painterResource(Res.drawable.ic_swap_hor)
                    }
                    Icon(
                        painter = icon,
                        contentDescription = "orientation",
                        modifier = Modifier.size(40.dp).padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.size(10.dp))

                Box(
                    Modifier
                        .width(40.dp)
                        .background(
                            color = colorScheme.tertiary,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val number by remember {
                        derivedStateOf {
                            (pagerState.currentPage + 1).coerceAtMost(999)
                        }
                    }
                    val numberCount by remember {
                        derivedStateOf {
                            (params.startPosition + params.photos.size).coerceAtMost(999)
                        }
                    }
                    val numberText by remember {
                        derivedStateOf {
                            "${number}\n·\n$numberCount"
                        }
                    }
                    Text(
                        text = numberText,
                        textAlign = TextAlign.Center,
                        color = colorScheme.onTertiary,
                        style = TextStyle(lineHeight = 12.sp),
                    )
                }

                Spacer(modifier = Modifier.size(10.dp))

                var showSettingsDialog by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showSettingsDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.tertiary,
                        contentColor = colorScheme.onTertiary
                    )
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_settings),
                        contentDescription = "settings",
                        modifier = Modifier.size(40.dp).padding(8.dp)
                    )
                }
                if (showSettingsDialog) {
                    ZoomImageOptionsDialog(
                        my = true,
                        state = rememberZoomImageOptionsState()
                    ) {
                        showSettingsDialog = false
                    }
                }
            }

            TurnPageIndicator(pagerState)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun BoxScope.TurnPageIndicator(pagerState: PagerState) {
    if (runtimePlatformInstance.isMobile()) return
    val turnPage = remember { MutableSharedFlow<Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        EventBus.keyEvent.collect { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyUp && !keyEvent.isMetaPressed) {
                when (keyEvent.key) {
                    Key.PageUp, Key.DirectionLeft -> turnPage.emit(true)
                    Key.PageDown, Key.DirectionRight -> turnPage.emit(false)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        turnPage.collect {
            if (it) {
                val nextPageIndex = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPageIndex)
            } else {
                val nextPageIndex =
                    (pagerState.currentPage - 1).let { if (it < 0) pagerState.pageCount + it else it }
                pagerState.animateScrollToPage(nextPageIndex)
            }
        }
    }
    val turnPageIconModifier = Modifier
        .padding(20.dp)
        .size(50.dp)
        .clip(CircleShape)
    val appSettings = LocalPlatformContext.current.appSettings
    val horizontalLayout by appSettings.horizontalPagerLayout.collectAsState(initial = true)
    if (horizontalLayout) {
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(false) } },
            modifier = turnPageIconModifier.align(Alignment.CenterStart),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(0.5f),
                contentColor = Color.White
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = "Previous",
            )
        }
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(true) } },
            modifier = turnPageIconModifier.align(Alignment.CenterEnd),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(0.5f),
                contentColor = Color.White
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_right),
                contentDescription = "Next",
            )
        }
    } else {
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(false) } },
            modifier = turnPageIconModifier.align(Alignment.TopCenter),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(0.5f),
                contentColor = Color.White
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_up),
                contentDescription = "Previous",
            )
        }
        IconButton(
            onClick = { coroutineScope.launch { turnPage.emit(true) } },
            modifier = turnPageIconModifier.align(Alignment.BottomCenter),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black.copy(0.5f),
                contentColor = Color.White
            ),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = "Next",
            )
        }
    }
}