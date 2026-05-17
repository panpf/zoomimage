package com.github.panpf.zoomimage.sample.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import com.github.panpf.zoomimage.sample.ui.components.AppendState

@Composable
fun PagingListAppendState(pagingItems: LazyPagingItems<*>) {
    val state by remember {
        derivedStateOf {
            when (val loadState = pagingItems.loadState.append) {
                is LoadStateLoading -> AppendState.Loading
                is LoadStateError -> AppendState.Error { pagingItems.retry() }
                is LoadStateNotLoading -> if (loadState.endOfPaginationReached) AppendState.End else null
                else -> null
            }
        }
    }
    AppendState(state)
}