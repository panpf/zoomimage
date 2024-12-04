@file:Suppress("EnumValuesSoftDeprecate")

package com.github.panpf.zoomimage.sample.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.LocalPlatformContext
import com.github.panpf.zoomimage.sample.appSettings
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.logo_zoomimage
import com.github.panpf.zoomimage.sample.ui.base.BaseScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

object HorHomeScreen : BaseScreen() {

    @Composable
    override fun DrawContent() {
        Row(Modifier.fillMaxSize()) {
            val coroutineScope = rememberCoroutineScope()
            val context = LocalPlatformContext.current
            val appSettings = context.appSettings
            val homeTabs = remember { HomeTab.values() }

            val pagerState = rememberPagerState(
                initialPage = appSettings.currentPageIndex.value.coerceIn(0, homeTabs.size - 1),
                pageCount = { homeTabs.size }
            )
            LaunchedEffect(Unit) {
                snapshotFlow { pagerState.currentPage }.collect { index ->
                    appSettings.currentPageIndex.value = index
                }
            }

            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                header = {
                    Image(
                        painter = painterResource(Res.drawable.logo_zoomimage),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 50.dp)
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            ) {
                homeTabs.forEachIndexed { index, homeTab ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                painter = painterResource(homeTab.icon),
                                contentDescription = homeTab.title,
                                modifier = Modifier.size(24.dp)
                                    .padding(homeTab.padding)
                            )
                        },
                        label = { Text(homeTab.title) },
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.scrollToPage(index) } },
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxHeight().weight(1f),
                userScrollEnabled = false,
            ) { pageIndex ->
                homeTabs[pageIndex].content.invoke(this@HorHomeScreen)
            }
        }
    }
}