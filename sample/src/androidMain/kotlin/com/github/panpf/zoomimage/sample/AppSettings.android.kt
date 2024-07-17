package com.github.panpf.zoomimage.sample

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.logo_basic
import com.github.panpf.zoomimage.sample.resources.logo_coil
import com.github.panpf.zoomimage.sample.resources.logo_glide
import com.github.panpf.zoomimage.sample.resources.logo_sketch
import com.github.panpf.zoomimage.sample.util.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.util.ParamLazy
import org.jetbrains.compose.resources.painterResource

private val appSettingsLazy = ParamLazy<PlatformContext, AppSettings> { AppSettings(it) }

actual val PlatformContext.appSettings: AppSettings
    get() = appSettingsLazy.get(this.applicationContext)

val Fragment.appSettings: AppSettings
    get() = this.requireContext().appSettings

val View.appSettings: AppSettings
    get() = this.context.appSettings

actual fun isDebugMode(): Boolean = BuildConfig.DEBUG

actual val composeImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: AsyncImage (Sketch)\nDetail: SketchZoomAsyncImage"),
    ImageLoaderSettingItem("Coil", "List: AsyncImage (Coil)\nDetail: CoilZoomAsyncImage"),
    ImageLoaderSettingItem("Glide", "List: GlideImage\nDetail: GlideZoomAsyncImage"),
    ImageLoaderSettingItem("Basic", "List: Image + Sketch\nDetail: ZoomImage + Sketch"),
)

@Composable
actual fun getComposeImageLoaderIcon(composeImageLoader: String): Painter {
    return when (composeImageLoader) {
        "Sketch" -> painterResource(Res.drawable.logo_sketch)
        "Coil" -> painterResource(Res.drawable.logo_coil)
        "Glide" -> painterResource(Res.drawable.logo_glide)
        "Basic" -> painterResource(Res.drawable.logo_basic)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}

val viewImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: ImageView + Sketch\nDetail: SketchZoomImageView"),
    ImageLoaderSettingItem("Coil", "List: ImageView + Coil\nDetail: CoilZoomImageView"),
    ImageLoaderSettingItem("Glide", "List: ImageView + Glide\nDetail: GlideZoomImageView"),
    ImageLoaderSettingItem("Picasso", "List: ImageView + Picasso\nDetail: PicassoZoomImageView"),
    ImageLoaderSettingItem("Basic", "List: ImageView + Sketch\nDetail: ZoomImageView + Sketch"),
)

fun getViewImageLoaderIcon(context: Context, viewImageLoader: String): Drawable {
    val resources = context.resources
    return when (viewImageLoader) {
        "Sketch" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_sketch, null)!!
        "Coil" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_coil, null)!!
        "Glide" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_glide, null)!!
        "Picasso" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_square, null)!!
        "Basic" -> ResourcesCompat.getDrawable(resources, R.drawable.logo_basic, null)!!
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $viewImageLoader")
    }
}