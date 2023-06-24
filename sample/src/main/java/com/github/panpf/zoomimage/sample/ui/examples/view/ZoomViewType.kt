package com.github.panpf.zoomimage.sample.ui.examples.view

import com.github.panpf.assemblyadapter.pager.FragmentItemFactory

enum class ZoomViewType(
    val title: String,
    val createItemFactory: () -> FragmentItemFactory<String>
) {
    ZoomImageView(
        title = "ZoomImageView",
        createItemFactory = { ZoomImageViewFragment.ItemFactory() }
    ),

    SketchZoomImageView(
        title = "SketchZoomImageView",
        createItemFactory = { SketchZoomImageViewFragment.ItemFactory() }
    ),

    CoilZoomImageView(
        title = "CoilZoomImageView",
        createItemFactory = { CoilZoomImageViewFragment.ItemFactory() }
    ),

    GlideZoomImageView(
        title = "GlideZoomImageView",
        createItemFactory = { GlideZoomImageViewFragment.ItemFactory() }
    ),

    PicassoZoomImageView(
        title = "PicassoZoomImageView",
        createItemFactory = { PicassoZoomImageViewFragment.ItemFactory() }
    ),

    PhotoView(
        title = "PhotoView",
        createItemFactory = { PhotoViewFragment.ItemFactory() }
    ),

    SubsamplingScaleImageView(
        title = "SubsamplingScaleImageView",
        createItemFactory = { SubsamplingViewFragment.ItemFactory() }
    ),
}