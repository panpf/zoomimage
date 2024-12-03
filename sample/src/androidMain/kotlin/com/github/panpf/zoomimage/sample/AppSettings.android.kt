package com.github.panpf.zoomimage.sample

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.logo_basic
import com.github.panpf.zoomimage.sample.resources.logo_coil
import com.github.panpf.zoomimage.sample.resources.logo_glide
import com.github.panpf.zoomimage.sample.resources.logo_sketch
import com.github.panpf.zoomimage.sample.ui.model.ImageLoaderSettingItem
import com.github.panpf.zoomimage.sample.ui.util.name
import com.github.panpf.zoomimage.sample.util.ParamLazy
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.sample.util.booleanSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.floatSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.intSettingsStateFlow
import com.github.panpf.zoomimage.sample.util.isDebugMode
import com.github.panpf.zoomimage.sample.util.stateMap
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow
import com.github.panpf.zoomimage.subsampling.internal.TileManager
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.zoom.AlignmentCompat
import com.github.panpf.zoomimage.zoom.ContentScaleCompat
import com.github.panpf.zoomimage.zoom.ScalesCalculator
import com.github.panpf.zoomimage.zoom.valueOf
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.painterResource

private val appSettingsLazy = ParamLazy<PlatformContext, AppSettings> { AppSettings(it) }

actual val PlatformContext.appSettings: AppSettings
    get() = appSettingsLazy.get(this.applicationContext)

val Fragment.appSettings: AppSettings
    get() = this.requireContext().appSettings

val View.appSettings: AppSettings
    get() = this.context.appSettings

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

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AppSettings actual constructor(val context: PlatformContext) {

    // ---------------------------------------- ZoomImage ------------------------------------------

    actual val contentScaleName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "contentScale", ContentScale.Fit.name)
    }
    actual val contentScale: StateFlow<ContentScaleCompat> =
        contentScaleName.stateMap { ContentScaleCompat.valueOf(it) }

    actual val alignmentName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "alignment", Alignment.Center.name)
    }
    actual val alignment: StateFlow<AlignmentCompat> =
        alignmentName.stateMap { AlignmentCompat.valueOf(it) }

    actual val animateScale: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "animateScale", true)
    }

    actual val rubberBandScale: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "rubberBandScale", true)
    }

    actual val threeStepScale: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "threeStepScale", false)
    }

    actual val slowerScaleAnimation: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "slowerScaleAnimation", false)
    }

    actual val reverseMouseWheelScale: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "reverseMouseWheelScale", false)
    }

    actual val scalesCalculatorName: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "scalesCalculator", "Dynamic")
    }
    actual val scalesMultiple: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "scalesMultiple", ScalesCalculator.MULTIPLE.toString())
    }
    // stateCombine will cause UI lag
//    actual val scalesCalculator: StateFlow<ScalesCalculator> =
//        stateCombine(listOf(scalesCalculatorName, scalesMultiple)) {
//            val scalesCalculatorName: String = it[0]
//            val scalesMultiple: Float = it[1].toFloat()
//            buildScalesCalculator(scalesCalculatorName, scalesMultiple)
//        }

    actual val disabledGestureTypes: SettingsStateFlow<Int> by lazy {
        intSettingsStateFlow(context, "disabledGestureTypes", 0)
    }

    actual val limitOffsetWithinBaseVisibleRect: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "limitOffsetWithinBaseVisibleRect", false)
    }

    actual val containerWhitespaceMultiple: SettingsStateFlow<Float> by lazy {
        floatSettingsStateFlow(context, "containerWhitespaceMultiple1", 0f)
    }

    actual val containerWhitespace: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "containerWhitespace", false)
    }

    actual val readModeEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "readModeEnabled", true)
    }

    actual val readModeAcceptedBoth: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "readModeAcceptedBoth", true)
    }

    actual val pausedContinuousTransformTypes by lazy {
        val initialize = TileManager.DefaultPausedContinuousTransformTypes
        intSettingsStateFlow(context, "pausedContinuousTransformTypes", initialize)
    }

    actual val disabledBackgroundTiles: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "disabledBackgroundTiles", false)
    }

    actual val showTileBounds: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "showTileBounds", false)
    }

    actual val tileAnimation: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "tileAnimation", true)
    }

    actual val tileMemoryCache: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "tileMemoryCache", true)
    }

    actual val scrollBarEnabled: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "scrollBarEnabled", true)
    }


    // ------------------------------------------ other --------------------------------------------

    val composePage: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "composePage", true)
    }

    val viewImageLoader: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "viewImageLoader", "Sketch")
    }

    actual val composeImageLoader: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "composeImageLoader", composeImageLoaders.first().name)
    }

    actual val currentPageIndex: SettingsStateFlow<Int> by lazy {
        intSettingsStateFlow(context, "currentPageIndex", 0)
    }

    actual val horizontalPagerLayout: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "horizontalPagerLayout", true)
    }

    actual val staggeredGridMode: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(
            context = context,
            key = "staggeredGridMode",
            initialize = false,
        )
    }

    actual val logLevelName: SettingsStateFlow<String> by lazy {
        val initialize = if (isDebugMode()) Logger.Level.Debug.name else Logger.Level.Info.name
        stringSettingsStateFlow(context, "logLevel3", initialize)
    }
    actual val logLevel: StateFlow<Logger.Level> =
        logLevelName.stateMap { Logger.Level.valueOf(it) }

    actual val debugLog: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "debugLog", isDebugMode())
    }

    actual val pagerGuideShowed: SettingsStateFlow<Boolean> by lazy {
        booleanSettingsStateFlow(context, "pagerGuideShowed", false)
    }
}