package com.github.panpf.zoomimage.sample.ui.view

import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.ui.view.photoview.PhotoViewFragment
import com.github.panpf.zoomimage.sample.ui.view.subsamplingview.SubsamplingViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.CoilZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.GlideZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.SketchZoomImageViewFragment
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