package com.github.panpf.zoomimage.sample.ui.compose

import com.github.panpf.assemblyadapter.pager.FragmentItemFactory
import com.github.panpf.zoomimage.sample.ui.view.photoview.PhotoViewFragment
import com.github.panpf.zoomimage.sample.ui.view.subsamplingview.SubsamplingViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.CoilZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.GlideZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.PicassoZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.SketchZoomImageViewFragment
import com.github.panpf.zoomimage.sample.ui.view.zoomimage.ZoomImageViewFragment

enum class ComposeZoomImageType(
    val title: String,
//    val createItemFactory: () -> FragmentItemFactory<String>
) {
    MyZoomImage(
        title = "MyZoomImage",
//        createItemFactory = { ZoomImageViewFragment.ItemFactory() }
    ),

    TelephotoZoomableImage(
        title = "TelephotoZoomableImage",
//        createItemFactory = { SketchZoomImageViewFragment.ItemFactory() }
    ),
}