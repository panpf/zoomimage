package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.cachedIn
import com.github.panpf.sketch.Sketch
import com.github.panpf.zoomimage.sample.ui.common.gridPagingConfig

class LocalPhotoListViewModel(sketch: Sketch) : ViewModel() {

    val pagingFlow = Pager(
        config = gridPagingConfig,
        initialKey = 0,
        pagingSourceFactory = {
            LocalPhotoListPagingSource(sketch)
        }
    ).flow.cachedIn(viewModelScope)
}