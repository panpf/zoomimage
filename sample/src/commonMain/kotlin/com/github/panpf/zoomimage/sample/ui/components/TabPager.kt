package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.util.isMobile
import com.github.panpf.zoomimage.sample.util.runtimePlatformInstance
import kotlinx.coroutines.launch

data class PagerItem<T>(
    val data: T,
    val titleFactory: (data: T) -> String,
    val contentFactory: @Composable (data: T, index: Int) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> HorizontalTabPager(pagerItems: Array<PagerItem<T>>) {
    val pagerState = rememberPagerState { pagerItems.size }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.pagerTabIndicatorOffset3(pagerState, tabPositions),
                )
            }
        ) {
            pagerItems.forEachIndexed { index, item ->
                Tab(
                    selected = index == pagerState.currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    },
                ) {
                    Text(
                        text = item.titleFactory(item.data),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                beyondBoundsPageCount = 0,
                modifier = Modifier.fillMaxSize()
            ) { index ->
                val item = pagerItems[index]
                item.contentFactory(item.data, index)
            }

            if (!runtimePlatformInstance.isMobile()) {
                TurnPageIndicator(pagerState)
            }
        }
    }
}