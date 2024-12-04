package com.github.panpf.zoomimage.sample.ui.components

import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.sample.EventBus
import kotlinx.coroutines.launch

data class PagerItem<T>(
    val data: T,
    val titleFactory: (data: T) -> String,
    val contentFactory: @Composable (data: T, index: Int, pageSelected: Boolean) -> Unit
)

@Composable
fun <T> HorizontalTabPager(pagerItems: Array<PagerItem<T>>) {
    val pagerState = rememberPagerState { pagerItems.size }
    val coroutineScope = rememberCoroutineScope()
    val focusRequest = remember { androidx.compose.ui.focus.FocusRequester() }
    Column(
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
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                val pageSelected by remember {
                    derivedStateOf {
                        pagerState.currentPage == index
                    }
                }
                val item = pagerItems[index]
                item.contentFactory(item.data, index, pageSelected)
            }

            TurnPageIndicator(pagerState)
        }
    }
    LaunchedEffect(Unit) {
        focusRequest.requestFocus()
    }
}
