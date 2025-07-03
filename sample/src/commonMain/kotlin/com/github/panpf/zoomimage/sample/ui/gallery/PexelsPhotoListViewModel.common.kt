package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.lifecycle.ViewModel
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi

expect class PexelsPhotoListViewModel(pexelsApi: PexelsApi) : ViewModel