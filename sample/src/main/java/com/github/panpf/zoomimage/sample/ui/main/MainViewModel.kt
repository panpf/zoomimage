package com.github.panpf.zoomimage.sample.ui.main

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.panpf.zoomimage.sample.NavMainDirections
import com.github.panpf.zoomimage.sample.SampleImages.Asset
import com.github.panpf.zoomimage.sample.ui.model.Link
import com.github.panpf.zoomimage.sample.ui.model.ListSeparator
import com.github.panpf.zoomimage.sample.ui.view.ZoomViewType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _data = MutableStateFlow<List<Any>>(emptyList())
    val data: StateFlow<List<Any>> = _data

    init {
        viewModelScope.launch {
            exportAssetImages(application)
            _data.value = buildData()
        }
    }

    private suspend fun exportAssetImages(context: Context) {
        withContext(Dispatchers.IO) {
            Asset.ALL.forEach {
                val file = File(context.getExternalFilesDir("assets"), it.fileName)
                if (!file.exists()) {
                    context.assets.open(it.fileName).use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        }
    }

    private fun buildData(): List<Any> {
        return listOf(
            ListSeparator("Compose"),
            Link(
                title = "ZoomImage（My）",
                navDirections = NavMainDirections.actionGlobalMyZoomImagePagerFragment(),
                minSdk = 21
            ),
            Link(
                title = "ZoomableAsyncImage（Telephoto）",
                navDirections = NavMainDirections.actionGlobalTelephotoZoomableAsyncImagePagerFragment(),
                minSdk = 21
            ),

            ListSeparator("View"),
            Link(
                title = "ZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.ZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE) // todo api 33 上直接拒绝
            ),
            Link(
                title = "SketchZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.SketchZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "CoilZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.CoilZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                minSdk = 21
            ),
            Link(
                title = "GlideZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.GlideZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "PicassoZoomImageView（My）",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.PicassoZoomImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "PhotoView",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.PhotoView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            Link(
                title = "SubsamplingScaleImageView",
                navDirections = NavMainDirections.actionGlobalPhotoAlbumFragment(ZoomViewType.SubsamplingScaleImageView.name),
                permissions = listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),

            ListSeparator("Test"),
            Link(
                title = "ZoomImageView Layout Orientation Test",
                navDirections = NavMainDirections.actionGlobalLayoutOrientationTestFragment(),
            ),
            Link(
                title = "ZoomImageView Exif Orientation Test",
                navDirections = NavMainDirections.actionGlobalExifOrientationTestFragment(),
            ),
            // todo 增加 scales 测试，提供正方形的，长的，宽的，超宽的，超长的，超宽超长的图片
        )
    }
}