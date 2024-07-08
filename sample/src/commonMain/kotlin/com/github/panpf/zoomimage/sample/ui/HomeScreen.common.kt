@file:Suppress("EnumValuesSoftDeprecate")

package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.getComposeImageLoaderIcon
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_debug
import com.github.panpf.zoomimage.sample.resources.ic_layout_grid
import com.github.panpf.zoomimage.sample.resources.ic_layout_grid_staggered
import com.github.panpf.zoomimage.sample.resources.ic_pexels
import com.github.panpf.zoomimage.sample.resources.ic_phone
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import com.github.panpf.zoomimage.sample.ui.gallery.LocalPhotoListPage
import com.github.panpf.zoomimage.sample.ui.gallery.PexelsPhotoListPage
import com.github.panpf.zoomimage.sample.ui.test.TestPage
import com.github.panpf.zoomimage.sample.util.isMobile
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

val gridCellsMinSize: Dp = if (runtimePlatformInstance.isMobile()) 100.dp else 150.dp

@Composable
expect fun HomeHeader()

enum class HomeTab(
    val title: String,
    val icon: DrawableResource,
    val content: @Composable Screen.() -> Unit
) {
    LOCAL("Local", Res.drawable.ic_phone, { LocalPhotoListPage() }),
    PEXELS("Pexels", Res.drawable.ic_pexels, { PexelsPhotoListPage() }),
    TEST("Test", Res.drawable.ic_debug, { TestPage() }),
}

// TODO Make it a way to switch image loaders, and then clear the memory cache of other image loaders when switching, so that the current one can use the largest memory cache possible
object HomeScreen : BaseScreen() {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun DrawContent() {
        Column {
            HomeHeader()

            val coroutineScope = rememberCoroutineScope()
            val context = LocalPlatformContext.current
            val appSettings = context.appSettings
            val homeTabs = remember {
                HomeTab.values()
            }

            Column {
                val pagerState = rememberPagerState(
                    initialPage = appSettings.currentPageIndex.value.coerceIn(0, homeTabs.size - 1),
                    pageCount = { homeTabs.size }
                )
                LaunchedEffect(Unit) {
                    snapshotFlow { pagerState.currentPage }.collect { index ->
                        appSettings.currentPageIndex.value = index
                    }
                }
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { pageIndex ->
                        homeTabs[pageIndex].content.invoke(this@HomeScreen)
                    }

                    BottomToolbar(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                    )
                }

                NavigationBar(Modifier.fillMaxWidth()) {
                    homeTabs.forEachIndexed { index, homeTab ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(homeTab.icon),
                                    contentDescription = homeTab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(homeTab.title) },
                            selected = pagerState.currentPage == index,
                            onClick = { coroutineScope.launch { pagerState.scrollToPage(index) } }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun BottomToolbar(modifier: Modifier) {
        val context = LocalPlatformContext.current
        val appSettings = context.appSettings
        Row(
            modifier
                .background(colorScheme.tertiaryContainer, RoundedCornerShape(50))
                .padding(horizontal = 10.dp)
        ) {
            val staggeredGridMode by appSettings.staggeredGridMode.collectAsState()
            val staggeredGridModeIcon = if (!staggeredGridMode) {
                painterResource(Res.drawable.ic_layout_grid_staggered)
            } else {
                painterResource(Res.drawable.ic_layout_grid)
            }
            IconButton(
                onClick = { appSettings.staggeredGridMode.value = !staggeredGridMode },
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = staggeredGridModeIcon,
                    contentDescription = null,
                    tint = colorScheme.onTertiaryContainer
                )
            }

            var showSwitchImageLoaderDialog by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .clickable { showSwitchImageLoaderDialog = true },
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
            if (showSwitchImageLoaderDialog) {
                SwitchImageLoaderDialog {
                    showSwitchImageLoaderDialog = false
                }
            }
        }
    }
}