package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import app.cash.paging.PagingData
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.flow.Flow

actual class PexelsPhotoListViewModel actual constructor(val pexelsApi: PexelsApi) : ViewModel() {

    val pagingFlow: Flow<PagingData<Photo>> = app.cash.paging.Pager(
        config = app.cash.paging.PagingConfig(
            pageSize = 60,
            enablePlaceholders = false,
        ),
        initialKey = 0,
        pagingSourceFactory = {
            PexelsPhotoListPagingSource(pexelsApi)
        }
    ).flow.cachedIn(viewModelScope)
}