package com.github.panpf.zoomimage.sample

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.ui.model.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.sample.util.booleanSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow
import org.jetbrains.compose.resources.painterResource

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

actual class AppSettings actual constructor(context: PlatformContext) : BaseAppSettings(context) {

    val composePage: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "composePage", true)
    }

    val viewImageLoader: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "viewImageLoader", "Sketch")
    }
}

actual fun platformSupportedDarkModes(): List<DarkMode> {
    return if (VERSION.SDK_INT >= VERSION_CODES.O) {
        DarkMode.entries
    } else {
        listOf(DarkMode.LIGHT, DarkMode.DARK)
    }
}

fun applyDarkMode(appSettings: AppSettings) {
    val mode = when (appSettings.darkMode.value) {
        DarkMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        DarkMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        DarkMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}