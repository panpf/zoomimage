package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.AppSettings
import com.github.panpf.zoomimage.sample.EventBus
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.getComposeImageLoaderIcon
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_settings
import com.github.panpf.zoomimage.sample.resources.ic_swap_hor
import com.github.panpf.zoomimage.sample.resources.ic_swap_ver
import com.github.panpf.zoomimage.sample.ui.SwitchImageLoader
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.TurnPageIndicator
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.util.Platform
import com.github.panpf.zoomimage.sample.util.current
import com.github.panpf.zoomimage.sample.util.isMobile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

val photoPagerTopBarHeight = 80.dp

class PhotoPagerScreen(private val params: PhotoPagerScreenParams) : BaseScreen() {

    @Composable
    override fun DrawContent() {
        val coroutineScope = rememberCoroutineScope()
        val focusRequest = remember { androidx.compose.ui.focus.FocusRequester() }
        Box(
            Modifier.fillMaxSize()
                .focusable()
                .focusRequester(focusRequest)
                .onKeyEvent {
                    coroutineScope.launch {
                        EventBus.keyEvent.emit(it)
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

            val appSettings = LocalPlatformContext.current.appSettings
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

            PhotoPagerHeaders(params, pagerState, horizontalLayout, photoPaletteState)

            TurnPageIndicator(pagerState, photoPaletteState)

            if (!Platform.current.isMobile()) {
                GestureDialog(appSettings)
            }
        }
        LaunchedEffect(Unit) {
            focusRequest.requestFocus()
        }
    }
}

@Composable
expect fun PhotoPagerBackground(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PhotoPagerHeaders(
    params: PhotoPagerScreenParams,
    pagerState: PagerState,
    horizontalLayout: Boolean,
    photoPaletteState: MutableState<PhotoPalette>
) {
    val context = LocalPlatformContext.current
    val appSettings = context.appSettings
    val photoPalette by photoPaletteState
    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(TopAppBarDefaults.windowInsets)) {
        Row(
            modifier = Modifier.height(photoPagerTopBarHeight).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigator = LocalNavigator.current!!
            IconButton(
                onClick = { navigator.pop() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(40.dp).padding(8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            val switchImageLoaderDialogState = rememberMyDialogState()
            Box(
                modifier = Modifier.size(40.dp)
                    .clip(CircleShape)
                    .background(photoPalette.containerColor)
                    .clickable { switchImageLoaderDialogState.show() },
            ) {
                val imageLoaderName by appSettings.composeImageLoader.collectAsState()
                val imageLoaderIcon = getComposeImageLoaderIcon(imageLoaderName)
                Image(
                    painter = imageLoaderIcon,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(24.dp).clip(CircleShape).align(Alignment.Center),
                )
            }
            MyDialog(switchImageLoaderDialogState) {
                SwitchImageLoader {
                    switchImageLoaderDialogState.dismiss()
                }
            }

            Spacer(modifier = Modifier.size(10.dp))

            IconButton(
                onClick = { appSettings.horizontalPagerLayout.value = !horizontalLayout },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
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

            val zoomImageSettingsListDialogState = rememberMyDialogState()
            IconButton(
                onClick = { zoomImageSettingsListDialogState.show() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = photoPalette.containerColor,
                    contentColor = photoPalette.contentColor
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = "settings",
                    modifier = Modifier.size(40.dp).padding(8.dp)
                )
            }
            MyDialog(zoomImageSettingsListDialogState) {
                ZoomImageSettingList()
            }

            Spacer(modifier = Modifier.size(10.dp))

            Box(
                Modifier
                    .height(40.dp)
                    .background(
                        color = photoPalette.containerColor,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                val numberText by remember {
                    derivedStateOf {
                        val number = params.startPosition + pagerState.currentPage + 1
                        "${number}/${params.totalCount}"
                    }
                }
                Text(
                    text = numberText,
                    textAlign = TextAlign.Center,
                    color = photoPalette.contentColor,
                    style = TextStyle(lineHeight = 12.sp),
                )
            }
        }
    }
}

@Composable
fun GestureDialog(appSettings: AppSettings) {
    val pagerGuideShowed by appSettings.pagerGuideShowed.collectAsState()
    var showPagerGuide by remember { mutableStateOf(true) }
    if (!pagerGuideShowed && showPagerGuide) {
        AlertDialog(
            onDismissRequest = { showPagerGuide = false },
            title = { Text("Operation gestures") },
            text = {
                Text(
                    text = """The current page supports the following gestures or operations：
                            |1. Turn page:
                            |    1.2. Key.LeftBracket + (meta/ctrl)/alt, Key.RightBracket + (meta/ctrl)/alt
                            |    1.3. Key.DirectionLeft + (meta/ctrl)/alt, Key.DirectionRight + (meta/ctrl)/alt
                            |2. Scaling image：
                            |    2.1. Double-click the image with one finger
                            |    2.2. Double-click the image with one finger and slide up and down without letting go.
                            |    2.3. Pinch with two fingers
                            |    2.4. Mouse scroll scaling
                            |    2.5. Key.ZoomIn, Key.ZoomOut, 
                            |        Key.Equals + (meta/ctrl)/alt, Key.Minus + (meta/ctrl)/alt, 
                            |        Key.DirectionUp + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt
                            |3. Moving image：
                            |    3.1. Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight
                            """.trimMargin(),
                    fontSize = 12.sp
                )
            },
            dismissButton = {
                Button(onClick = { showPagerGuide = false }) {
                    Text("I Known")
                }
            },
            confirmButton = {
                Button(onClick = { appSettings.pagerGuideShowed.value = true }) {
                    Text("Not prompting")
                }
            }
        )
    }
}
