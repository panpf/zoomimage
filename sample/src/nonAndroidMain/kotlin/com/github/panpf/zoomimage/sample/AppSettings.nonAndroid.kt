package com.github.panpf.zoomimage.sample

import com.github.panpf.zoomimage.sample.util.ImageLoaderSettingItem

actual val composeImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: AsyncImage(Sketch)\nDetail: SketchZoomAsyncImage"),
    ImageLoaderSettingItem("Coil", "List: AsyncImage(Coil)\nDetail: CoilZoomAsyncImage"),
    ImageLoaderSettingItem("Basic", "List: Image + Sketch\nDetail: ZoomAsyncImage"),
)