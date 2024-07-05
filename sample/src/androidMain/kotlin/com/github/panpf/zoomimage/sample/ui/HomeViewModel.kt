package com.github.panpf.zoomimage.sample.ui

import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.ui.examples.compose.ZoomImageType
import com.github.panpf.zoomimage.sample.ui.examples.view.ZoomViewType
import com.github.panpf.zoomimage.sample.ui.model.Link
import com.github.panpf.zoomimage.sample.ui.model.ListSeparator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _data = MutableStateFlow<List<Any>>(emptyList())
    val data: StateFlow<List<Any>> = _data

    init {
        viewModelScope.launch {
            _data.value = buildData()
        }
    }

    private fun buildData(): List<Any> {
        return listOf(
            ListSeparator("Compose"),
            Link(
                title = "ZoomImage（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumComposeFragment(
                    ZoomImageType.MyZoomImage.name
                ),
                minSdk = 21,
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "SketchZoomAsyncImage（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumComposeFragment(
                    ZoomImageType.SketchZoomAsyncImage.name
                ),
                minSdk = 21,
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "CoilZoomAsyncImage（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumComposeFragment(
                    ZoomImageType.CoilZoomAsyncImage.name
                ),
                minSdk = 21,
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "GlideZoomAsyncImage（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumComposeFragment(
                    ZoomImageType.GlideZoomAsyncImage.name
                ),
                minSdk = 21,
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "ZoomableAsyncImage（Telephoto）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumComposeFragment(
                    ZoomImageType.TelephotoZoomableAsyncImage.name
                ),
                minSdk = 21,
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "Modifier.zoom()",
                navDirections = NavMainDirections.actionGlobalZoomFunctionSampleFragment(),
                minSdk = 21,
            ),

            ListSeparator("View"),
            Link(
                title = "ZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.ZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "SketchZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.SketchZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "CoilZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.CoilZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                minSdk = 21
            ),
            Link(
                title = "GlideZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.GlideZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "PicassoZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.PicassoZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                minSdk = 21
            ),
            Link(
                title = "PhotoView",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.PhotoView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "SubsamplingScaleImageView",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumViewFragment(ZoomViewType.SubsamplingScaleImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
        )
    }
}