package com.github.panpf.zoomimage.sample.ui.common.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class PagerItem<T>(
    val data: T,
    val titleFactory: (T) -> String,
    val contentFactory: @Composable (data: T, index: Int) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> HorizontalTabPager(pagerItems: Array<PagerItem<T>>) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset3(pagerState, tabPositions),
                    color = MaterialTheme.colorScheme.onPrimary
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
                    }
                ) {
                    Text(
                        text = item.titleFactory(item.data),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
        HorizontalPager(
            pageCount = pagerItems.size,
            state = pagerState,
            userScrollEnabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            val item = pagerItems[index]
            item.contentFactory(item.data, index)
        }
    }
}