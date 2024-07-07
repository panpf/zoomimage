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
    ImageLoaderSettingItem("Basic", "List: Image + Sketch\nDetail: ZoomAsyncImage"),
)

val viewImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: Sketch+ImageView\nDetail: SketchZoomImageView"),
    ImageLoaderSettingItem("Coil", "List: Coil+ImageView\nDetail: CoilZoomImageView"),
    ImageLoaderSettingItem("Glide", "List: Glide+ImageView\nDetail: GlideZoomImageView"),
    ImageLoaderSettingItem("Picasso", "List: Picasso+ImageView\nDetail: PicassoZoomImageView"),
    ImageLoaderSettingItem("Basic", "List: Sketch+ImageView\nDetail: ZoomImageView"),
)