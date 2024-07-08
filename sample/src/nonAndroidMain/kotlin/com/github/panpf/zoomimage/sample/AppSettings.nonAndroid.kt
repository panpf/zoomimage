package com.github.panpf.zoomimage.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.logo_basic
import com.github.panpf.zoomimage.sample.resources.logo_coil
import com.github.panpf.zoomimage.sample.resources.logo_sketch
import com.github.panpf.zoomimage.sample.util.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.util.ParamLazy
import org.jetbrains.compose.resources.painterResource

private val appSettingsLazy = ParamLazy<PlatformContext, AppSettings> { AppSettings(it) }

actual val PlatformContext.appSettings: AppSettings
    get() = appSettingsLazy.get(this)

actual val composeImageLoaders: List<ImageLoaderSettingItem> = listOf(
    ImageLoaderSettingItem("Sketch", "List: AsyncImage (Sketch)\nDetail: SketchZoomAsyncImage"),
    ImageLoaderSettingItem("Coil", "List: AsyncImage (Coil)\nDetail: CoilZoomAsyncImage"),
    ImageLoaderSettingItem("Basic", "List: Image + Sketch\nDetail: ZoomAsyncImage"),
)

@Composable
actual fun getComposeImageLoaderIcon(composeImageLoader: String): Painter {
    return when (composeImageLoader) {
        "Sketch" -> painterResource(Res.drawable.logo_sketch)
        "Coil" -> painterResource(Res.drawable.logo_coil)
        "Basic" -> painterResource(Res.drawable.logo_basic)
        else -> throw IllegalArgumentException("Unsupported composeImageLoader: $composeImageLoader")
    }
}