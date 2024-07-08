package com.github.panpf.zoomimage.sample

import android.view.View
import androidx.fragment.app.Fragment
import com.github.panpf.zoomimage.sample.util.ImageLoaderSettingItem

val Fragment.appSettings: AppSettings
    get() = this.requireContext().appSettings
val View.appSettings: AppSettings
    get() = this.context.appSettings

actual fun isDebugMode(): Boolean = BuildConfig.DEBUG

actual val composeImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: AsyncImage(Sketch)\nDetail: SketchZoomAsyncImage"),
    ImageLoaderSettingItem("Coil", "List: AsyncImage(Coil)\nDetail: CoilZoomAsyncImage"),
    ImageLoaderSettingItem("Glide", "List: GlideImage\nDetail: GlideZoomAsyncImage"),
    ImageLoaderSettingItem("Basic", "List: Image+Sketch\nDetail: ZoomImage+Sketch"),
)

val viewImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: ImageView+Sketch\nDetail: SketchZoomImageView"),
    ImageLoaderSettingItem("Coil", "List: ImageView+Coil\nDetail: CoilZoomImageView"),
    ImageLoaderSettingItem("Glide", "List: ImageView+Glide\nDetail: GlideZoomImageView"),
    ImageLoaderSettingItem("Picasso", "List: ImageView+Picasso\nDetail: PicassoZoomImageView"),
    ImageLoaderSettingItem("Basic", "List: ImageView+Sketch\nDetail: ZoomImageView+Sketch"),
)