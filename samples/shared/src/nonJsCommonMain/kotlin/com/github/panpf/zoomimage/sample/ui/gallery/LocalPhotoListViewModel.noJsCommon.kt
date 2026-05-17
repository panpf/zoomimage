package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.github.panpf.sketch.Sketch

actual class LocalPhotoListViewModel actual constructor(sketch: Sketch) : ViewModel() {

    val pagingFlow = app.cash.paging.Pager(
        config = app.cash.paging.PagingConfig(
            pageSize = 60,
            enablePlaceholders = false,
        ),
        initialKey = 0,
        pagingSourceFactory = {
            LocalPhotoListPagingSource(sketch)
        }
    ).flow.cachedIn(viewModelScope)
}