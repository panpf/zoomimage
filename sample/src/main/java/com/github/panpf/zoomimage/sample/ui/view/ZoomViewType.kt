package com.github.panpf.zoomimage.sample.ui.view

import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.ui.view.subsamplingview.SubsamplingViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.ZoomImageViewFragment

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
        createItemFactory = { com.github.panpf.zoomimage.sample.ui.view.zoomimage.SketchZoomImageViewFragment.ItemFactory() }
    ),

    CoilZoomImageView(
        title = "CoilZoomImageView",
        createItemFactory = { com.github.panpf.zoomimage.sample.ui.view.zoomimage.CoilZoomImageViewFragment.ItemFactory() }
    ),

    GlideZoomImageView(
        title = "GlideZoomImageView",
        createItemFactory = { com.github.panpf.zoomimage.sample.ui.view.zoomimage.GlideZoomImageViewFragment.ItemFactory() }
    ),

    PicassoZoomImageView(
        title = "PicassoZoomImageView",
        createItemFactory = { PicassoZoomImageViewFragment.ItemFactory() }
    ),

    PhotoView(
        title = "PhotoView",
        createItemFactory = { com.github.panpf.zoomimage.sample.ui.view.photoview.PhotoViewFragment.ItemFactory() }
    ),

    SubsamplingScaleImageView(
        title = "SubsamplingScaleImageView",
        createItemFactory = { SubsamplingViewFragment.ItemFactory() }
    ),
}