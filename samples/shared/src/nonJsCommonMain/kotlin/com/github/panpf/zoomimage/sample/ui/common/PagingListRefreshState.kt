package com.github.panpf.zoomimage.sample.ui.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateError
import app.cash.paging.compose.LazyPagingItems
import com.github.panpf.zoomimage.sample.ui.components.PageState

@Composable
fun PagingListRefreshState(
    pagingItems: LazyPagingItems<*>,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val pageState by remember {
        derivedStateOf {
            val refreshState = pagingItems.loadState.refresh
            if (refreshState is LoadStateError) {
                PageState.Error(refreshState.error.message) {
                    pagingItems.refresh()
                }
            } else {
                null
            }
        }
    }
    PageState(pageState = pageState, modifier = modifier)
}