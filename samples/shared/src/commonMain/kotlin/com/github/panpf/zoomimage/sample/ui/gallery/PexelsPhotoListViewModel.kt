package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.coroutines.flow.Flow

class PexelsPhotoListViewModel(val pexelsApi: PexelsApi) : ViewModel() {

    val pagingFlow: Flow<PagingData<Photo>> = Pager(
        config = PagingConfig(
            pageSize = 60,
            enablePlaceholders = false,
        ),
        initialKey = 0,
        pagingSourceFactory = {
            PexelsPhotoListPagingSource(pexelsApi)
        }
    ).flow.cachedIn(viewModelScope)
}