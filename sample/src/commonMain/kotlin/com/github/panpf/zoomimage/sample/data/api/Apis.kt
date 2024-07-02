package com.github.panpf.zoomimage.sample.data.api

import com.github.panpf.zoomimage.sample.data.api.giphy.GiphyApi
import com.github.panpf.zoomimage.sample.data.api.pexels.PexelsApi

object Apis {
    val pexelsApi = PexelsApi(Client.client)
    val giphyApi = GiphyApi(Client.client)
}